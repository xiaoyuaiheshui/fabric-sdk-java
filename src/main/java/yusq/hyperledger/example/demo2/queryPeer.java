package yusq.hyperledger.example.demo2;

import yusq.hyperledger.example.AppUser;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import yusq.hyperledger.example.demo.CreateChannel;

import java.util.Collection;
import java.util.EnumSet;

/**
 * @Author: yusq
 * @Date: 2019/2/20 0020
 */
public class queryPeer {

    public static void main(String[] args) throws Exception {
        AppUser org1Admin = CreateChannel.getAdmin2();
        querypeer(org1Admin);
    }


    public static void querypeer(AppUser Admin) throws Exception {
        EnumSet<Peer.PeerRole> roles = EnumSet.complementOf(EnumSet.of(Peer.PeerRole.ENDORSING_PEER));
        Channel channel = CreateChannel.getChannel(Admin);
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


}
