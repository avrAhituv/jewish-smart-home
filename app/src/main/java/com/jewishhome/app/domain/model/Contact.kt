package com.jewishhome.app.domain.model

import android.net.Uri

data class Contact(
    val id: String,
    val displayName: String,
    val phoneNumbers: List<PhoneNumber>,
    val emails: List<String>,
    val photoUri: Uri?,
    val isFavorite: Boolean = false,
    val lastContacted: Long? = null
)

data class PhoneNumber(
    val number: String,
    val type: PhoneType,
    val label: String? = null
)

enum class PhoneType {
    MOBILE,
    HOME,
    WORK,
    OTHER
}

data class ContactGroup(
    val id: String,
    val name: String,
    val contacts: List<Contact>
)

// For quick dial / speed dial
data class SpeedDial(
    val position: Int, // 1-9
    val contactId: String,
    val phoneNumber: String
)
