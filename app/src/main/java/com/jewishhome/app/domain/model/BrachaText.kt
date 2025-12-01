package com.jewishhome.app.domain.model

data class BrachaText(
    val id: String,
    val name: String,
    val category: BrachaCategory,
    val textAshkenaz: String,
    val textSefard: String? = null,
    val textEdotMizrach: String? = null,
    val transliteration: String? = null,
    val translation: String? = null,
    val instructions: String? = null
)

enum class BrachaCategory {
    BIRKAT_HAMAZON,     // ברכת המזון
    ASHER_YATZAR,       // אשר יצר
    MEZONOT,            // ברכות הנהנין
    TEFILAT_HADERECH,   // תפילת הדרך
    SHEMA,              // קריאת שמע על המיטה
    MORNING_BRACHOT,    // ברכות השחר
    HALLEL,             // הלל
    SPECIAL             // ברכות מיוחדות
}

enum class Nusach {
    ASHKENAZ,
    SEFARD,
    EDOT_MIZRACH
}

data class TextCategory(
    val id: String,
    val name: String,
    val icon: String,
    val texts: List<BrachaText>
)
