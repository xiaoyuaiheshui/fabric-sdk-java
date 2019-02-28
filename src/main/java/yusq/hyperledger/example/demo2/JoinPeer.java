package yusq.hyperledger.example.demo2;

import yusq.hyperledger.example.AppUser;
import yusq.hyperledger.example.demo.SampleStore;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import yusq.hyperledger.example.demo.CreateChannel;

import java.io.File;

/**
 * @Author: yusq
 * @Date: 2019/2/20 0020
 */
public class JoinPeer {
    private static final String PATH = System.getProperty("user.dir");

    public static void main(String[] args) throws Exception {
        HFClient client = CreateChannel.getHfClient();
        newChannel2(client);
    }

    static Channel newChannel2(HFClient client) throws Exception {
        AppUser org2Admin = CreateChannel.getAdmin2();
        client.setUserContext(org2Admin);
        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050", CreateChannel.getOrdererProperties());
        Peer peer = client.newPeer("peer0.org2.example.com", "grpc://192.168.190.128:6051", CreateChannel.getPeer2Properties());
//        获取tx文件
        Channel newChannel = CreateChannel.getChannel(org2Admin);
        newChannel.joinPeer(peer);
        newChannel.addOrderer(orderer);
        File sampleStoreFile = new File(PATH + "\\basic-network\\channel" + "/HFCSampletestchannelupdate.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        sampleStore.saveChannel(newChannel);
        newChannel.initialize();
        //添加peer节点
        return newChannel;
    }
}
