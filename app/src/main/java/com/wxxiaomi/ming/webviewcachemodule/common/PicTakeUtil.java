package com.wxxiaomi.ming.webviewcachemodule.common;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.yancy.gallerypick.config.GalleryConfig;
import com.yancy.gallerypick.config.GalleryPick;
import com.yancy.gallerypick.inter.IHandlerCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mr.W on 2016/11/25.
 * E-maiil：122627018@qq.com
 * github：https://github.com/122627018
 * 从本地取照片的一个工具类
 */
public class PicTakeUtil {
    private Context context;
    private GalleryConfig galleryConfig;
    private List<String> path = new ArrayList<>();
    public PicTakeUtil(Context context){
        this.context = context;
        galleryConfig = new GalleryConfig.Builder()
                .imageLoader(new GlideImageLoader())    // ImageLoader 加载框架（必填）
                .pathList(path)                         // 记录已选的图片
                .multiSelect(false)                      // 是否多选   默认：false
                .multiSelect(false, 9)                   // 配置是否多选的同时 配置多选数量   默认：false ， 9
                .maxSize(9)                             // 配置多选时 的多选数量。    默认：9
                .crop(false)                             // 快捷开启裁剪功能，仅当单选 或直接开启相机时有效
                .crop(false, 1, 1, 500, 500)             // 配置裁剪功能的参数，   默认裁剪比例 1:1
                .isShowCamera(true)                     // 是否现实相机按钮  默认：false
                .filePath("/Gallery/Pictures")          // 图片存放路径
                .build();
        galleryConfig.getBuilder().isShowCamera(true).build();
    }

    public void takePicture(IHandlerCallBack iHandlerCallBack){
        galleryConfig.getBuilder().multiSelect(true).build();
        galleryConfig.getBuilder().crop(false).build();
        galleryConfig.getBuilder().iHandlerCallBack(iHandlerCallBack);
        openTakeWindow();
    }

    public void openTakeWindow(){
        galleryConfig.getBuilder().isOpenCamera(false).build();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(context, "请在 设置-应用管理 中开启此应用的储存授权。", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions((Activity)context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 8);
            }
        } else {
            GalleryPick.getInstance().setGalleryConfig(galleryConfig).open((Activity)context);

        }
    }
}
