package com.wxxiaomi.ming.webviewcachemodule.common.util;

import android.util.Log;


import java.util.List;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by 12262 on 2016/11/25.
 * 缓存的处理服务
 */

public class CacheEngine {
    private CacheEngine(){};
    private static CacheEngine INSTANCE;
    public static CacheEngine getInstance(){
        if(INSTANCE==null){
            synchronized (CacheEngine.class){
                INSTANCE = new CacheEngine();
            }
        }
        return INSTANCE;
    }

    /**
     * 获取本地的图片
     * 先从硬盘获取，如果硬盘获取不到
     * 再拿图片进行压缩，再存到硬盘缓存
     * 然后返回
     * @param path
     * @return
     */
    public Observable<byte[]> getLocalImg(final String path){
        Observable<byte[]> diskCache = DiskCache.getInstance().getDiskCache(path);
        Observable<byte[]> doCache = CompressUtil.compressImg(path, 100, 100)
                .flatMap(new Func1<byte[], Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(final byte[] bytes) {
                        return DiskCache.getInstance().toDiskCache(path, bytes);
                    }
                });
        return Observable.concat(diskCache,doCache)
                .first(new Func1<byte[], Boolean>() {
                    @Override
                    public Boolean call(byte[] bytes) {
                        return bytes != null;
                    }
                });
    }

    /**
     * 获取本地的图片
     * 先从硬盘获取，如果硬盘获取不到
     * 再拿图片进行压缩，再存到硬盘缓存
     * 然后返回
     * @param path
     * @return
     */
    public Observable<byte[]> getLocalImgInMain(final String path){
        Observable<byte[]> diskCache = DiskCache.getInstance().getDiskCacheInMain(path);
        return Observable.concat(diskCache,getDoCache(path))
                .first(new Func1<byte[], Boolean>() {
                    @Override
                    public Boolean call(byte[] bytes) {
                        return bytes != null;
                    }
                });
    }

    /**
     * 对图片做压缩并缓存的一个操作
     * @param path
     * @return
     */
    public Observable<byte[]> getDoCache(final String path){
        return   CompressUtil.compressImgInMain(path, 100, 100)
                .flatMap(new Func1<byte[], Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(final byte[] bytes) {
                        Log.i("wang","CacheEngine-getLocalImg-doCache.currentThread:"+Thread.currentThread().getName());
                        return DiskCache.getInstance().toDiskCacheInMain(path, bytes);
                    }
                });
    }

    /**
     * 从本地数据取一些图片，分发多个源发送
     * @param path
     * @return
     */
    public Observable<byte[]> getLocalImgsMany2(final List<String> path){
        return Observable.from(path)
                .flatMap(new Func1<String, Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(String s) {
                        return getLocalImg(s);
                    }
                });
    }

    /**
     * 从本地数据取一些图片，合并在一个源里面一起发送
     * @param path
     * @return
     */
//    public Observable<List<byte[]>> getLocalImgs(final List<String> path){
//        List<Observable<byte[]>> list = new ArrayList<>();
//        final List<byte[]> result = new ArrayList<>();
//        for(String item : path){
//            Observable<byte[]> localImg = getLocalImg(item);
//            list.add(localImg);
//        }
//        return Observable.zip(list, new FuncN<List<byte[]>>() {
//            @Override
//            public List<byte[]> call(Object... args) {
//                for(Object item : args){
//                    result.add((byte[])item);
//                }
//                return result;
//            }
//        });
//    }

    /**
     * 从本地数据取一些图片，分发多个源发送
     * @param path
     * @return
     */
//    public Observable<byte[]> getLocalImgsMany(final List<String> path){
//        Log.i("wang","CacheEngine-getLocalImgsMany.currentThread:"+Thread.currentThread().getName());
//        List<Observable<byte[]>> list = new ArrayList<>();
//        for(String item : path){
//            Observable<byte[]> localImg = getLocalImg(item);
//            list.add(localImg);
//        }
//
//       return Observable.merge(list)
//               .subscribeOn(Schedulers.newThread())
//               ;
//
//    }



}
