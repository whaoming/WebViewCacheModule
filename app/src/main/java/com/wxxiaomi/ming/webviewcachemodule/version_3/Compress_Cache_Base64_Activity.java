package com.wxxiaomi.ming.webviewcachemodule.version_3;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wxxiaomi.ming.webviewcachemodule.R;
import com.wxxiaomi.ming.webviewcachemodule.TAG;
import com.wxxiaomi.ming.webviewcachemodule.common.BaseWebActivity;
import com.wxxiaomi.ming.webviewcachemodule.common.PicTakeUtil;
import com.wxxiaomi.ming.webviewcachemodule.common.util.CacheEngine;
import com.wxxiaomi.ming.webviewcachemodule.common.util.DiskCache;
import com.yancy.gallerypick.inter.IHandlerCallBack;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Mr.W on ${DATE}.
 * E-maiil：122627018@qq.com
 * github：https://github.com/122627018
 * 使用压缩+缓存+base64编码完成(多线程工作)
 */
public class Compress_Cache_Base64_Activity extends BaseWebActivity {
    private Button btn;
    private PicTakeUtil util;


    @Override
    protected int initViewAndReutrnWebViewId(Bundle savedInstanceState) {
        setContentView(R.layout.activity_compress__cache__base64_);
        setContentView(R.layout.activity_version_2_);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        DiskCache.getInstance().open(getApplicationContext());
        return R.id.web_view;
    }

    @Override
    protected void initWebView() {
        mWebView.loadUrl("file:///android_asset/test1.html");
        mWebView.getSettings().setAllowFileAccess(true);
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
        CacheEngine.getInstance().getLocalImgsMany2(photoList)
                .flatMap(new Func1<byte[], Observable<String>>() {
                    @Override
                    public Observable<String> call(byte[] bytes) {
                        return byte2base64(bytes);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                sendToH5(s);
            }
        });
    }

    private void sendToH5(String s) {
        String result = "<img src=\"data:image/png;base64," + s + "\" height=\"70\" width=\"70\"/>";
            mWebView.callHandler("adapter",result,null);
    }

    public Observable<String> byte2base64(final byte[] bytes) {
        return
                Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        Log.i(com.wxxiaomi.ming.webviewcachemodule.TAG.TAG,"我在编码，我的线程："+Thread.currentThread().getName());
                        String s = Base64.encodeToString(bytes, Base64.NO_WRAP);
                        subscriber.onNext(s);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                ;
    }

}
