package com.allen.netty.rpc.consumer;

import com.allen.netty.rpc.api.IRpcHelloService;
import com.allen.netty.rpc.consumer.proxy.RpcProxy;

/**
 * TODO
 *
 * @author zhuyunkai
 * @create 2021/9/15 11:03
 */
public class RpcConsumer {

    public static void main(String[] args) {
        IRpcHelloService rpcHelloService = RpcProxy.create(IRpcHelloService.class);
        System.out.println(rpcHelloService.hello("Allen"));
    }
}
