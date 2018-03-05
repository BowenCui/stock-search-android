package com.bowen.hw9real;

//import android.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

/**
 * Created by Bowen Cui on 11/24/2017.
 */

public class FragmentTwo extends Fragment{
    WebView webView;
    SharedPreferences preference;
    String inputSymbol;
    TextView errorText;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_two,container,false);
        //errorText = (TextView)rootView.findViewById(R.id.errorTextView2);
        //errorText.setVisibility(View.GONE);

        webView =(WebView) rootView.findViewById(R.id.historicalChart);
        webView.setVisibility(View.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("https://")||url.startsWith("http://"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new WebViewInterfaceTwo(),"android");
        webView.loadUrl("file:///android_asset/highstock.html");

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    public class WebViewInterfaceTwo {
        @JavascriptInterface
        public String getName(){
            preference = getActivity().getSharedPreferences("MySavedData", Context.MODE_PRIVATE);
            inputSymbol = preference.getString("symbolName","Not found");
            return inputSymbol;
        }
        @JavascriptInterface
        public void showError(){

                   // errorText.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);


        }
    }
}
