package yusq.hyperledger.example.demo;

import yusq.hyperledger.example.AppUser;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.UpdateChannelConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author: yusq
 * @Date: 2019/2/15 0015
 */
public class UpdateChannel {

    private static final String ORIGINAL_BATCH_TIMEOUT = "\"timeout\": \"5s\"";
    private static final String UPDATED_BATCH_TIMEOUT = "\"timeout\": \"15s\"";
    private static final String CONFIGTXLATOR_LOCATION ="http://192.168.190.128:7059/";
    private static final String PATH = System.getProperty("user.dir");
    public static void main(String[] args) throws Exception {
        HFClient client = CreateChannel.getHfClient();
        AppUser admin = CreateChannel.getAdmin();
//
        Path fpath=Paths.get(PATH+"/config2.JSON");
        File file = new File(PATH + "/config2.JSON");
        String input = FileUtils.readFileToString(file, "UTF-8");
        update(admin,client,input);
    }
    public static void update(AppUser peerAdmin, HFClient client,String config) throws Exception {

        Channel channel = CreateChannel.getChannel2(peerAdmin);
        channel.initialize();
        byte[] channelConfigurationBytes = channel.getChannelConfigurationBytes();
        HttpClient httpclient = HttpClients.createDefault();
        /*String responseAsString = configTxlatorDecode(httpclient, channelConfigurationBytes);
        //responseAsString is JSON but use just string operations for this test.
        if (!responseAsString.contains(ORIGINAL_BATCH_TIMEOUT)) {
            System.out.println(
                    (format("Did not find expected batch timeout '%s', in:%s", ORIGINAL_BATCH_TIMEOUT, responseAsString)));
        }*/


        //修改批处理超时
//        String updateString = responseAsString.replace(ORIGINAL_BATCH_TIMEOUT, UPDATED_BATCH_TIMEOUT);
        HttpPost httppost = new HttpPost(CONFIGTXLATOR_LOCATION + "/protolator/encode/common.Config");
        httppost.setEntity(new StringEntity(config));
        HttpResponse response = httpclient.execute(httppost);
        int statuscode = response.getStatusLine().getStatusCode();
        System.out.println(("Got %s status for encoding the new desired channel config bytes"+ statuscode));
        byte[] newConfigBytes = EntityUtils.toByteArray(response.getEntity());

        // 现在发送到configtxlator多部分表单帖子，其中包含原始配置字节，更新的配置字节和通道名称。
        httppost = new HttpPost(CONFIGTXLATOR_LOCATION + "/configtxlator/compute/update-from-configs");
        HttpEntity multipartEntity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("original", channelConfigurationBytes, ContentType.APPLICATION_OCTET_STREAM, "originalFakeFilename")
                .addBinaryBody("updated", newConfigBytes, ContentType.APPLICATION_OCTET_STREAM, "updatedFakeFilename")
                .addBinaryBody("channel", channel.getName().getBytes()).build();
        httppost.setEntity(multipartEntity);
        response = httpclient.execute(httppost);
        statuscode = response.getStatusLine().getStatusCode();
        System.out.println(("Got %s status for encoding the new desired channel config bytes"+ statuscode));
        byte[] updateBytes = EntityUtils.toByteArray(response.getEntity());

        UpdateChannelConfiguration updateChannelConfiguration = new UpdateChannelConfiguration(updateBytes);
        AppUser ordererAdmin = CreateChannel.getAdmin();
        client.setUserContext(ordererAdmin);
        channel.updateChannelConfiguration(updateChannelConfiguration, client.getUpdateChannelConfigurationSignature(updateChannelConfiguration, ordererAdmin));
        Thread.sleep(3000);
    }

    private static String configTxlatorDecode(HttpClient httpclient, byte[] channelConfigurationBytes) throws IOException {
        HttpPost httppost = new HttpPost("http://192.168.190.128:7059/" + "/protolator/decode/common.Config");
        httppost.setEntity(new ByteArrayEntity(channelConfigurationBytes));
        HttpResponse response = httpclient.execute(httppost);
        int statuscode = response.getStatusLine().getStatusCode();
        //  out("Got %s status for decoding current channel config bytes", statuscode);
        return EntityUtils.toString(response.getEntity());
    }

}
