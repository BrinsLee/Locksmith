package com.brins.locksmith.ui.activity

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.brins.locksmith.R
import com.brins.locksmith.utils.getStatusBarHeight
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.header.*

class WebViewActivity : BaseActivity() {
    private var mURL: String? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_web_view
    }

    override fun onCreateBeforeBinding(savedInstanceState: Bundle?) {
        super.onCreateBeforeBinding(savedInstanceState)
        mURL = intent.getStringExtra("URL")
    }

    override fun onCreateAfterBinding(savedInstanceState: Bundle?) {
        super.onCreateAfterBinding(savedInstanceState)
        header_layout.setPadding(0, getStatusBarHeight(this), 0, 0)
        val webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                title_tv.text = title
            }
        }
        webView.webChromeClient = webChromeClient
        webView.loadUrl(mURL)

        val webSettings = webView.getSettings()
        webSettings.javaScriptEnabled = true
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
            finish()
        }
    }
}
