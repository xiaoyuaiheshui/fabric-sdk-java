package yusq.hyperledger.example.demo4;

import yusq.hyperledger.example.AppUser;
import yusq.hyperledger.example.demo.FabricConfig;
import yusq.hyperledger.example.demo.SampleStore;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import yusq.hyperledger.example.demo.CreateChannel;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

/**
 * @Author: yusq
 * @Date: 2019/2/20 0020
 */
public class NewChannel {

    private static final String PATH = System.getProperty("user.dir");
    private static final FabricConfig FABRIC_CONFIG = new FabricConfig();

    public static void main(String[] args) throws Exception {
        HFClient client = CreateChannel.getHfClient();
        AppUser org3Admin = getAdmin();
        newChannel(client,org3Admin);
    }
    static Channel newChannel(HFClient client, AppUser org3Admin) throws InvalidArgumentException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, TransactionException, ProposalException {

        client.setUserContext(org3Admin);
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050", CreateChannel.getOrdererProperties());
        Peer peer = client.newPeer("peer0.org3.example.com", "grpc://192.168.190.128:6151", CreateChannel.getPeerProperties());
        //获取tx文件
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(PATH + "\\basic-network\\config\\channel3.tx"));
        Channel newChannel = client.newChannel("channel3", orderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, org3Admin));

        //添加peer节点
        newChannel.joinPeer(peer);
        newChannel.addOrderer(orderer);
        File sampleStoreFile = new File(PATH + "\\basic-network\\channel" + "/HFCSampletestchannel3.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleStore.saveChannel(newChannel);
        newChannel.initialize();

        return newChannel;
    }
    public static AppUser getAdmin() throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, IOException {
        File file = new File(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org3.example.com\\users\\Admin@org3.example.com\\msp\\keystore");
        File cert = new File(PATH + "\\basic-network\\crypto-config\\peerOrganizations\\org3.example.com\\users\\Admin@org3.example.com\\msp\\admincerts\\Admin@org3.example.com-cert.pem");
        AppUser peerAdmin = FABRIC_CONFIG.getMember("admin", "Org3", "Org3MSP", FabricConfig.findFileSk(file), cert);
        return peerAdmin;
    }
}
