package nrpc.callback;


import nrpc.exception.transfer.RequestTimeoutException;
import nrpc.rpc.RpcRequest;
import nrpc.rpc.RpcResponse;
import scala.concurrent.util.Unsafe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ConcurrentCallback {
    private final static long RESPONSE_OFFSET;
    private final ReentrantLock lock;
    private final Condition condition;
    private volatile RpcResponse response = null;
    private final RpcRequest request;
    private final Long timeout;

    public RpcResponse getResponse() {
        return response;
    }

    public RpcRequest getRequest() {
        return request;
    }

    public Long getTimeout() {
        return timeout;
    }

    static {
        try {
            RESPONSE_OFFSET = Unsafe.instance.objectFieldOffset(ConcurrentCallback.class.getDeclaredField("response"));
        } catch (Exception e) {
            throw new Error("ConcurrentCallback RESPONSE_OFFSET :" + e.getMessage());
        }
    }

    public ConcurrentCallback(RpcRequest request, Long timeout) {
        this.request = request;
        this.timeout = timeout;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    public boolean putIfAbsent(RpcResponse response) {
        if (this.response != null) return false;
        return Unsafe.instance.compareAndSwapObject(this, RESPONSE_OFFSET, null, response);
    }

    public RpcResponse getRpcResponse() {
        return this.response;
    }

    public void notifyCallback() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public RpcResponse awaitCallback(long timeoutMils) throws InterruptedException {
        if (this.response != null) return this.response;

        long timeout = System.currentTimeMillis() + timeoutMils;
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (System.currentTimeMillis() < timeout) {
                if (this.response != null) {
                    return this.response;
                } else {
                    condition.await(timeoutMils, TimeUnit.MILLISECONDS);
                    //唤醒后再尝试一次
                    if (this.response != null) return this.response;
                }
            }
        } finally {
            lock.unlock();
        }
        throw new RequestTimeoutException("timeout");
    }
}
