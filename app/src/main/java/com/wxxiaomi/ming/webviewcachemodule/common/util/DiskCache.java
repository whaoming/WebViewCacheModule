package com.wxxiaomi.ming.webviewcachemodule.common.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;
import com.wxxiaomi.ming.webviewcachemodule.TAG;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by 12262 on 2016/11/24.
 * DiskLruCache
 */
public class DiskCache {
    private Context context;
    DiskLruCache mDiskLruCache = null;
    public static DiskCache INSTANCE;

    private DiskCache() {
    }

    public static DiskCache getInstance() {
        if (INSTANCE == null) {
            synchronized (DiskCache.class) {
                INSTANCE = new DiskCache();
            }
        }
        return INSTANCE;
    }


    public void open(Context context) {
        try {
            this.context = context;
            File cacheDir = getDiskCacheDir(context, "bitmap");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            //第一个参数指定的是数据的缓存地址，
            // 第二个参数指定当前应用程序的版本号，
            // 第三个参数指定同一个key可以对应多少个缓存文件，基本都是传1，
            // 第四个参数指定最多可以缓存多少字节的数据。
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Observable<byte[]> toDiskCache(final String path, final byte[] obj) {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                String key = hashKeyForDisk(path);

                BufferedInputStream in = null;
                BufferedOutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot1 = mDiskLruCache.get(key);
                    if (snapshot1 == null) {
                        Log.i(TAG.TAG, "将数据存入缓存");
                        DiskLruCache.Editor editor = mDiskLruCache.edit(key);

                        in = new BufferedInputStream(new ByteArrayInputStream(obj), 8 * 1024);
                        OutputStream outputStream = editor.newOutputStream(0);
                        out = new BufferedOutputStream(outputStream, 8 * 1024);
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        editor.commit();
                    } else {
                        Log.i(TAG.TAG, "数据已存在缓存中，不需要再次插入");
                    }
                    subscriber.onNext(obj);
                    subscriber.onCompleted();

                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                ;
    }


    public Observable<byte[]> getDiskCache(final String url) {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                try {
                    Log.i(TAG.TAG, "从硬盘取数据的线程.currentThread:" + Thread.currentThread().getName());
                    String key = hashKeyForDisk(url);
                    DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
                    if (snapShot != null) {
                        Log.i(TAG.TAG, "从硬盘取到缓存");
                        InputStream is = snapShot.getInputStream(0);
                        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                        byte[] buff = new byte[100];
                        int rc = 0;
                        while ((rc = is.read(buff, 0, 100)) > 0) {
                            swapStream.write(buff, 0, rc);
                        }
                        byte[] in2b = swapStream.toByteArray();
                        subscriber.onNext(in2b);
                    } else {
                        subscriber.onNext(null);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(null);
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                ;
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }


    /**
     * 获取缓存位置
     *
     * @param context
     * @param uniqueName
     * @return
     */
    public File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 用来将字符串进行MD5编码
     *
     * @param key
     * @return
     */
    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public Observable<byte[]> getDiskCacheInMain(final String path) {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                try {
                    Log.i("cache", "从硬盘取数据的线程.currentThread:" + Thread.currentThread().getName());
                    String key = hashKeyForDisk(path);
                    DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
                    if (snapShot != null) {
                        InputStream is = snapShot.getInputStream(0);
                        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                        byte[] buff = new byte[100];
                        int rc = 0;
                        while ((rc = is.read(buff, 0, 100)) > 0) {
                            swapStream.write(buff, 0, rc);
                        }
                        byte[] in2b = swapStream.toByteArray();
                        subscriber.onNext(in2b);
                    } else {
                        subscriber.onNext(null);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(null);
                }
            }
        });
    }

    public Observable<byte[]> toDiskCacheInMain(final String path, final byte[] obj) {
        return Observable.create(new Observable.OnSubscribe<byte[]>() {
            @Override
            public void call(Subscriber<? super byte[]> subscriber) {
                String key = hashKeyForDisk(path);

                BufferedInputStream in = null;
                BufferedOutputStream out = null;
                try {
                    DiskLruCache.Snapshot snapshot1 = mDiskLruCache.get(key);
                    if (snapshot1 == null) {
                        Log.i("wang", "存入缓存");
                        DiskLruCache.Editor editor = mDiskLruCache.edit(key);

                        in = new BufferedInputStream(new ByteArrayInputStream(obj), 8 * 1024);
                        OutputStream outputStream = editor.newOutputStream(0);
                        out = new BufferedOutputStream(outputStream, 8 * 1024);
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        editor.commit();
                    } else {
                        Log.i("wang", "已经存在，不用存入缓存了");
                    }
                    subscriber.onNext(obj);
                    subscriber.onCompleted();

                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        })
                ;
    }
}
