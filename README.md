# 前言
先讲讲为什么会有这篇blog，话说前几天做个模块，要求是这样的：
做一个webview的页面，功能类似于微信发朋友圈一样，要求能上传本地图片到webview中进行展示，并按用户喜好添加和删除，当用户点击发布的时候，将这些图片上传至阿里云oss，收到oss响应后封装页面信息提交给服务器

# 技术要点

 1. webview与native的交互
 2. webview显示客户端本地图片的方式
 3. webview加载大量图片的问题(优化方案)
 4. 自定义关于webview的缓存系统

# webview与native的交互
关于交互其实网上有很多文章，在之前我也写过一篇关于webview与native交互方案的blog：[ Android混合开发的入门和方案](http://blog.csdn.net/qq122627018/article/details/52207600)
因此在这个demo中我采用的也是JsBridge的方式来让webview与native进行通信，所以主要提一下其中一些坑：

 1. 使用JsBridge的方式，在子线程是无法发送消息给WebView的
 2. 如果webview要加载本地文件，必须设置mWebView.getSettings().setAllowFileAccess(true);
 3. 如果html文件存在于服务器中，就算你按照第二点设置了，那webview也无法读取本地文件，会报not allowed to load local resource(解决办法后面我会给出)，这个坑异常深！！！
 4. 使用base64编码来让webview中img便签加载编码后的图片的异常(大坑)

# webview显示客户端本地图片的方式
## 直接设置
拿到本地文件的路径，然后拼装<img>便签，设置其src属性为"file://"+路径，webview就会自动去找此地址("file://"是属性webview的一种协议，就像我们的http协议的道理)

```
	//imgPaths是我们选择的那些图片的本地路径
    private void adapterH5Data(List<String> imgPaths){
        String result = "";
        //拼装标签
        for(int i=0;i<imgPaths.size();i++){
            result += "<img src=\"file://"+imgPaths.get(i)+"\" height=\"70\" width=\"70\"/>";
        }
		//发送给webview容器
        mWebView.callHandler("adapter",result,null);
    }
```
那么在我们的html文件中，有一个这样的方法：

```
	function adapter(pars){
			//直接把native拼装好的html添加到id为info_img的便签里面
		    $("#info_img").prepend(pars);
	}
```

## Base64编码
base64编码大家应该都有接触过，还记得宝宝当年刚撸项目的时候，图片上传就是利用app把图片转化为base64，然后把这段字符串发送给服务器接收再进行解码，差点把老师气吐血。
那么这个方案，就是把目标路径的文件，通过base64编码编写成字符串，然后设置到img便签的src属性，这样img便可以显示出图片。

```
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
            public void call(String result) {
                size++;
                tv.setText("编码完成:"+size+"张");
                mWebView.callHandler("adapter", result, null);
            }
        })
        ;
    }
```

## 方案的对比
图一为第一种方案，图二为第二种方案
![这里写图片描述](http://img.blog.csdn.net/20161129002425992)       ![这里写图片描述](http://img.blog.csdn.net/20161129002439915)

 1. 其实还有通过shouldInterceptRequest方法来显示图片，在这里其实跟第一种方案的原理一样(放到后面讲)
 2. 总体来说第一种方案的速度是比较快的，但是第一种方案有一个缺点就是如果是服务器的html的文件，是无法读取本地文件的
 3. 上面写的第二种方案只是一个入门级的，对比第一种方案唯一的优点是就算是非本地的文件也可以读取，但是总体速度非常慢

# 优化
很明显，上面的俩种方案都只是一个入门级的方案，根本走不上台面，缺点：

 - 本地图片没有经过压缩，直接放在webview中，如果图片比较多，会导致oom
 - 由上面的截图也可以看出从选完照片到webview显示，速度还是非常慢的，卡顿的感觉非常明显
 - 无法适应我们的需求，有时候html是在服务器上面的，有时候是存在本地的，无法做到统一处理
那么下面介绍我的第三种方案

## 多线程工作+压缩+缓存+base64编码
ps:本来压缩想单独做个demo出来的，后面发现压缩模块如果没有缓存模块一起合作的化，每次加载图片都要进行压缩，非常恐怖哈哈！
这里讲一下主要思路：

 - 每当我们拿到本地图片的路径之后，把这个路径md5之后当作key，检查缓存中是否存在：
 - 不存在：调用压缩模块对次路径的图片进行压缩，压缩完成再存入缓存中，再返回压缩后的文件的byte数组
 - 存在：直接返回

在这里我的缓存系统是使用RxJAva+DiskLruCache实现的：

```
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
        //存入缓存的操作,先压缩，再缓存，CompressUtil是图片压缩工具类
        Observable<byte[]> doCache = CompressUtil.compressImg(path, 100, 100)
                .flatMap(new Func1<byte[], Observable<byte[]>>() {
                    @Override
                    public Observable<byte[]> call(final byte[] bytes) {
                        return DiskCache.getInstance().toDiskCache(path, bytes);
                    }
                });
        //使用concat操作符来实现先调用diskCache,再调用doCache
        return Observable.concat(diskCache,doCache)
                .first(new Func1<byte[], Boolean>() {
                    @Override
                    public Boolean call(byte[] bytes) {
                        return bytes != null;
                    }
                });
    }
```
上面是缓存系统的核心代码，避免太多代码太混乱。其中关于压缩模块的代码，DiskLruCache工具类的代码可以直接看demo。
拿到本地图片路径之后的操作:

```
     private void adapter(List<String> photoList) {
        CacheEngine.getInstance().getLocalImgsMany2(photoList)
                .flatMap((Fun1) (bytes) -> { return byte2Base64(bytes); })
                .ObserverOn(AndroidSchedulers.mainThread())
                .subscribe((Action1)(result) -> { sendToH5(result) });
```

然后就是关于多线程的实现，使用RxJava一个最大的亮点就是线程切换，在发出每个源数据的时候，只需要调用subscribeOn(Schedulers.newThread())即可，如果要看具体实现请移步demo，下面是效果：
![这里写图片描述](http://img.blog.csdn.net/20161126162145526)
这个方案大体的思想就是这样的，总体的速度提升了超级多(后面有截图对比),下面看看第四种方案

## 压缩+缓存+流
从上面第二种方案可以看出，base64编码多么消耗资源(很慢)，只能说base64能绕过尽量绕过，我们都知道，webview在访问每一个连接的时候，都必须要经过shouldInterceptRequest这个方法：

```
	/**
     * 自定义的WebViewClient
     */
    protected class MyWebViewClient extends BridgeWebViewClient {
        public MyWebViewClient(BridgeWebView webView) {
            super(webView);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return super.shouldInterceptRequest(view, request);
        }
    }
```
WebResourceResponse就是返回给webview的资源，然后在WebResourceResponse 有这样一个构造函数：

```
public WebResourceResponse(String mimeType, String encoding, InputStream data) {
        throw new RuntimeException("Stub!");
    }
```
通过传入一个流InputStream来实现发送数据流给webview，那么我现在的思路是这样的：  

 - 取得本地图片路径之后，直接跟第一种方案一样生成img便签，例如：
`<img src=\"file://sdcard0/picture/xxx.jpg\" height=\"70\" width=\"70\"/>`
 - 然后在webview加载到此标签的时候，就会被shouldInterceptRequest方法拦截
 - 那么此时我们要解析含有"file://"的url，解析本地路径地址
 - 调用我们的缓存系统，传入此path, (缓存+压缩)系统返回byte[],解析成inputStream
 - 构造WebResourceResponse返回

那么核心的代码就是：  
```
//自定义的WebViewClient,重载shouldInterceptRequest方法
class MyWebViewClient extends BridgeWebViewClient {
        public MyWebViewClient(BridgeWebView webView) {
            super(webView);
        }
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            String key = "http://localhost";
            final WebResourceResponse[] response = {null};
            if(url.contains(key)){
	            //拿到文件地址
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
```
ps:细心的朋友可能发现了的key是"http://localhost"而不是"file:///"，这是因为如果是在服务器的html文件后者会被webview主动拦截了，不会经过shouldInterceptRequest方法，所以我们假装成http协议就行了(这是我暂时的解决办法，如果有更好的可以提出来哈)
## 方案的对比
(图一为第三种方案。图四为第四种方案)
![这里写图片描述](http://img.blog.csdn.net/20161129085442968)    ![这里写图片描述](http://img.blog.csdn.net/20161129085718781)

# 总结
总体来说个人比较喜欢第四种解决方式，因为搭配缓存系统不仅可以做到本地图片的压缩+缓存，还可以做到网络图片的缓存，只需要通过前缀名来判断是哪种图片即可，而且shouldInterceptRequest方法默认是在io线程工作的，就算你的图片有多大都不会阻塞主线程。另外有一个点要说的就是，上面我的缓存系统只用到了硬盘缓存，其实还可以再加一层内存缓存，那样效果就真的完美了(不过因为这种情景下图片的再利用率不高，所以没做内存缓存，如果是网络图片的加载的话，就很有必要使用内存缓存了)  
文章中的代码(start哦谢谢)：[github地址](https://github.com/122627018/WebViewCacheModule)

 - 

