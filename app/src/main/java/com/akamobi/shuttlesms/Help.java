package com.akamobi.shuttlesms;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class Help extends Activity{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                 
        WebView webview = new WebView(this);
        setContentView(webview);
        webview.loadUrl("http://shuttlesms.com/");
    }
}