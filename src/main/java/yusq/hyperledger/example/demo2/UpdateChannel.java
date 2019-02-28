package yusq.hyperledger.example.demo2;

import yusq.hyperledger.example.AppUser;
import yusq.hyperledger.example.demo.FabricConfig;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.UpdateChannelConfiguration;
import yusq.hyperledger.example.demo.CreateChannel;

import java.io.File;

/**
 * @Author: yusq
 * @Date: 2019/2/20 0020
 */
public class UpdateChannel {
    private static final String PATH = System.getProperty("user.dir");
    private static final FabricConfig FABRIC_CONFIG = new FabricConfig();
    private static final String CONFIGTXLATOR_LOCATION ="http://192.168.190.128:7059/";

    public static void main(String[] args) throws Exception {
        HFClient client = CreateChannel.getHfClient();
        AppUser org1Admin = CreateChannel.getAdmin();

        File file = new File(PATH + "/config2.JSON");
        String input = FileUtils.readFileToString(file, "UTF-8");
        update(org1Admin,client,input);
    }

    public static void update(AppUser org1Admin, HFClient client, String config) throws Exception {

        Channel channel = CreateChannel.getChannel(org1Admin);
        channel.initialize();
        byte[] channelConfigurationBytes = channel.getChannelConfigurationBytes();
        HttpClient httpclient = HttpClients.createDefault();

        HttpPost httppost = new HttpPost(CONFIGTXLATOR_LOCATION + "/protolator/encode/common.Config");
        httppost.setEntity(new StringEntity(config));
        HttpResponse response = httpclient.execute(httppost);
        int statuscode = response.getStatusLine().getStatusCode();
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
        byte[] updateBytes = EntityUtils.toByteArray(response.getEntity());

        UpdateChannelConfiguration updateChannelConfiguration = new UpdateChannelConfiguration(updateBytes);
        AppUser ordererAdmin = CreateChannel.getAdmin();
        client.setUserContext(ordererAdmin);
        channel.updateChannelConfiguration(updateChannelConfiguration, client.getUpdateChannelConfigurationSignature(updateChannelConfiguration, ordererAdmin));
        Thread.sleep(3000);
    }
}
