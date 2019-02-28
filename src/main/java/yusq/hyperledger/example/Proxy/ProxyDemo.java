package yusq.hyperledger.example.Proxy;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yusq
 * @Date: 2019/2/26 0026
 */
public class ProxyDemo implements Runnable {
    @Override
    public void run() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//设置连接超时时间
        builder.connectTimeout(1, TimeUnit.MINUTES);
//设置代理,需要替换
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("183.47.40.35", 8088));
        builder.proxy(proxy);
//cookie管理器
        CookieManager cookieManager = new CookieManager();
        OkHttpClient client = builder
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
        Request cookieRequest = new Request.Builder()
//                .headers()
                .url("https://www.cnblogs.com/dengjiali2015/p/4560542.html")
                .get()
                .build();
        Response execute = null;
        try {
            execute = client.newCall(cookieRequest).execute();
            System.out.println(execute.body().string());
            execute.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
