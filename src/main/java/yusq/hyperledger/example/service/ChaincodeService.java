package yusq.hyperledger.example.service;

import yusq.hyperledger.example.AppUser;
import yusq.hyperledger.example.demo.CreateChannel;
import yusq.hyperledger.example.demo.Result;
import org.hyperledger.fabric.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @Author: yusq
 * @Date: 2018/12/27 0027
 */
public class ChaincodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChaincodeService.class);


    public Result<String> executeQuery(String fcn, String[] args) throws Exception {
        return executeQuery(fcn, args, null);
    }

    public Result<String> executeQuery(String fcn, String[] args, Map<String,byte[]> transientData) throws Exception {
        return execute(fcn, args, transientData, ChaincodeFunctionAccessType.QUERY, false);
    }
    public Result<String> executeUpdate(String fcn, String[] args) throws Exception {
        return executeUpdate(fcn, args, null);
    }
    public Result<String> executeUpdate(String fcn, String[] args, Map<String, byte[]> transientData) throws Exception {
        return execute(fcn, args, transientData, ChaincodeFunctionAccessType.UPDATE, false);
    }
    public Result<String> execute(String fcn, String[] args, Map<String,byte[]> transientData, ChaincodeFunctionAccessType funcType, boolean asyncUpdate) throws Exception {
        LOGGER.info("【chaincode】>>> Prepare to invoke chaincode({}) now, fcn = {}, args = {} ", "bankmaster", fcn, Arrays.toString(args));
        AppUser admin = CreateChannel.getAdmin();
        HFClient client = CreateChannel.getHfClient();
        client.setUserContext(admin);
        Channel channel = CreateChannel.getChannel(admin);
        channel.initialize();
        final ChaincodeID chaincodeID;
        ChaincodeID.Builder chaincodeIDBuilder = ChaincodeID.newBuilder().setName("bankmaster")
                .setVersion("1");
        chaincodeID = chaincodeIDBuilder.build();

        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(chaincodeID);

        transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
        transactionProposalRequest.setFcn(fcn);
        if(args != null) {
            transactionProposalRequest.setArgs(args);
        }
        if(transientData!=null) {
            transactionProposalRequest.setTransientMap(transientData);
        }
        transactionProposalRequest.setProposalWaitTime(600000);

        List<ProposalResponse> successResponses = new ArrayList<ProposalResponse>();
        List<ProposalResponse> failureResponses = new ArrayList<ProposalResponse>();

        long startTime = System.currentTimeMillis();
        Collection<ProposalResponse> proposalResponses = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
        for (ProposalResponse proposalResponse : proposalResponses) {
            System.out.println("==============================================");
            System.out.println(proposalResponse.getStatus());
            System.out.println(proposalResponse.getMessage());
            if (proposalResponse.getStatus() == ProposalResponse.Status.SUCCESS) { //成功的提案
                successResponses.add(proposalResponse);
                LOGGER.info("【chaincode】>>> Successful transaction proposal response Txid: {} from peer {}", proposalResponse.getTransactionID(), proposalResponse.getPeer().getName());
            } else { //失败的提案
                failureResponses.add(proposalResponse);
                LOGGER.error("【chaincode】>>> Failed transaction proposal response Txid: {} from peer {}", proposalResponse.getTransactionID(), proposalResponse.getPeer().getName());
            }
        }
        LOGGER.info("【chaincode】>>> Channel send transaction proposal all done, it takes time : " + ( System.currentTimeMillis() - startTime));
        LOGGER.info("【chaincode】>>> Received {} transaction proposal responses, successful and verified：{}, failed：{}", proposalResponses.size(), successResponses.size(), failureResponses.size());

        if(!failureResponses.isEmpty()) { //存在失败的提案
            ProposalResponse firstProposalResponse = failureResponses.iterator().next();
            LOGGER.error("【chaincode】>>> Not enough endorsers for invoke chaincode({})：{}, endorser error：{} . Was verified：{}", failureResponses.size(), firstProposalResponse.getMessage(), firstProposalResponse.isVerified());
            return Result.failure().message("endorser error：" + firstProposalResponse.getMessage()).build();
        } else { //全部是成功的提案
            Collection<Set<ProposalResponse>> proposalConsistencySets = SDKUtils.getProposalConsistencySets(proposalResponses); //检测各个节点成功背书结果的一致性
            if(proposalConsistencySets.size() != 1) { //如果所有节点的背书结果存在不同结果
                LOGGER.error("【chaincode】>>> Expected only one set of consistent proposal responses but got {}.", proposalConsistencySets.size());
            }
            if(ChaincodeFunctionAccessType.UPDATE.equals(funcType)) { //如果是更新操作，则需要最终提交更新的事务
                CompletableFuture<BlockEvent.TransactionEvent> future = channel.sendTransaction(successResponses); //最终提交事务
                if(!asyncUpdate) {
                    future.get(); //同步操作
                }
            }
            ProposalResponse successResponse = successResponses.iterator().next();
            LOGGER.info("【chaincode】>>> Successfully received transaction proposal response：{}", successResponse);
            byte[] dataBytes = successResponse.getChaincodeActionResponsePayload(); // chaincode返回结果
            String dataString = new String(dataBytes, "UTF-8");
            return Result.success().data(dataString).build();
        }
    }
    public static enum ChaincodeFunctionAccessType {

        QUERY, UPDATE;

    }
}
