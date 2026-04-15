package com.example.demofirst.test;

import java.util.concurrent.locks.LockSupport;

/**
 * 测试lockSupport的park()与unpark(Thread thread)方法
 */
public class TestLockSupport {

    private void printA(Thread thread) {
        try{
            Thread.sleep(20);
            System.out.println("A");
            LockSupport.unpark(thread);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void printB(Thread thread) {
        try{
            Thread.sleep(10);
            LockSupport.park();
            System.out.println("B");
            LockSupport.unpark(thread);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void printC() {
        try{
            Thread.sleep(5);
            LockSupport.park();
            System.out.println("C");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        TestLockSupport t = new TestLockSupport();
        Thread tc = new Thread(t::printC);
        Thread tb = new Thread(()->t.printB(tc));
        Thread ta = new Thread(()->t.printA(tb));

        ta.start();
        tb.start();
        tc.start();

    }

}
