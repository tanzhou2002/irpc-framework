package org.idea.irpc.framework.core.common;

import java.util.concurrent.Semaphore;

public class PackingRpcInvocation {
    private RpcInvocation rpcInvocation;
    private Semaphore semaphore;

    public PackingRpcInvocation(RpcInvocation rpcInvocation, Semaphore semaphore) {
        this.rpcInvocation = rpcInvocation;
        this.semaphore = semaphore;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public RpcInvocation getRpcInvocation() {
        return rpcInvocation;
    }

    public void setRpcInvocation(RpcInvocation rpcInvocation) {
        this.rpcInvocation = rpcInvocation;
    }
}
