#Fabric-SDK
 ###Api
  * 通道的声明周期管理
  * 智能合约的生命周期管理
  * 调用，执行智能合约
  * 查询接口（区块查询、交易信息）
  * 事件监听
 ### 模块
   #### Client模块
   ##### HFClient
     >  newChannel(String name)                         创建一个已经存在的通道实例
     >  newChannel(String name, Orderer orderer, ChannelConfiguration channelConfiguration, byte[]... channelConfigurationSignatures) 创建一个新的通道，并返回通道实例
     >  getChannel(String name)                         获取通道对象实例
     >  queryChannels                                   查询peer节点加入的通道名称
     >  setCryptoSuite
     >  getCryptoSuite                                  获取客户端凭证套件
     >  newInstallProposaRequest                        安装智能合约交易提案
     >  newInstantiationProposalRequest                 创建智能合约实例化交易提案
     >  newOrderer                                      创建Orderer节点的终端实例
     >  newPeer                                         创建Peer节点的终端实例
     >  newQueryProposalRequest                         创建智能合约查询提案
     >  newTransactionProposalRequest                   创建智能合约交易提案
     >  newUpgradeProposalRequest                       创建智能合约更新提案
     >  queryInstalledChaincodes                        查询指定的Peer节点已经安装的智能合约
     >  sendInstallProposal                             向指定的Peer节点发送安装智能合约的交易提案
   #### Chains模块
   #####Channel
     >  addPeer(Peer peer) 添加一个Peer节点到通道实例中，默认节点包含所有角色
     >  addPeer(Peer peer,PeerOptions peerOptions) 添加一个Peer节点到通道实例中，指定节点角色
     >  removePeer(Peer peer)  从通道中删除peer节点实例
     >  getPeers() 获取当前通道实例中的Peer节点实例
     >  getPeers(EnumSet<PeerRole> roles) 获取指定角色
     >  joinPeer   将peer节点加入到通道中（与addPeer不同的是操作Fabric网络的对象）
     >  addOrderer   添加Orderer实例到通道实例中
     >  getOrderers    获取通道中的所有实例
     >  initialize      通道实例化