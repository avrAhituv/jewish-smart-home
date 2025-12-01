package com.jewishhome.app.domain.repository

import com.jewishhome.app.domain.model.BrachaCategory
import com.jewishhome.app.domain.model.BrachaText
import com.jewishhome.app.domain.model.Nusach
import com.jewishhome.app.domain.model.TextCategory
import kotlinx.coroutines.flow.Flow

interface TextsRepository {
    fun getCategories(): List<TextCategory>
    fun getTextsByCategory(category: BrachaCategory): List<BrachaText>
    fun getTextById(id: String): BrachaText?
    fun searchTexts(query: String): List<BrachaText>

    fun getCurrentNusach(): Flow<Nusach>
    suspend fun setNusach(nusach: Nusach)

    fun getFontSize(): Flow<Float>
    suspend fun setFontSize(size: Float)
}
