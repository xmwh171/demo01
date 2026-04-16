package com.example.demofirst.zk;

import jakarta.annotation.Resource;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class ZkDistributedLock {

//    @Resource
//    private CuratorFramework zkClient;
//
//    public boolean doLock(String key, Runnable task) {
//        String lockPath = "/lock/" + key;
//        InterProcessMutex lock = new InterProcessMutex(zkClient, lockPath);
//
//        try {
//            // 等待3秒
//            if (lock.acquire(3, TimeUnit.SECONDS)) {
//                task.run();
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                lock.release();
//            } catch (Exception ignored) {}
//        }
//        return false;
//    }
}
