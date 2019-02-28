package yusq.hyperledger.example.demo2;

import yusq.hyperledger.example.AppUser;
import yusq.hyperledger.example.demo.FabricConfig;
import yusq.hyperledger.example.demo.Result;
import yusq.hyperledger.example.service.ChaincodeService;
import yusq.hyperledger.example.utils.JsonUtils;
import yusq.hyperledger.example.utils.Util;
import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import yusq.hyperledger.example.demo.CreateChannel;
import yusq.hyperledger.example.demo3.queryPeer;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE;

/**
 * @Author: yusq
 * @Date: 2018/12/27 0027
 */
public class Chaincode {
    private static final FabricConfig FABRIC_CONFIG = new FabricConfig();

    public static void main(String[] args) throws Exception {

        AppUser admin = CreateChannel.getAdmin2();
//        installChaincode(admin);
        instantiate(admin);
//        createAccount1();
//        createAccount1();
//        getAllAccounts();
//        getAccountBalance();
//        depositMoney();
//        getAccountBalance();
//        checkInstantiatedChaincode(admin);
//        checkInstalledChaincode(admin);
//        queryBlockByTransactionID(admin);
    }

    /**
     * 安装链码
     */
    static void installChaincode(AppUser peerAdmin) throws Exception {
        final ChaincodeID chaincodeID;
        HFClient client = getHfClient();
        client.setUserContext(peerAdmin);
        Channel channel = queryPeer.getChannel2(peerAdmin);
        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName("bankmaster")
                .setVersion("1");
        chaincodeID = chaincodeIDBuilder.build();


        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        installProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
        installProposalRequest.setChaincodeVersion("1");
        installProposalRequest.setChaincodeInputStream(Util.generateTarGzInputStream(
                new File("E:\\Fabric\\fabric-chaincode-example-bankmaster2"),
                "src"));
        EnumSet<Peer.PeerRole> roles = EnumSet.complementOf(EnumSet.of(Peer.PeerRole.ENDORSING_PEER));
        Collection<Peer> peers = channel.getPeers(roles);
        Collection<ProposalResponse> responses;
        responses = client.sendInstallProposal(installProposalRequest, peers);
        for (ProposalResponse respons : responses) {
            System.out.println("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            System.out.println("ChaincodeID" + respons.getChaincodeID());
            System.out.println(respons.getChaincodeActionResponseStatus());
            System.out.println(respons.getMessage());
        }
    }

    /**
     * @param admin
     * @return
     * @throws Exception
     */
    private static boolean checkInstalledChaincode(AppUser admin) throws Exception {
        HFClient client = getHfClient();
        client.setUserContext(admin);
        Properties peerProperties = FABRIC_CONFIG.getPeerProperties("peer0.org1.example.com");
        Peer peer = client.newPeer("peer0.org1.example.com", "grpc://192.168.190.128:7051", peerProperties);
        List<Query.ChaincodeInfo> ccinfoList = client.queryInstalledChaincodes(peer);

        boolean found = false;

        for (Query.ChaincodeInfo ccifo : ccinfoList) {
            System.out.println("name" + ccifo.getName());
            System.out.println("path" + ccifo.getPath());
            System.out.println("version" + ccifo.getVersion());
        }

        return found;
    }

    private static boolean checkInstantiatedChaincode(AppUser admin) throws Exception {
        HFClient client = getHfClient();
        client.setUserContext(admin);
        Channel channel = CreateChannel.getChannel(admin);
        for (Peer peer : channel.getPeers()) {
            List<Query.ChaincodeInfo> ccinfoList = channel.queryInstantiatedChaincodes(peer);
            for (Query.ChaincodeInfo ccifo : ccinfoList) {
                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                System.out.println("name:" + ccifo.getName() + "path:" + ccifo.getPath() + "version:" + ccifo.getVersion());

            }
        }

        boolean found = false;


        return found;
    }

    /**
     * 实例化链码
     *
     * @param peerAdmin
     * @throws Exception
     */
    static void instantiate(AppUser peerAdmin) throws Exception {

        final ChaincodeID chaincodeID;

        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName("bankmaster")
                .setVersion("1");
        chaincodeID = chaincodeIDBuilder.build();
        Channel channel = queryPeer.getChannel2(peerAdmin);
        channel.initialize();
        Collection<Orderer> orderers = channel.getOrderers();
        for (Orderer orderer : orderers) {
            System.out.println("->>>>>>>>>>>>>>>>>>>>>");
            System.out.println(orderer.getName());
            System.out.println(orderer.getUrl());
        }
        HFClient client = HFClient.createNewInstance();

        client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
        client.setUserContext(peerAdmin);

        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
        instantiateProposalRequest.setFcn("init");
        instantiateProposalRequest.setArgs(new String[]{"100000"});

        Map<String, byte[]> tm = new HashMap<>();
        tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
        tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
        instantiateProposalRequest.setTransientMap(tm);

//        背书策略E:\Fabric\scratch\hyperledger\fabric-sdk-java-scratch\
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File("E:\\Fabric\\Sample\\scratch\\hyperledger\\fabric-sdk-java-scratch\\basic-network\\chaincode\\chaincodeendorsementpolicy.yaml"));
//        instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

        Collection<ProposalResponse> responses = channel.sendInstantiationProposal(instantiateProposalRequest, channel.getPeers());
        for (ProposalResponse response : responses) {
            System.out.println("Message:" + response.getMessage());
            System.out.println("Instantiation_status:" + response.getStatus());
            System.out.println("=====");
            System.out.println("ChaincodeID:" + response.getChaincodeID());
            System.out.println(response.getChaincodeActionResponseStatus());
            System.out.println(response.getPeer());
        }
        channel.sendTransaction(responses,channel.getOrderers()).thenApply(transactionEvent -> {
            System.out.println(transactionEvent.isValid());
            return null;
        }).get(3,TimeUnit.SECONDS);
        System.out.println(responses);
    }

