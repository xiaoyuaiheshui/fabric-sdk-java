package yusq.hyperledger.example.demo3;

import yusq.hyperledger.example.AppUser;
import yusq.hyperledger.example.demo.SampleStore;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;

import java.io.File;
import java.util.Collection;
import java.util.EnumSet;

import static yusq.hyperledger.example.demo.CreateChannel.getAdmin2;
import static yusq.hyperledger.example.demo.CreateChannel.getHfClient;

/**
 * @Author: yusq
 * @Date: 2019/2/20 0020
 */
public class queryPeer {

    public static void main(String[] args) throws Exception {
        AppUser org1Admin = getAdmin2();
        querypeer(org1Admin);
    }


    public static void querypeer(AppUser Admin) throws Exception {
        EnumSet<Peer.PeerRole> roles = EnumSet.complementOf(EnumSet.of(Peer.PeerRole.ENDORSING_PEER));
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
    public static Channel getChannel2(AppUser peerAdmin) throws Exception {

        HFClient client = getHfClient();
        client.setUserContext(peerAdmin);

        File sampleStoreFile = new File("E:\\Fabric\\Sample\\scratch\\hyperledger\\fabric-sdk-java-scratch\\basic-network\\channel" + "/HFCSampletestchannelupdate2.properties");
        SampleStore sampleStore = new SampleStore(sampleStoreFile);
        Channel channel = sampleStore.getChannel(client, "channel2");
        return channel;
    }

}
