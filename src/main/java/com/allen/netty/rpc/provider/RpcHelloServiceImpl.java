package com.allen.netty.rpc.provider;

import com.allen.netty.rpc.api.IRpcHelloService;

/**
 * TODO
 *
 * @author zhuyunkai
 * @create 2021/9/15 9:12
 */
public class RpcHelloServiceImpl implements IRpcHelloService {

    @Override
    public String hello(String name) {
        return "Hello "+ name+"!";
    }
}
