package com.jewishhome.app.presentation.screens.recipes

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.jewishhome.app.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showBrowser) {
        RecipesBrowser(
            url = uiState.currentUrl,
            viewModel = viewModel,
            onClose = { viewModel.closeBrowser() }
        )
    } else {
        RecipesSiteList(
            sites = viewModel.favoriteRecipeSites,
            onSiteClick = { viewModel.selectSite(it) },
            onNavigateBack = onNavigateBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipesSiteList(
    sites: List<RecipeSite>,
    onSiteClick: (RecipeSite) -> Unit,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Primary, PrimaryVariant)
                )
            )
    ) {
        TopAppBar(
            title = { Text("מתכונים", color = Color.White) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowForward, "חזרה", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "אתרי מתכונים כשרים",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "בחר אתר מתכונים להתחיל",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        // Sites List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sites) { site ->
                RecipeSiteCard(
                    site = site,
                    onClick = { onSiteClick(site) }
                )
            }
        }
    }
}

@Composable
private fun RecipeSiteCard(
    site: RecipeSite,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Secondary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = site.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = site.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Icon(
            Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecipesBrowser(
    url: String,
    viewModel: RecipesViewModel,
    onClose: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = uiState.canGoBack) {
        webView?.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Browser Toolbar
        TopAppBar(
            title = {
                Text(
                    text = uiState.pageTitle.ifEmpty { "טוען..." },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White
                )
            },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "סגור", tint = Color.White)
                }
            },
            actions = {
                // Navigation buttons
                IconButton(
                    onClick = { webView?.goBack() },
                    enabled = uiState.canGoBack
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        "אחורה",
                        tint = if (uiState.canGoBack) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
                IconButton(
                    onClick = { webView?.goForward() },
                    enabled = uiState.canGoForward
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        "קדימה",
                        tint = if (uiState.canGoForward) Color.White else Color.White.copy(alpha = 0.3f)
                    )
                }
                IconButton(onClick = { webView?.reload() }) {
                    Icon(Icons.Default.Refresh, "רענן", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Primary)
        )

        // Loading Progress
        if (uiState.loadingProgress in 1..99) {
            LinearProgressIndicator(
                progress = uiState.loadingProgress / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = Secondary
            )
        }

        // WebView
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.setSupportZoom(true)

                    // Block ads and enforce whitelist
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val requestUrl = request?.url?.toString() ?: return false

                            // Block if not in whitelist
                            if (!viewModel.isUrlAllowed(requestUrl)) {
                                return true // Block navigation
                            }

                            return false // Allow navigation
                        }

                        override fun shouldInterceptRequest(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): WebResourceResponse? {
                            val requestUrl = request?.url?.toString() ?: return null

                            // Block ads
                            if (viewModel.shouldBlockUrl(requestUrl)) {
                                return WebResourceResponse(
                                    "text/plain",
                                    "UTF-8",
                                    null
                                )
                            }

                            return null
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            viewModel.updateProgress(10)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            viewModel.updateProgress(100)
                            viewModel.updateCanGoBack(view?.canGoBack() ?: false)
                            viewModel.updateCanGoForward(view?.canGoForward() ?: false)

                            // Inject CSS to hide common ad elements
                            view?.evaluateJavascript("""
                                (function() {
                                    var style = document.createElement('style');
                                    style.innerHTML = `
                                        [class*="ad-"], [class*="ads-"], [class*="advert"],
                                        [id*="ad-"], [id*="ads-"], [id*="advert"],
                                        .advertisement, .ad-container, .ad-wrapper,
                                        .google-ad, .banner-ad, .sidebar-ad,
                                        iframe[src*="doubleclick"], iframe[src*="googlesyndication"],
                                        div[data-ad], div[data-adunit] {
                                            display: none !important;
                                        }
                                    `;
                                    document.head.appendChild(style);
                                })();
                            """.trimIndent(), null)
                        }
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            viewModel.updateProgress(newProgress)
                        }

                        override fun onReceivedTitle(view: WebView?, title: String?) {
                            super.onReceivedTitle(view, title)
                            viewModel.updatePageTitle(title ?: "")
                        }
                    }

                    loadUrl(url)
                    webView = this
                }
            },
            update = { view ->
                webView = view
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
