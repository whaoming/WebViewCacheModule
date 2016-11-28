package com.wxxiaomi.ming.webviewcachemodule.version_4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;

import com.github.lzyzsd.jsbridge.BridgeHandler;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.BridgeWebViewClient;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.wxxiaomi.ming.webviewcachemodule.R;
import com.wxxiaomi.ming.webviewcachemodule.common.PicTakeUtil;
import com.wxxiaomi.ming.webviewcachemodule.common.util.CacheEngine;
import com.wxxiaomi.ming.webviewcachemodule.common.util.DiskCache;
import com.yancy.gallerypick.inter.IHandlerCallBack;

import java.io.ByteArrayInputStream;
import java.util.List;

import rx.functions.Action1;

public class StreamActivity extends AppCompatActivity {
    protected BridgeWebView mWebView;
    private Button btn;
    private PicTakeUtil util;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        mWebView = (BridgeWebView) findViewById(R.id.web_view);
        mWebView.setWebViewClient(new MyWebViewClient(mWebView));
        mWebView.loadUrl("file:///android_asset/test1.html");
        mWebView.getSettings().setAllowFileAccess(true);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        DiskCache.getInstance().open(getApplicationContext());
    }

    private void takePicture() {
        util = new PicTakeUtil(this);
        util.takePicture(new IHandlerCallBack() {
            @Override
            public void onStart() {
            }

            @Override
            public void onSuccess(List<String> photoList) {
                adapter(photoList);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onFinish() {

            }

            @Override
            public void onError() {

            }
        });
    }

    private void adapter(List<String> photoList) {
        String result = "";
        for(int i=0;i<photoList.size();i++){
            result += "<img src=\"http://localhost"+photoList.get(i)+"\" height=\"70\" width=\"70\" border=\"2\"/>";
        }
        mWebView.callHandler("adapter",result,null);
    }


    class MyWebViewClient extends BridgeWebViewClient {
        public MyWebViewClient(BridgeWebView webView) {
            super(webView);
        }
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            String key = "http://localhost";
            final WebResourceResponse[] response = {null};
            if(url.contains(key)){
                String imgPath = url.replace(key,"");
                CacheEngine.getInstance().getLocalImgInMain(imgPath)
                        .subscribe(new Action1<byte[]>() {
                            @Override
                            public void call(byte[] bytes) {
                                response[0] = new WebResourceResponse("image/png", "UTF-8", new ByteArrayInputStream(bytes));
                            }
                        });
                return response[0];
            }else {
                return super.shouldInterceptRequest(view, url);
            }
        }
    }
}
