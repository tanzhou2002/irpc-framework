package org.idea.irpc.framework.core.proxy.javassist;

import org.idea.irpc.framework.core.common.PackingRpcInvocation;
import org.idea.irpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.idea.irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static org.idea.irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

public class JavassistInvocationHandler implements InvocationHandler {
    private final static Object OBJECT = new Object();

    private Class<?> clazz;

    public JavassistInvocationHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setTargetServiceName(clazz.getName());
        //这里面注入了一个uuid，对每一次请求都做了单独区分
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        Semaphore semaphore = new Semaphore(1);
        RESP_MAP.put(rpcInvocation.getUuid(), new PackingRpcInvocation(rpcInvocation, semaphore));
        //这里就是将请求参数放入到发送队列中
        SEND_QUEUE.add(rpcInvocation);
        semaphore.acquire();
        if (semaphore.tryAcquire(3, TimeUnit.SECONDS)) {
            return ((RpcInvocation) RESP_MAP.get(rpcInvocation.getUuid())).getResponse();
        } else {
            throw new TimeoutException("client wait server's response timeout");
        }
    }
}
