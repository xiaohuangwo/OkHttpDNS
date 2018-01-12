package com.baidu.rim.guofeng007.okhttpdns;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient client;
    private OkHttpClient httpDnsclient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getClient();
    }

    public void onClick(View view){
        switch (view.getId()) {
            case R.id.buttonFireNetwork:
                Request request = new Request.Builder().url("http://www.baidu.com")
                        .build();
                getClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        final String str = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                break;
        }
    }
    private OkHttpClient getHTTPDnsClient() {
        if (httpDnsclient == null) {
            final File cacheDir = getExternalCacheDir();
            httpDnsclient = new OkHttpClient.Builder()
                    .addNetworkInterceptor(new Interceptor() {
                        @Override public Response intercept(Chain chain) throws IOException {
                            Response originalResponse = chain.proceed(chain.request());
                            return originalResponse.newBuilder()
                                    //在返回header中加入缓存消息
                                    //下次将不再发送请求
                                    .header("Cache-Control", "max-age=600").build();
                        }
                    })
                    //5MB的文件缓存
                    .cache(new Cache(new File(cacheDir, "httpdns"), 5 * 1024 * 1024))
                    .build();
        }
        return httpDnsclient;
    }

    private Dns HTTP_DNS =  new Dns(){
        @Override public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            //防御代码
            if (hostname == null) throw new UnknownHostException("hostname == null");
            //dnspod提供的dns服务
            HttpUrl httpUrl = new HttpUrl.Builder().scheme("http")
                    .host("119.29.29.29")
                    .addPathSegment("d")
                    .addQueryParameter("dn", hostname)
                    .build();
            Request dnsRequest = new Request.Builder().url(httpUrl).get().build();
            try {
                String s = getHTTPDnsClient().newCall(dnsRequest).execute().body().string();
                //避免服务器挂了却无法查询DNS
                // 原始代码如下，解析有误，仍然走的是系统 dns 查询，而不是返回的结果
//                if (!s.matches("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b")) {
//                    return Dns.SYSTEM.lookup(hostname);
//                }
                // 这里小心解析，出错，或者解析无结果，仍然走系统
                if(!TextUtils.isEmpty(s)){
                    LinkedList<InetAddress> inetAddresses = null;
                    try {
                        inetAddresses = IPUtils.strToInetAddress(hostname, s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(inetAddresses!=null && inetAddresses.size()>0){
                        return inetAddresses;
                    }
                }
                return Dns.SYSTEM.lookup(hostname);
            } catch (IOException e) {
                return Dns.SYSTEM.lookup(hostname);
            }
        }
    };

    //真正的调用客户端，供Retrofit与Picasso使用
    private OkHttpClient getClient() {
        if (client == null) {
            final File cacheDir = getExternalCacheDir();
            client = new OkHttpClient.Builder()
                    .cache(new Cache(new File(cacheDir, "okhttp"), 60 * 1024 * 1024))
                    //配置DNS查询实现
                    .dns(HTTP_DNS)
                    .build();
        }
        return client;
    }


}
