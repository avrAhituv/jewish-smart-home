package com.jewishhome.app.domain.repository

import com.jewishhome.app.domain.model.Contact
import com.jewishhome.app.domain.model.ContactGroup
import com.jewishhome.app.domain.model.SpeedDial
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {

    // Contact queries
    suspend fun getAllContacts(): List<Contact>
    suspend fun searchContacts(query: String): List<Contact>
    suspend fun getContactById(id: String): Contact?
    suspend fun getFavoriteContacts(): List<Contact>
    suspend fun getRecentContacts(limit: Int = 10): List<Contact>

    // Contact groups
    suspend fun getContactGroups(): List<ContactGroup>
    suspend fun getContactsInGroup(groupId: String): List<Contact>

    // Speed dial
    fun getSpeedDials(): Flow<List<SpeedDial>>
    suspend fun setSpeedDial(position: Int, contactId: String, phoneNumber: String)
    suspend fun removeSpeedDial(position: Int)

    // Actions
    suspend fun makeCall(phoneNumber: String)
    suspend fun markAsContacted(contactId: String)

    // Sync
    suspend fun syncContacts(): Boolean
}
