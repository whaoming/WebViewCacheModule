function connectWebViewJavascriptBridge(callback) {
    if (window.WebViewJavascriptBridge) {
        callback(WebViewJavascriptBridge)
    } else {
        document.addEventListener(
            'WebViewJavascriptBridgeReady'
            , function() {
                callback(WebViewJavascriptBridge)
            },
            false
        );
    }
}

function registerBridge(MyInitCallback,registerCallBack){
    connectWebViewJavascriptBridge(function(bridge){
        bridge.init(MyInitCallback);
        registerCallBack(bridge);

    });
}

function showLog(data){
    window.WebViewJavascriptBridge.callHandler(
        'showLog'
        , data
        , null
    );
}

