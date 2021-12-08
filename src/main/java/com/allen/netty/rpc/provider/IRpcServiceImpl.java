package com.allen.netty.rpc.provider;

import com.allen.netty.rpc.api.IRpcService;

/**
 * TODO
 *
 * @author zhuyunkai
 * @create 2021/9/15 9:13
 */
public class IRpcServiceImpl implements IRpcService {

    @Override
    public int add(int a, int b) {
        return a+b;
    }
}
