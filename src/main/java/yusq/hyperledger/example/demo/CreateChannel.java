package yusq.hyperledger.example.demo;

import yusq.hyperledger.example.AppUser;
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
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @Author: yusq
 * @Date: 2018/12/26 0026
 */
public class CreateChannel {
    private static final String PATH = System.getProperty("user.dir");
    private static final FabricConfig FABRIC_CONFIG = new FabricConfig();


    public static void main(String[] args) throws Exception {
        HFClient client = getHfClient();
        AppUser admin = getAdmin();
        AppUser Orderadmin = getOrderAdmin();
        AppUser admin1 =getAdmin2();
//        newChannel3(client,admin);
//        newChannel2(client,Orderadmin);
//        newChannel3(client,admin);
//        getConfig(admin);
//        getOrderAdmin();
//        getChannelConfig(admin, client);
//        System.out.println(updateString);
//        querypeer(admin);
       /* Orderer orderer = getOrderer(client,admin);
        Peer peer = getPeer(client, admin);
        getChannel1(client,admin,orderer,peer);*/
//        getChannel1(client, admin,peer);
        newChannel(client, admin);
//        newChannel2(client, admin2);
//        getChannel1(client,admin);
//            getPeer(client,admin);
        /* querypeer(admin);
        queryInstallChaincodes(client,admin);
        System.out.println(PATH);*/
    }

    public static Peer getPeer(HFClient client, AppUser admin) throws InvalidArgumentException {
        client.setUserContext(admin);
        Properties peerProperties = FABRIC_CONFIG.getPeerProperties("peer0.org1.example.com");
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://192.168.190.128:7051", peerProperties);
        return peer;
    }

    public static void getChannel1(HFClient client, AppUser admin) throws InvalidArgumentException, ProposalException, TransactionException {
        client.setUserContext(admin);
        Channel channel = client.newChannel("mychannel");
        System.out.println(channel.getOrderers());
    }

    public static Orderer getOrderer(HFClient client, AppUser admin) throws InvalidArgumentException {
        client.setUserContext(admin);
        Properties ordererProperties = FABRIC_CONFIG.getOrdererProperties("orderer.example.com");
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050", ordererProperties);
        return orderer;
    }

    static void queryInstallChaincodes(HFClient client, AppUser admin) throws InvalidArgumentException, ProposalException {
        client.setUserContext(admin);
        Properties peerProperties = FABRIC_CONFIG.getPeerProperties("peer0.org1.example.com");
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://192.168.190.128:7051", peerProperties);
        List<Query.ChaincodeInfo> list = client.queryInstalledChaincodes(peer);
        for (Query.ChaincodeInfo chaincodeInfo : list) {
            System.out.println("->>>>>>>>>>>>>>>>>>>>");
            System.out.println(chaincodeInfo.getName());
            System.out.println(chaincodeInfo.getPath());
        }
    }

