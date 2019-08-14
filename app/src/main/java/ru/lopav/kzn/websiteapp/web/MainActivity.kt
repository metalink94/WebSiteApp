package ru.lopav.kzn.websiteapp.web

import android.Manifest
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.webkit.*
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import ru.lopav.kzn.websiteapp.BuildConfig
import ru.lopav.kzn.websiteapp.R
import android.webkit.CookieSyncManager
import android.view.WindowManager



class MainActivity: AppCompatActivity() {

    var currentUrl: String = BuildConfig.URL
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var mUploadMessage: ValueCallback<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        currentUrl = intent.getStringExtra(KEY_URL)
        checkPermissions()
        initWebView()
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            load()
        }
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Companion.PICK_FROM_GALLERY
            )
        }
    }


    private fun initWebView() {
        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(
                    webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage?.onReceiveValue(null)
                    uploadMessage = null
                }

                uploadMessage = filePathCallback
                var intent: Intent? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent = fileChooserParams?.createIntent()
                }
                try {
                    startActivityForResult(intent, Companion.PICK_FROM_GALLERY_2)
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    Toast.makeText(applicationContext, "Cannot Open File Chooser", Toast.LENGTH_LONG).show()
                    return false
                }

                return true
            }

            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                Log.d("LogTag", message)
                result?.confirm()
                return true;
            }
        }
        webView.webViewClient = object : WvClient() {

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val uri = request?.url
                return shouldOverrideUrlLoading(uri.toString())
            }

            @Deprecated("shouldOverrideUrlLoading")
            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                return shouldOverrideUrlLoading(url ?: "")
            }

            private fun shouldOverrideUrlLoading(url: String): Boolean {
                if (url.startsWith("market")) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    finish()
                    return true
                }
                currentUrl = url
                Log.d("WebView", "get Url $currentUrl")
//                load()
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
            }
        }
        CookieSyncManager.createInstance(this)
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
//        webView.clearCache(true)
        webView.clearHistory()
        webView.setInitialScale(1)
        webView.settings.setAppCacheEnabled(true)
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isScrollbarFadingEnabled = false
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.settings.domStorageEnabled = true
        load()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private fun load() {
        webView.loadUrl(currentUrl)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.PICK_FROM_GALLERY_2) {
            if (uploadMessage == null) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                uploadMessage?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            }
            uploadMessage = null
        } else if (requestCode == Companion.PICK_FROM_GALLERY_3) {
            if (null == mUploadMessage) return
            val result = if (data == null || resultCode != RESULT_OK) {
                null
            } else {
                data.data
            }
            mUploadMessage?.onReceiveValue(result);
            mUploadMessage = null
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        const val KEY_URL = "url"

        fun getInstance(context: Context, url: String?): Intent =
                Intent(context, MainActivity::class.java)
                        .putExtra(KEY_URL, url)

        private const val PICK_FROM_GALLERY_3 = 3
        private const val PICK_FROM_GALLERY_2 = 2
        private const val PICK_FROM_GALLERY = 1
    }
}

