package yusq.hyperledger.example.Proxy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author: yusq
 * @Date: 2019/2/27 0027
 */
public class ThreadTest {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1; i++) {
            executorService.execute(new ProxyDemo());
        }
    }
}
