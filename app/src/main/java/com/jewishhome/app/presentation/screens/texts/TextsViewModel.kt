package com.jewishhome.app.presentation.screens.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.domain.model.BrachaText
import com.jewishhome.app.domain.model.Nusach
import com.jewishhome.app.domain.model.TextCategory
import com.jewishhome.app.domain.repository.TextsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TextsViewModel @Inject constructor(
    private val textsRepository: TextsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextsUiState())
    val uiState: StateFlow<TextsUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
        observeSettings()
    }

    private fun loadCategories() {
        val categories = textsRepository.getCategories()
        _uiState.value = _uiState.value.copy(categories = categories)
    }

    private fun observeSettings() {
        viewModelScope.launch {
            textsRepository.getCurrentNusach().collect { nusach ->
                _uiState.value = _uiState.value.copy(currentNusach = nusach)
            }
        }
        viewModelScope.launch {
            textsRepository.getFontSize().collect { size ->
                _uiState.value = _uiState.value.copy(fontSize = size)
            }
        }
    }

    fun selectCategory(category: TextCategory) {
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedText = null
        )
    }

    fun selectText(text: BrachaText) {
        _uiState.value = _uiState.value.copy(selectedText = text)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            selectedText = null
        )
    }

    fun goBackToCategory() {
        _uiState.value = _uiState.value.copy(selectedText = null)
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        } else {
            val results = textsRepository.searchTexts(query)
            _uiState.value = _uiState.value.copy(searchResults = results)
        }
    }

    fun setNusach(nusach: Nusach) {
        viewModelScope.launch {
            textsRepository.setNusach(nusach)
        }
    }

    fun increaseFontSize() {
        val currentSize = _uiState.value.fontSize
        if (currentSize < 32f) {
            viewModelScope.launch {
                textsRepository.setFontSize(currentSize + 2f)
            }
        }
    }

    fun decreaseFontSize() {
        val currentSize = _uiState.value.fontSize
        if (currentSize > 14f) {
            viewModelScope.launch {
                textsRepository.setFontSize(currentSize - 2f)
            }
        }
    }

    fun getTextForCurrentNusach(text: BrachaText): String {
        return when (_uiState.value.currentNusach) {
            Nusach.SEFARD -> text.textSefard ?: text.textAshkenaz
            Nusach.EDOT_MIZRACH -> text.textEdotMizrach ?: text.textAshkenaz
            else -> text.textAshkenaz
        }
    }
}

data class TextsUiState(
    val categories: List<TextCategory> = emptyList(),
    val selectedCategory: TextCategory? = null,
    val selectedText: BrachaText? = null,
    val searchQuery: String = "",
    val searchResults: List<BrachaText> = emptyList(),
    val currentNusach: Nusach = Nusach.ASHKENAZ,
    val fontSize: Float = 20f
)
