package com.jewishhome.app.presentation.screens.recipes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RecipesUiState())
    val uiState: StateFlow<RecipesUiState> = _uiState.asStateFlow()

    // Whitelist of allowed recipe websites
    val allowedDomains = listOf(
        "nikib.co.il",
        "www.nikib.co.il",
        "oneg-shabat.co.il",
        "www.oneg-shabat.co.il",
        "kosher.com",
        "www.kosher.com",
        "kosherfood.co.il",
        "www.kosherfood.co.il",
        "matkonita.co.il",
        "www.matkonita.co.il",
        "foodish.co.il",
        "www.foodish.co.il",
        "shiratshabbat.co.il",
        "www.shiratshabbat.co.il"
    )

    val favoriteRecipeSites = listOf(
        RecipeSite("ניקי ב", "https://nikib.co.il", "מתכוני שבת וחג"),
        RecipeSite("עונג שבת", "https://oneg-shabat.co.il", "מתכונים כשרים"),
        RecipeSite("Kosher.com", "https://kosher.com", "מתכונים כשרים באנגלית"),
        RecipeSite("מטקוניטה", "https://matkonita.co.il", "מתכונים ישראלים"),
        RecipeSite("פודיש", "https://foodish.co.il", "מתכונים ביתיים")
    )

    // Ad block patterns - common ad domains to block
    val adBlockPatterns = listOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "google-analytics.com",
        "facebook.com/tr",
        "facebook.net",
        "ads.",
        "ad.",
        "adserver",
        "advertising",
        "adtrack",
        "advert",
        "banner",
        "popup",
        "taboola.com",
        "outbrain.com",
        "criteo.com",
        "amazon-adsystem.com",
        "moatads.com",
        "scorecardresearch.com",
        "chartbeat.com",
        "mixpanel.com",
        "segment.io",
        "hotjar.com",
        "crazyegg.com",
        "clicktale.net",
        "pingdom.net",
        "quantserve.com",
        "bluekai.com",
        "exelator.com",
        "rubiconproject.com",
        "pubmatic.com",
        "openx.net",
        "indexexchange.com",
        "casalemedia.com",
        "adsrvr.org",
        "bidswitch.net",
        "mediamath.com",
        "appnexus.com"
    )

    fun selectSite(site: RecipeSite) {
        _uiState.value = _uiState.value.copy(
            currentUrl = site.url,
            showBrowser = true
        )
    }

    fun loadUrl(url: String) {
        if (isUrlAllowed(url)) {
            _uiState.value = _uiState.value.copy(
                currentUrl = url,
                showBrowser = true
            )
        }
    }

    fun closeBrowser() {
        _uiState.value = _uiState.value.copy(
            showBrowser = false,
            currentUrl = ""
        )
    }

    fun updateProgress(progress: Int) {
        _uiState.value = _uiState.value.copy(loadingProgress = progress)
    }

    fun updateCanGoBack(canGoBack: Boolean) {
        _uiState.value = _uiState.value.copy(canGoBack = canGoBack)
    }

    fun updateCanGoForward(canGoForward: Boolean) {
        _uiState.value = _uiState.value.copy(canGoForward = canGoForward)
    }

    fun updatePageTitle(title: String) {
        _uiState.value = _uiState.value.copy(pageTitle = title)
    }

    fun isUrlAllowed(url: String): Boolean {
        val host = try {
            java.net.URL(url).host
        } catch (e: Exception) {
            return false
        }

        return allowedDomains.any { domain ->
            host == domain || host.endsWith(".$domain")
        }
    }

    fun shouldBlockUrl(url: String): Boolean {
        val lowerUrl = url.lowercase()
        return adBlockPatterns.any { pattern ->
            lowerUrl.contains(pattern)
        }
    }

    fun addToFavorites(url: String, title: String) {
        // TODO: Implement favorites persistence
    }
}

data class RecipesUiState(
    val showBrowser: Boolean = false,
    val currentUrl: String = "",
    val loadingProgress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val pageTitle: String = ""
)

data class RecipeSite(
    val name: String,
    val url: String,
    val description: String
)
