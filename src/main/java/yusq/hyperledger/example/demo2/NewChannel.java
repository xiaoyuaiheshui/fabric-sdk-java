package yusq.hyperledger.example.demo2;

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
    private static final String CONFIGTXLATOR_LOCATION ="http://192.168.190.128:7059/";

    public static void main(String[] args) throws Exception {
        HFClient client = CreateChannel.getHfClient();
        AppUser org1Admin = CreateChannel.getAdmin();
        Channel channel = newChannel(client,org1Admin);
    }

    static Channel newChannel(HFClient client, AppUser org1Admin) throws InvalidArgumentException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, TransactionException, ProposalException {

        client.setUserContext(org1Admin);
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050", CreateChannel.getOrdererProperties());
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://192.168.190.128:7051", CreateChannel.getPeerProperties());
        //获取tx文件
        ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(PATH + "\\basic-network\\config\\channel.tx"));
        Channel newChannel = client.newChannel("mychannel", orderer, channelConfiguration, client.getChannelConfigurationSignature(channelConfiguration, org1Admin));

        //添加peer节点
        newChannel.joinPeer(peer);
        newChannel.addOrderer(orderer);
        File sampleStoreFile = new File(PATH + "\\basic-network\\channel" + "/HFCSampletestchannel1.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleStore.saveChannel(newChannel);
        newChannel.initialize();

        return newChannel;
    }
}
