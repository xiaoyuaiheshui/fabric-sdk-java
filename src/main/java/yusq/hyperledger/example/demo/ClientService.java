package yusq.hyperledger.example.demo;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Author: yusq
 * @Date: 2019/1/15 0015
 *
 */
public class ClientService {

    /**
     * 创建一个已经存在的通道对象的实例
     *
     * @param name
     * @return
     * @throws Exception
     */
    public Channel newChannel(String name) throws Exception {
        HFClient client = getHfClient();
        return client.newChannel(name);
    }

    /**
     * 创建通道，并返回通道实例
     *
     * @param client
     * @param name                           通道名称
     * @param orderer                        Orderer对象实例
     * @param channelConfiguration           通道的配置文件
     * @param channelConfigurationSignatures 通道的配置文件的签名
     * @return
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    public Channel newChannel(HFClient client, String name, Orderer orderer, ChannelConfiguration channelConfiguration, byte[]... channelConfigurationSignatures) throws InvalidArgumentException, TransactionException {
        return client.newChannel(name, orderer, channelConfiguration, channelConfigurationSignatures);
    }

    /**
     * 获取一个通道对象的实例
     *
     * @param name
     * @return
     * @throws Exception
     */
    public Channel getChannel(HFClient client, String name) throws Exception {
        return client.getChannel(name);
    }

    /**
     * 查询peer节点加入的通道名称
     *
     * @param client
     * @param peer
     * @return
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public List<String> queryChannels(HFClient client, Peer peer) throws ProposalException, InvalidArgumentException {
        Set<String> channels = client.queryChannels(peer);
        return new ArrayList<>(channels);
    }

    static HFClient getHfClient() throws Exception {
        // 初始化默认的cryptosuite
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // 设置客户端
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }
}
