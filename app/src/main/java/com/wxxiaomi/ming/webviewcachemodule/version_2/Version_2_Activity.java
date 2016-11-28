package com.wxxiaomi.ming.webviewcachemodule.version_2;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.wxxiaomi.ming.webviewcachemodule.R;
import com.wxxiaomi.ming.webviewcachemodule.common.BaseWebActivity;
import com.wxxiaomi.ming.webviewcachemodule.common.PicTakeUtil;
import com.yancy.gallerypick.inter.IHandlerCallBack;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Mr.W on ${DATE}.
 * E-maiil：122627018@qq.com
 * github：https://github.com/122627018
 * 模拟从本地取得照片地址，并在webview中加载
 */
public class Version_2_Activity extends BaseWebActivity {

    private Button btn;
    private PicTakeUtil util;
    private TextView tv;
    private int size = 0;


    @Override
    protected int initViewAndReutrnWebViewId(Bundle savedInstanceState) {
        setContentView(R.layout.activity_version_2_);
        tv = (TextView) findViewById(R.id.tv);
        tv.setText("建议不要选择太多相片");
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });
        return R.id.web_view;
    }

    private void takePicture() {
        tv.setText("正在编码");
        util = new PicTakeUtil(this);
        util.takePicture(new IHandlerCallBack() {
            @Override
            public void onStart() {
            }
            @Override
            public void onSuccess(List<String> photoList) {
                adapterH5Data(photoList);
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

    /**
     * 在这里必须使用多线程，因为base64对图片进行编码非常耗时，
     * 所以必须在子线程并且多线程处理每张图片的编码
     * RxJava的线程切换(这里暂时还没有使用多线程)
     * @param imgPaths
     */
    private void adapterH5Data(final List<String> imgPaths) {
        Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                for (int i = 0; i < imgPaths.size(); i++) {
                    String result = "";
                    String item = imgPaths.get(i);
                    Log.i("wang","item:"+item);
                    String s = Base64.encodeToString(getBytes(item), Base64.NO_WRAP);
                    result += "<img src=\"data:image/png;base64," + s + "\" height=\"70\" width=\"70\"/>";
                    subscriber.onNext(result);
                }
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                size++;
                tv.setText("编码完成:"+size+"张");
                mWebView.callHandler("adapter", s, null);
            }
        })
        ;
    }


    @Override
    protected void initWebView() {
        mWebView.loadUrl("file:///android_asset/test1.html");
        mWebView.getSettings().setAllowFileAccess(true);

    }

    /**
     * 获得指定文件的byte数组
     */
    private byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
