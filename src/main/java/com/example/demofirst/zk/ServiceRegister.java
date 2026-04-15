package com.example.demofirst.zk;

import jakarta.annotation.Resource;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.zookeeper.CreateMode;
//import org.apache.zookeeper.data.Stat;
import org.springframework.stereotype.Component;

@Component
public class ServiceRegister {

//    @Resource
//    private CuratorFramework zkClient;
//
//    // 注册服务：/services/user-service/ip:port
//    public void register(String serviceName, String address) throws Exception {
//        String path = "/services/" + serviceName + "/" + address;
//
//        // 检查节点是否存在
//        Stat stat = zkClient.checkExists().forPath(path);
//        if (stat == null) {
//            // 创建临时有序节点（服务下线自动删除）
//            zkClient.create()
//                    .creatingParentsIfNeeded()
//                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
//                    .forPath(path);
//        }
//    }

}