    /**
     * 创建账户
     *
     * @throws Exception
     */

    public static void createAccount1() throws Exception {
        ChaincodeService chaincodeService = new ChaincodeService();
        Map<String, Object> account = new HashMap<String, Object>();
        account.put("realName", "测试1");
        account.put("idCardNo", "3424251985281442");
        account.put("mobilePhone", "15060135731");
        account.put("accountBalance", 1000);
        Map<String, byte[]> transientData = null;
        Result<String> result = chaincodeService.execute("createAccount", new String[]{JsonUtils.object2Json(account)}, transientData, ChaincodeService.ChaincodeFunctionAccessType.UPDATE, true);
        System.out.println("【createAccount】>>> result = " + result);
    }

    /**
     * 查询所有账户列表
     *
     * @throws Exception
     */
    public static void getAllAccounts() throws Exception {
        ChaincodeService chaincodeService = new ChaincodeService();
        Map<String, byte[]> transientData = null;
        Result<String> result = chaincodeService.execute("getAllAccounts", new String[]{""}, transientData, ChaincodeService.ChaincodeFunctionAccessType.QUERY, true);
        System.out.println("【getAccountBalance】>>> result = " + result);
    }
    /**
     * 查询账户余额
     * @throws Exception
     */
    public static void getAccountBalance() throws Exception {
        //String accountNo = "6225149443666547";
        ChaincodeService chaincodeService = new ChaincodeService();
        String accountNo = "6225573925880226";
        Result<String> result = chaincodeService.executeQuery("getAccountBalance", new String[] {accountNo});
        System.out.println("【getAccountBalance】>>> result = " + result);
    }
    /**
     * 存款
     * @throws Exception
     */
    public static void depositMoney() throws Exception {

        ChaincodeService chaincodeService = new ChaincodeService();
        String accountNo = "6225573925880226";
        String amount = "700";
        Result<String> result = chaincodeService.executeUpdate("depositMoney", new String[] {accountNo, amount});
        System.out.println("【depositMoney】>>> result = " + result);
    }
    public static HFClient getHfClient() throws Exception {
        // initialize default cryptosuite
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        // setup the client
        HFClient client = HFClient.createNewInstance();
        client.setCryptoSuite(cryptoSuite);
        return client;
    }

    public static void queryBlockByTransactionID(AppUser peerAdmin) throws Exception {
        Channel channel = CreateChannel.getChannel(peerAdmin);
        channel.initialize();
        String transactionId = "3a8faa78d5a7cb912f69e1973c442f45f3fbad7e68932614c45152e36efab009";
        BlockInfo blockInfo = channel.queryBlockByTransactionID(transactionId);
        int envelopeCount = blockInfo.getEnvelopeCount();
        if(envelopeCount > 0) {
            for (BlockInfo.EnvelopeInfo envelopeInfo : blockInfo.getEnvelopeInfos()) {
                System.out.println(">>> envelopeType = " + envelopeInfo.getType());
                if (envelopeInfo.getType() == TRANSACTION_ENVELOPE) {
                    BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;
                    for(BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo.getTransactionActionInfos()) {
                        List<String> args = new ArrayList<String>();
                        int argLength = transactionActionInfo.getChaincodeInputArgsCount();
                        for(int i = 0; i < argLength; i++) {
                            byte[] argBytes = transactionActionInfo.getChaincodeInputArgs(i);
                            args.add(new String(argBytes, "UTF-8"));
                        }
                        System.out.println(String.format(">>> chaincodeId = %s, args = %s", transactionActionInfo.getChaincodeIDName(), args));
                    }
                }
            }
        }
        System.out.println(blockInfo);

    }

}
