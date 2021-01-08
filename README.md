# rvListerDemo

### 这是一个 Tibco RV 的发送消息和接收消息的实例

#### 实现监听rv发送的message，监听到后将message中的xml转为对象

   1. 启动多个TibrvTransport  和TibrvListener,监听多个service
   2. 启动TibrvTransport时server判断是否有正常工作，如果没有，则启动备用监听
   3. 创建inbox启用监听
   4. runtime()显示当前进程的内存大小，启动线程池分配线程执行消息处理。

         #### RV 消息监听流程

         ![inbox创建与监听](https://github.com/yanzhao77/rvListerDemo/blob/master/resources/picture/%E6%80%BB%E6%B5%81%E7%A8%8B%E5%9B%BE.png)

#### RV 消息监听处理架构图

![inbox创建与监听](https://github.com/yanzhao77/rvListerDemo/blob/master/resources/picture/%E6%9E%B6%E6%9E%84%E5%9B%BE.png)

#### RV 消息监听 –监听不同的service

在需要监听多个service时，只需要将参数保存到集合里，遍历创建不同的TibrvTransport
和对应的TibrvListener即可
![inbox创建与监听](https://github.com/yanzhao77/rvListerDemo/blob/master/resources/picture/%E7%9B%91%E5%90%AC%E5%A4%9A%E4%B8%AAservice.png)


#### RV 消息监听 –主备机制

在创建TibrvTransport时，会检测是否能够创建主机是否正常。
方法 TibrvTransport.isValid（）会返回主机的工作状态，如果主机没有打开，那么就启动备用机
![inbox创建与监听](https://github.com/yanzhao77/rvListerDemo/blob/master/resources/picture/%E4%B8%BB%E5%A4%87%E6%9C%BA%E5%88%B6.png)

#### RV 消息监听 –多个subjectName

RV监听是在指定RV的service，network，deason，和指定的subjectName(可以多个)进行监听
![inbox创建与监听](https://github.com/yanzhao77/rvListerDemo/blob/master/resources/picture/%E7%9B%91%E5%90%AC%E5%A4%9A%E4%B8%AAsubjectName.png)

#### RV 消息监听 –inbox

inbox监听是在指定RV的service，network，deason后，指定点对点的进行 server –client通信
在RV中，主要是通过创建inbox(唯一的通信地址)来实现监听

![inbox创建与监听](https://github.com/yanzhao77/rvListerDemo/blob/master/resources/picture/inbox%E5%88%9B%E5%BB%BA%E4%B8%8E%E7%9B%91%E5%90%AC.png)

#### RV 消息处理

将消息进行筛选后，使用Xstram 将类转换为对象，并输出

![inbox创建与监听](https://github.com/yanzhao77/rvListerDemo/blob/master/resources/picture/%E6%B6%88%E6%81%AF%E5%A4%84%E7%90%86.png)



#### 监听系统通知主备切换

主备切换即是在主机收不到消息或者主机关闭时，能够切换到备用机上继续监听消息，实现服务的连续性。实现主备切换的重点在于对于rv的守护进程（daemon）的监听，在daemon发送错误信息时，根据信息情况，自动切换到备用机上

