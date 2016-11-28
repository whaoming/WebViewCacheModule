package com.wxxiaomi.ming.webviewcachemodule.version_1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.wxxiaomi.ming.webviewcachemodule.R;
import com.wxxiaomi.ming.webviewcachemodule.common.BaseWebActivity;
import com.wxxiaomi.ming.webviewcachemodule.common.PicTakeUtil;
import com.yancy.gallerypick.inter.IHandlerCallBack;

import java.util.List;

/**
 * Created by Mr.W on ${DATE}.
 * E-maiil：122627018@qq.com
 * github：https://github.com/122627018
 * 模拟从本地取得照片地址，并在webview中加载
 */
public class Version_1_Activity extends BaseWebActivity {
    private Button btn;
    private PicTakeUtil util;
    private String path = "/storage/sdcard0/demo.jpg";

    @Override
    protected int initViewAndReutrnWebViewId(Bundle savedInstanceState) {
        setContentView(R.layout.activity_version_1_);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                adapterH5();
                takePicture();
            }
        });
        return R.id.web_view;
    }

    private void takePicture() {
        util = new PicTakeUtil(this);
        util.takePicture(new IHandlerCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(List<String> photoList) {
                for(String item : photoList){
                    Log.i("wang","item:"+item);
                }
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

    private void adapterH5Data(List<String> imgPaths){
        String result = "";
        for(int i=0;i<imgPaths.size();i++){
            result += "<img src=\"file://"+imgPaths.get(i)+"\" height=\"70\" width=\"70\"/>";
        }
        Log.i("wang","result:"+result);
        mWebView.callHandler("adapter",result,null);
    }

    private void adapterH5(){
        String result = "";
        for(int i=0;i<20;i++){
            result += "<img src=\"file://"+path+"\" height=\"300\" width=\"300\"/>";
        }
        Log.i("wang","result:"+result);
        mWebView.callHandler("adapter",result,null);
    }

    @Override
    protected void initWebView() {
        mWebView.loadUrl("file:///android_asset/test1.html");
        mWebView.getSettings().setAllowFileAccess(true);
//        util = new PicTakeUtil(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWebView.removeAllViews();
        mWebView.destroy();
    }
}