    /**
     * 创建通道
     *
     * @param client
     * @return
     * @throws InvalidArgumentException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     * @throws TransactionException
     */
    static Channel newChannel(HFClient client, AppUser peerAdmin) throws InvalidArgumentException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, TransactionException, ProposalException {

        client.setUserContext(peerAdmin);
        Properties ordererProperties = FABRIC_CONFIG.getOrdererProperties("orderer.example.com");
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050", getOrdererProperties());

        Properties peerProperties = FABRIC_CONFIG.getPeerProperties("peer0.org1.example.com");
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://192.168.190.128:7051", getPeerProperties());
        //获取tx文件
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(PATH + "\\basic-network\\config\\channel.tx"));
        Channel newChannel = client.newChannel("mychannel", orderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, peerAdmin));
        //添加peer节点
        newChannel.joinPeer(peer);
        newChannel.addOrderer(orderer);
        File sampleStoreFile = new File(PATH + "\\basic-network\\channel" + "/HFCSampletestchannel1.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleStore.saveChannel(newChannel);
        newChannel.initialize();
        return newChannel;
    }
    static Channel newChannel3(HFClient client, AppUser peerAdmin) throws InvalidArgumentException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, TransactionException, ProposalException {

        client.setUserContext(peerAdmin);
        Properties ordererProperties = FABRIC_CONFIG.getOrdererProperties("orderer.example.com");
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050", getOrdererProperties());

        Properties peerProperties = FABRIC_CONFIG.getPeerProperties("peer0.org1.example.com");
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://192.168.190.128:7051", getPeerProperties());
        Peer peer2 = client.newPeer("peer0.org2.example.com", "grpc://192.168.190.128:6051", getPeer2Properties());
        //获取tx文件
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(PATH + "\\basic-network\\config\\channel2.tx"));
        Channel newChannel = client.newChannel("channel2", orderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, peerAdmin));
        //添加peer节点
        newChannel.joinPeer(peer);
        newChannel.addOrderer(orderer);
        File sampleStoreFile = new File(PATH + "\\basic-network\\channel" + "/HFCSampletestchannel2.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleStore.saveChannel(newChannel);
        newChannel.initialize();
        return newChannel;
    }
    static Channel newChannel2(HFClient client, AppUser peerAdmin) throws Exception {
        client.setUserContext(getAdmin());
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050", getOrdererProperties());
        Peer peer = client.newPeer("peer0.org2.example.com", "grpc://192.168.190.128:6051", getPeer2Properties());
//        获取tx文件
        Channel newChannel = getChannel2(getAdmin2());
        newChannel.addOrderer(orderer);
        newChannel.joinPeer(peer);
        newChannel.initialize();
        //添加peer节点
        return newChannel;
    }

    public static AppUser getAdmin() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        File file = new File(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org1.example.com\\users\\Admin@org1.example.com\\msp\\keystore");
        File cert = new File(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org1.example.com\\users\\Admin@org1.example.com\\msp\\admincerts\\Admin@org1.example.com-cert.pem");
        AppUser peerAdmin = FABRIC_CONFIG.getMember("admin", "Org1", "Org1MSP", FabricConfig.findFileSk(file), cert);
        return peerAdmin;
    }
    public static AppUser getAdmin2() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        File file = new File(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org2.example.com\\users\\Admin@org2.example.com\\msp\\keystore");
        File cert = new File(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org2.example.com\\users\\Admin@org2.example.com\\msp\\admincerts\\Admin@org2.example.com-cert.pem");
        AppUser peerAdmin = FABRIC_CONFIG.getMember("admin", "Org2", "Org2MSP", FabricConfig.findFileSk(file), cert);
        return peerAdmin;
    }
    public static AppUser getOrderAdmin() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {

        File file = new File(PATH + "\\basic-network\\crypto-config\\ordererOrganizations\\example.com\\users\\Admin@example.com\\msp\\keystore");
        File cert = new File(PATH + "\\basic-network\\crypto-config\\ordererOrganizations\\example.com\\users\\Admin@example.com\\msp\\signcerts\\Admin@example.com-cert.pem");
        AppUser orderAdmin = FABRIC_CONFIG.getMember("OrderAdmin", "", "OrdererMSP", FabricConfig.findFileSk(file), cert);
        return orderAdmin;
    }

    /**
     * 获取Channel
     *
     * @throws Exception
     */
    public static Channel getChannel(AppUser peerAdmin) throws Exception {

        HFClient client = getHfClient();
        client.setUserContext(peerAdmin);

        File sampleStoreFile = new File("E:\\Fabric\\Sample\\scratch\\hyperledger\\fabric-sdk-java-scratch\\basic-network\\channel" + "/HFCSampletestchannelupdate.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        Channel channel = sampleStore.getChannel(client, "mychannel");
        return channel;
    }
    public static Channel getChannel2(AppUser peerAdmin) throws Exception {

        HFClient client = getHfClient();
        client.setUserContext(peerAdmin);

        File sampleStoreFile = new File("E:\\Fabric\\Sample\\scratch\\hyperledger\\fabric-sdk-java-scratch\\basic-network\\channel" + "/HFCSampletestchannel22.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        Channel channel = sampleStore.getChannel(client, "channel2");
        return channel;
    }

    public static void getConfig(AppUser peerAdmin) throws Exception {
        Channel channel = getChannel2(peerAdmin);
        channel.initialize();
        byte[] channelConfBytes = channel.getChannelConfigurationBytes();
        System.out.println("channel:"+channelConfBytes);
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://192.168.190.128:7059/protolator/decode/common.Config");
        httpPost.setEntity(new ByteArrayEntity(channelConfBytes));
        HttpResponse response = httpClient.execute(httpPost);
        int statuscode = response.getStatusLine().getStatusCode();
        String res = EntityUtils.toString(response.getEntity());
        Path fpath=Paths.get(PATH+"/config3.JSON");
        //创建文件
        if(!Files.exists(fpath)) {
            try {
                Files.createFile(fpath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            BufferedWriter bfw=Files.newBufferedWriter(fpath);
            bfw.write(res);
            bfw.flush();
            bfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(res);
    }

    public static void getChannelConfig(AppUser peerAdmin, HFClient client) throws Exception {
        Channel channel = getChannel(peerAdmin);
        channel.initialize();
        byte[] channelConfBytes = channel.getChannelConfigurationBytes();

        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://192.168.190.128:7059/protolator/decode/common.Config");
        httpPost.setEntity(new ByteArrayEntity(channelConfBytes));
        HttpResponse response = httpClient.execute(httpPost);
        int statuscode = response.getStatusLine().getStatusCode();
        String res = EntityUtils.toString(response.getEntity());

        String timout = "\"timeout\": \"2s\"";
        String timout2 = "\"timeout\": \"5s\"";
        String us = res.replace(timout, timout2);
        httpPost = new HttpPost("http://192.168.190.128:7059/protolator/decode/common.Config");
        httpPost.setEntity(new StringEntity(us));
        httpPost.setEntity(new ByteArrayEntity(channelConfBytes));
        response = httpClient.execute(httpPost);
        statuscode = response.getStatusLine().getStatusCode();
        byte[] newConfigBytes = EntityUtils.toByteArray(response.getEntity());

        httpPost = new HttpPost("http://192.168.190.128:7059/protolator/decode/common.Config");
        HttpEntity multipartEntity = MultipartEntityBuilder.create()
                .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                .addBinaryBody("original", channelConfBytes, ContentType.APPLICATION_OCTET_STREAM, "originalFakeFilename")
                .addBinaryBody("updated", newConfigBytes, ContentType.APPLICATION_OCTET_STREAM, "updatedFakeFilename")
                .addBinaryBody("mychannel", channel.getName().getBytes()).build();
        httpPost.setEntity(multipartEntity);
        httpPost.setEntity(new ByteArrayEntity(channelConfBytes));
        response = httpClient.execute(httpPost);
        statuscode = response.getStatusLine().getStatusCode();
        byte[] updateBytes = EntityUtils.toByteArray(response.getEntity());

        AppUser orderAdmin = getOrderAdmin();
        client.setUserContext(orderAdmin);
        UpdateChannelConfiguration updateChannelConfiguration = new UpdateChannelConfiguration(updateBytes);
        channel.updateChannelConfiguration(updateChannelConfiguration, client.getUpdateChannelConfigurationSignature(updateChannelConfiguration, peerAdmin));
        client.setUserContext(peerAdmin);
    }

    public static void querypeer(AppUser Admin) throws Exception {
//        EnumSet<Peer.PeerRole> roles = EnumSet.complementOf(EnumSet.of(Peer.PeerRole.ENDORSING_PEER));
        Channel channel = getChannel2(Admin);
        Collection<Peer> peers = channel.getPeers();
        for (Peer peer : peers) {
            System.out.println("peer" + peer.getName());
        }
        System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>");
        Collection<Orderer> orderers = channel.getOrderers();
        for (Orderer orderer : orderers) {
            System.out.println("orderer" + orderer.getName());
        }
    }

    public static HFClient getHfClient() throws Exception {
        // initialize default cryptosuite
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }

    public static Properties getPeerProperties() {

        Properties ret = new Properties();
        File cert = Paths.get(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org1.example.com\\peers\\peer0.org1.example.com\\" + "tls/server.crt").toFile();

        File clientCert = Paths.get(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org1.example.com\\users\\Admin@org1.example.com\\tls\\" + "client.crt").toFile();
        File clientKey = Paths.get(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org1.example.com\\users\\Admin@org1.example.com\\tls\\" + "client.key").toFile();

        ret.setProperty("clientCertFile", clientCert.getAbsolutePath());
        ret.setProperty("clientKeyFile", clientKey.getAbsolutePath());
        ret.setProperty("pemFile", cert.getAbsolutePath());
        ret.setProperty("hostnameOverride", "peer0.org1.example.com");
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");

        return ret;
    }
    public static Properties getPeer2Properties() {

        Properties ret = new Properties();
        File cert = Paths.get(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org2.example.com\\peers\\peer0.org2.example.com\\" + "tls/server.crt").toFile();

        File clientCert = Paths.get(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org2.example.com\\users\\Admin@org2.example.com\\tls\\" + "client.crt").toFile();
        File clientKey = Paths.get(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org2.example.com\\users\\Admin@org2.example.com\\tls\\" + "client.key").toFile();

        ret.setProperty("clientCertFile", clientCert.getAbsolutePath());
        ret.setProperty("clientKeyFile", clientKey.getAbsolutePath());
        ret.setProperty("pemFile", cert.getAbsolutePath());
        ret.setProperty("hostnameOverride", "peer0.org2.example.com");
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");

        return ret;
    }
    public static Properties getOrdererProperties() {

        Properties ret = new Properties();

        File cert = Paths.get(PATH + "\\basic-network\\crypto-config\\ordererOrganizations\\example.com\\orderers\\orderer.example.com\\" + "tls/server.crt").toFile();

        File clientCert = Paths.get(PATH + "\\basic-network\\crypto-config\\ordererOrganizations\\example.com\\users\\Admin@example.com\\tls\\" + "client.crt").toFile();
        File clientKey = Paths.get(PATH + "\\basic-network\\crypto-config\\ordererOrganizations\\example.com\\users\\Admin@example.com\\tls\\" + "client.key").toFile();
        ret.setProperty("clientCertFile", clientCert.getAbsolutePath());
        ret.setProperty("clientKeyFile", clientKey.getAbsolutePath());
        ret.setProperty("hostnameOverride", "orderer.example.com");
        ret.setProperty("pemFile", cert.getAbsolutePath());
        ret.setProperty("sslProvider", "openSSL");
        ret.setProperty("negotiationType", "TLS");
        return ret;
    }
}
