package yusq.hyperledger.example;

import org.apache.log4j.Logger;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;


public class HFJavaSDKBasicExample {

    private static final Logger log = Logger.getLogger(HFJavaSDKBasicExample.class);


    public static void main(String[] args) throws Exception {
        // 创建fabric-ca客户端
        HFCAClient caClient = getHfCaClient("http://192.168.190.128:7054", null);

        //注册或加载管理员
        AppUser admin = getAdmin(caClient);
        log.info(admin);

        // 注册并注册新用户
        AppUser appUser = getUser(caClient, admin, "hfuser");
        log.info(appUser);

        // 获取HFC客户端实例
        HFClient client = getHfClient();
        // 设置用户上下文
        client.setUserContext(admin);

        // 使用客户端获取HFC频道
//        Channel channel = getChannel(client);
//        log.info("Channel: " + channel.getName());

        // 调用查询区块链示例
        queryBlockChain(client);
    }
    public static AppUser findUser() throws Exception {
        HFCAClient caClient = getHfCaClient("http://192.168.190.128:7054", null);

        // 注册或加载管理员
        AppUser admin = getAdmin(caClient);
        log.info(admin);

        // 注册并注册新用户
        AppUser appUser = getUser(caClient, admin, "hfuser");
        return appUser;
    }

    /**
     * Invoke blockchain query
     *
     * @param client The HF Client
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    static void queryBlockChain(HFClient client) throws ProposalException, InvalidArgumentException {
        // 从客户端获取通道实例
        Channel channel = client.getChannel("mychannel");
        // 创建链代码请求
        QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
        //构建提供链代码名称的cc id。, 这里省略了版本。
        ChaincodeID fabcarCCId = ChaincodeID.newBuilder().setName("example_cc_java").build();
        qpr.setChaincodeID(fabcarCCId);

        qpr.setFcn("getAllAccounts");
        Collection<ProposalResponse> res = channel.queryByChaincode(qpr);

        for (ProposalResponse pres : res) {
            String stringResponse = new String(pres.getChaincodeActionResponsePayload());
            log.info(stringResponse);
        }
    }

    /**
     * 初始化并获得HF频道
     * @param client
     * @return
     * @throws InvalidArgumentException
     * @throws TransactionException
     */
    static Channel getChannel(HFClient client) throws InvalidArgumentException, TransactionException {
        //在fabcar网络中初始化通道,对等名称和端点
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://192.168.190.128:7051");
        // fabcar网络中的eventhub名称和端点
        EventHub eventHub = client.newEventHub("eventhub01", "grpc://192.168.190.128:7053");

        Orderer orderer = client.newOrderer("orderer.example.com", "grpc://192.168.190.128:7050");

        Channel channel = client.newChannel("mychannel");

        channel.addPeer(peer);
        channel.addEventHub(eventHub);
        channel.addOrderer(orderer);
        channel.initialize();
        return channel;
    }

    /**
     * 创建新的HLF客户端
     * @return
     * @throws Exception
     */
    static HFClient getHfClient() throws Exception {
        // initialize default cryptosuite
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }


    /**
     * 使用userId注册并注册用户。如果fs上已存在名称已存在的AppUser对象，则将加载该对象，并且将跳过*注册和注册
     * @param caClient
     * @param registrar
     * @param userId
     * @return
     * @throws Exception
     */
    static AppUser getUser(HFCAClient caClient, AppUser registrar, String userId) throws Exception {
        AppUser appUser = tryDeserialize(userId);
        if (appUser == null) {
            RegistrationRequest rr = new RegistrationRequest(userId, "org1");
            String enrollmentSecret = caClient.register(rr, registrar);
            Enrollment enrollment = caClient.enroll(userId, enrollmentSecret);
            appUser = new AppUser(userId, "org1", "Org1MSP", enrollment);
            serialize(appUser);
        }
        return appUser;
    }

    /**
     * 使用{@code admin / adminpw}凭据将admin注册到fabric-ca。
     * 如果已经在fs上序列化了AppUser对象，它将被加载并且*新注册将不会被执行。
     * @param caClient
     * @return
     * @throws Exception
     */
    static AppUser getAdmin(HFCAClient caClient) throws Exception {
        AppUser admin = tryDeserialize("admin");
        if (admin == null) {
            Enrollment adminEnrollment = caClient.enroll("admin", "adminpw");
            admin = new AppUser("admin", "org1", "Org1MSP", adminEnrollment);
            serialize(admin);
        }
        return admin;
    }

    /**
     * 获得新的fabic-ca客户端
     * @param caUrl
     * @param caClientProperties
     * @return
     * @throws Exception
     */
    public static HFCAClient getHfCaClient(String caUrl, Properties caClientProperties) throws Exception {
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        HFCAClient caClient = HFCAClient.createNewInstance(caUrl, caClientProperties);
        caClient.setCryptoSuite(cryptoSuite);
        return caClient;
    }


    // user serialization and deserialization utility functions
    // files are stored in the base directory

    /**
     * 将AppUser对象序列化为文件
     * @param appUser
     * @throws IOException
     */
    static void serialize(AppUser appUser) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(
                Paths.get(appUser.getName() + ".jso")))) {
            oos.writeObject(appUser);
        }
    }

    /**
     * 从文件反序列化AppUser对象
     * @param name
     * @return
     * @throws Exception
     */
    static AppUser tryDeserialize(String name) throws Exception {
        if (Files.exists(Paths.get(name + ".jso"))) {
            return deserialize(name);
        }
        return null;
    }

    static AppUser deserialize(String name) throws Exception {
        try (ObjectInputStream decoder = new ObjectInputStream(
                Files.newInputStream(Paths.get(name + ".jso")))) {
            return (AppUser) decoder.readObject();
        }
    }
}
