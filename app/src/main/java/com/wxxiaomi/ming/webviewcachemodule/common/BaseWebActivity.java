package com.wxxiaomi.ming.webviewcachemodule.common;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 12262 on 2016/11/12.
 * webview的基础activity，用于webview的初始化
 * 不能含有toolbar，toolbar的初始化应该是在simpleActivity中
 */
public abstract class BaseWebActivity extends AppCompatActivity {
    protected BridgeWebView mWebView;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        int webViewId = initViewAndReutrnWebViewId(savedInstanceState);
        mWebView = (BridgeWebView) findViewById(webViewId);
        mWebView.setWebViewClient(new MyWebViewClient(mWebView));

        initWebView();
        initCommonMethod();
    }

    protected void initCommonMethod() {
        mWebView.registerHandler("showLog", new BridgeHandler() {
            @Override
            public void handler(String data, CallBackFunction function) {
                Log.i("wang","js->showLog:"+data);
            }
        });
    }

    /**
     * 自定义的WebViewClient
     */
    protected class MyWebViewClient extends BridgeWebViewClient {
        public MyWebViewClient(BridgeWebView webView) {
            super(webView);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }
    }

    protected abstract int initViewAndReutrnWebViewId(Bundle savedInstanceState);
    protected abstract void initWebView();

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // TODO Auto-generated method stub
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
