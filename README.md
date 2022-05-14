##架构

<img width="676" alt="f66db18c-96bf-40c2-b1d2-3b5d2c7e114d" src="https://user-images.githubusercontent.com/48942966/168422425-e54a0c7f-86bc-418b-9bd8-46ceb78d8915.png">

##实现组件
1. node：一个node（netty server）的静态信息，如ip和port
2. worker：一个node（netty server）运行起来的动态信息，如将node注册到zk中，从zk中获取node的id信息
3. worker router：负责管理本node到所有其他node的连接，用于将发给非本节点管理的客户端的消息转发给该客户端对应的节点；同时监听zk中node的新增和删除，从而建立和断开本节点和其他节点
4. peer sender：本node到其他node的一个连接实例（本节点连接其他节点的netty client）
5. remote notification handler：负责处理其他节点发来的通知，如其他节点所管理的客户端上下线的通知；其他节点连接成功本节点的通知
6. sessionmanager

    负责管理本节点的下面四种session
    
    a. 本节点管理的客户端在本节点的localsession（会通过通知发给其他节点，在其他节点生成该客户端的remotesession），客户端id是客户端的ip
    
    ```
         client          —>         node
     
         clientsession          localsession
     
         client(clientid: ip)
    ```

    b. 连接到本节点的apiserver在本节点的localsession，apiserver会给指定客户端发送命令执行消息，通过指定客户端的id在本节点所管理的session中获取对应的session，然后进行消息发送，如果指定的客户端是本节点所管理的直接发送（指定客户端localsession），如果指定的客户端不是本节点所管理的就转发给指定客户端的管理节点（指定客户端remotesession），客户端id是apiserver的ip和port
    
    ```
        apiserver        —>         node
        
        clientsession           localsession
        
        client(clientid: ip:port)
    ```
    
    c. 别的节点作为客户端连接本节点在本节点的localsession，客户端id是别的节点的ip和port
    
    ```
        node            —>          node
   
        clientsession           localsession
   
        client(clientid: ip:port)
    ```
    
    d. 非本节点管理的客户端在本节点的remotesession，客户端id是客户端的ip

    ```
       非本节点管理的client 在本节点的session
       
       remotesession（该client所连接节点生成的localsession，会在客户端上线之后通过通知方式发给其他节点）
       client(clientid: ip)
    ```
