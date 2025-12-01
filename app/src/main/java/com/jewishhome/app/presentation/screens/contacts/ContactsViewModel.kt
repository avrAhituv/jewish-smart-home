package com.jewishhome.app.presentation.screens.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.domain.model.Contact
import com.jewishhome.app.domain.model.ContactGroup
import com.jewishhome.app.domain.model.SpeedDial
import com.jewishhome.app.domain.repository.ContactsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
        observeSpeedDials()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val contacts = contactsRepository.getAllContacts()
                val favorites = contactsRepository.getFavoriteContacts()
                val recent = contactsRepository.getRecentContacts()
                val groups = contactsRepository.getContactGroups()

                _uiState.value = _uiState.value.copy(
                    contacts = contacts,
                    filteredContacts = contacts,
                    favorites = favorites,
                    recentContacts = recent,
                    groups = groups,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private fun observeSpeedDials() {
        viewModelScope.launch {
            contactsRepository.getSpeedDials().collect { speedDials ->
                _uiState.value = _uiState.value.copy(speedDials = speedDials)
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        viewModelScope.launch {
            val filtered = if (query.isBlank()) {
                _uiState.value.contacts
            } else {
                contactsRepository.searchContacts(query)
            }
            _uiState.value = _uiState.value.copy(filteredContacts = filtered)
        }
    }

    fun selectTab(tab: ContactsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectContact(contact: Contact) {
        _uiState.value = _uiState.value.copy(selectedContact = contact)
    }

    fun clearSelectedContact() {
        _uiState.value = _uiState.value.copy(selectedContact = null)
    }

    fun callContact(contact: Contact) {
        contact.phoneNumbers.firstOrNull()?.let { phone ->
            callNumber(phone.number, contact)
        }
    }

    fun callNumber(number: String, contact: Contact? = null) {
        viewModelScope.launch {
            try {
                contactsRepository.makeCall(number)
                contact?.let {
                    contactsRepository.markAsContacted(it.id)
                    refreshRecentContacts()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "שגיאה בביצוע השיחה: ${e.message}")
            }
        }
    }

    private fun refreshRecentContacts() {
        viewModelScope.launch {
            val recent = contactsRepository.getRecentContacts()
            _uiState.value = _uiState.value.copy(recentContacts = recent)
        }
    }

    fun syncContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = contactsRepository.syncContacts()
                if (success) {
                    loadContacts()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "שגיאה בסנכרון אנשי קשר"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun setSpeedDial(position: Int, contact: Contact, phoneNumber: String) {
        viewModelScope.launch {
            contactsRepository.setSpeedDial(position, contact.id, phoneNumber)
        }
    }

    fun removeSpeedDial(position: Int) {
        viewModelScope.launch {
            contactsRepository.removeSpeedDial(position)
        }
    }

    fun selectGroup(group: ContactGroup) {
        viewModelScope.launch {
            val contacts = contactsRepository.getContactsInGroup(group.id)
            _uiState.value = _uiState.value.copy(
                filteredContacts = contacts,
                selectedTab = ContactsTab.CONTACTS
            )
        }
    }

    fun showAllContacts() {
        _uiState.value = _uiState.value.copy(filteredContacts = _uiState.value.contacts)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun showDialpad(show: Boolean) {
        _uiState.value = _uiState.value.copy(showDialpad = show)
    }

    fun updateDialpadNumber(number: String) {
        _uiState.value = _uiState.value.copy(dialpadNumber = number)
    }
}

data class ContactsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val contacts: List<Contact> = emptyList(),
    val filteredContacts: List<Contact> = emptyList(),
    val favorites: List<Contact> = emptyList(),
    val recentContacts: List<Contact> = emptyList(),
    val groups: List<ContactGroup> = emptyList(),
    val speedDials: List<SpeedDial> = emptyList(),
    val selectedContact: Contact? = null,
    val selectedTab: ContactsTab = ContactsTab.CONTACTS,
    val searchQuery: String = "",
    val showDialpad: Boolean = false,
    val dialpadNumber: String = ""
)

enum class ContactsTab {
    CONTACTS,
    FAVORITES,
    RECENT,
    GROUPS,
    SPEED_DIAL
}
