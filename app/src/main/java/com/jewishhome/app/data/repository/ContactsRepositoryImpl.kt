package com.jewishhome.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepositoryImpl @Inject constructor(
    private val context: Context,
    private val userPreferences: UserPreferences
) : ContactsRepository {

    private val contentResolver: ContentResolver = context.contentResolver
    private val contactsCache = mutableListOf<Contact>()
    private val speedDialsFlow = MutableStateFlow<List<SpeedDial>>(emptyList())
    private val recentContactsCache = mutableListOf<Contact>()

    override suspend fun getAllContacts(): List<Contact> = withContext(Dispatchers.IO) {
        if (contactsCache.isEmpty()) {
            syncContacts()
        }
        contactsCache.toList().sortedBy { it.displayName }
    }

    override suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        getAllContacts().filter { contact ->
            contact.displayName.contains(query, ignoreCase = true) ||
                    contact.phoneNumbers.any { it.number.contains(query) }
        }
    }

    override suspend fun getContactById(id: String): Contact? = withContext(Dispatchers.IO) {
        getAllContacts().find { it.id == id }
    }

    override suspend fun getFavoriteContacts(): List<Contact> = withContext(Dispatchers.IO) {
        getAllContacts().filter { it.isFavorite }
    }

    override suspend fun getRecentContacts(limit: Int): List<Contact> = withContext(Dispatchers.IO) {
        recentContactsCache.take(limit)
    }

    override suspend fun getContactGroups(): List<ContactGroup> = withContext(Dispatchers.IO) {
        val groups = mutableListOf<ContactGroup>()

        val projection = arrayOf(
            ContactsContract.Groups._ID,
            ContactsContract.Groups.TITLE
        )

        contentResolver.query(
            ContactsContract.Groups.CONTENT_URI,
            projection,
            "${ContactsContract.Groups.DELETED} = 0",
            null,
            "${ContactsContract.Groups.TITLE} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Groups._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(ContactsContract.Groups.TITLE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn).toString()
                val title = cursor.getString(titleColumn) ?: "ללא שם"

                groups.add(
                    ContactGroup(
                        id = id,
                        name = title,
                        contacts = emptyList()
                    )
                )
            }
        }

        groups
    }

    override suspend fun getContactsInGroup(groupId: String): List<Contact> = withContext(Dispatchers.IO) {
        val contactIds = mutableSetOf<String>()

        val projection = arrayOf(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID)
        val selection = "${ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID} = ?"
        val selectionArgs = arrayOf(groupId)

        contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID)

            while (cursor.moveToNext()) {
                contactIds.add(cursor.getLong(idColumn).toString())
            }
        }

        getAllContacts().filter { it.id in contactIds }
    }

    override fun getSpeedDials(): Flow<List<SpeedDial>> = speedDialsFlow

    override suspend fun setSpeedDial(position: Int, contactId: String, phoneNumber: String) {
        val currentList = speedDialsFlow.value.toMutableList()
        currentList.removeAll { it.position == position }
        currentList.add(SpeedDial(position, contactId, phoneNumber))
        speedDialsFlow.value = currentList.sortedBy { it.position }
    }

    override suspend fun removeSpeedDial(position: Int) {
        val currentList = speedDialsFlow.value.toMutableList()
        currentList.removeAll { it.position == position }
        speedDialsFlow.value = currentList
    }

    override suspend fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override suspend fun markAsContacted(contactId: String) = withContext(Dispatchers.IO) {
        getContactById(contactId)?.let { contact ->
            recentContactsCache.removeAll { it.id == contactId }
            recentContactsCache.add(0, contact.copy(lastContacted = System.currentTimeMillis()))
            while (recentContactsCache.size > 50) {
                recentContactsCache.removeLast()
            }
        }
    }

    override suspend fun syncContacts(): Boolean = withContext(Dispatchers.IO) {
        try {
            val contacts = mutableMapOf<String, Contact>()

            // First pass: Get basic contact info
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_URI,
                ContactsContract.Contacts.STARRED,
                ContactsContract.Contacts.LAST_TIME_CONTACTED
            )

            contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                projection,
                "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = 1",
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                val photoColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI)
                val starredColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.STARRED)
                val lastContactedColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.LAST_TIME_CONTACTED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn).toString()
                    val name = cursor.getString(nameColumn) ?: "ללא שם"
                    val photoUri = cursor.getString(photoColumn)?.let { Uri.parse(it) }
                    val starred = cursor.getInt(starredColumn) == 1
                    val lastContacted = cursor.getLong(lastContactedColumn).takeIf { it > 0 }

                    contacts[id] = Contact(
                        id = id,
                        displayName = name,
                        phoneNumbers = emptyList(),
                        emails = emptyList(),
                        photoUri = photoUri,
                        isFavorite = starred,
                        lastContacted = lastContacted
                    )
                }
            }

            // Second pass: Get phone numbers
            val phoneProjection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL
            )

            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                phoneProjection,
                null,
                null,
                null
            )?.use { cursor ->
                val contactIdColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
                val labelColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL)

                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(contactIdColumn).toString()
                    val number = cursor.getString(numberColumn) ?: continue
                    val type = cursor.getInt(typeColumn)
                    val label = cursor.getString(labelColumn)

                    contacts[contactId]?.let { contact ->
                        val phoneType = when (type) {
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> PhoneType.MOBILE
                            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> PhoneType.HOME
                            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> PhoneType.WORK
                            else -> PhoneType.OTHER
                        }

                        val updatedPhones = contact.phoneNumbers + PhoneNumber(
                            number = number,
                            type = phoneType,
                            label = label
                        )

                        contacts[contactId] = contact.copy(phoneNumbers = updatedPhones)
                    }
                }
            }

            // Third pass: Get emails
            val emailProjection = arrayOf(
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.ADDRESS
            )

            contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                emailProjection,
                null,
                null,
                null
            )?.use { cursor ->
                val contactIdColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
                val addressColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)

                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(contactIdColumn).toString()
                    val email = cursor.getString(addressColumn) ?: continue

                    contacts[contactId]?.let { contact ->
                        val updatedEmails = contact.emails + email
                        contacts[contactId] = contact.copy(emails = updatedEmails)
                    }
                }
            }

            contactsCache.clear()
            contactsCache.addAll(contacts.values)
            true
        } catch (e: Exception) {
            false
        }
    }
}
