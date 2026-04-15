package com.example.demofirst.zk;

import jakarta.annotation.Resource;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.recipes.cache.PathChildrenCache;
//import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ServiceDiscovery {

//    @Resource
//    private CuratorFramework zkClient;
//
//    // 获取服务列表
//    public List<String> getInstances(String serviceName) throws Exception {
//        String path = "/services/" + serviceName;
//        return zkClient.getChildren().forPath(path);
//    }
//
//    // 监听服务上下线
//    public void watchService(String serviceName) throws Exception {
//        String path = "/services/" + serviceName;
//        PathChildrenCache cache = new PathChildrenCache(zkClient, path, true);
//        cache.start();
//
//        PathChildrenCacheListener listener = (client, event) -> {
//            System.out.println("服务节点发生变化：" + event.getType());
//            System.out.println("最新列表：" + getInstances(serviceName));
//        };
//
//        cache.getListenable().addListener(listener);
//    }

}
