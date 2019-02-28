package yusq.hyperledger.example.Proxy;

import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * @Author: yusq
 * @Date: 2019/2/27 0027
 */
public class test extends Thread {
    private static int failure = 0;
    private static int success = 0;



    public static void main(String[] args) throws IOException {
        for (int j = 0; j < 20; j++) {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet("http://127.0.0.1:8080/api/get");
            HttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");
            JSONObject a = new JSONObject(result);
            String data = a.get("data").toString();
            JSONObject a2 = new JSONObject(data);
            String ip = a2.get("ip").toString();
            String port = a2.get("port").toString();
            execute(ip, port, result);
            System.out.println(result);
        }
        System.out.println("failure" + failure);
        System.out.println("success" + success);
    }

    private static void execute(String ip, String port, String result) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//设置连接超时时间
        builder.connectTimeout(1, TimeUnit.MINUTES);
//设置代理,需要替换
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, Integer.parseInt(port)));
        builder.proxy(proxy);
//cookie管理器
        CookieManager cookieManager = new CookieManager();
        OkHttpClient client = builder
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
        Request cookieRequest = new Request.Builder()
//                .headers()
                .url("https://myip.kkcha.com/")
                .get()
                .build();
        Response execute = null;
        try {
            execute = client.newCall(cookieRequest).execute();
            success = success + 1;
            execute.close();
        } catch (IOException e) {
            System.out.println("err" + e);
            System.out.println("Details" + result);
            failure = failure + 1;
        }

    }
}
