package com.allen.netty.rpc.protocol;

import java.io.Serializable;

import lombok.Data;

/**
 * TODO
 *
 * @author zhuyunkai
 * @create 2021/9/15 9:09
 */
@Data
public class InvokerProtocol implements Serializable {

    private String className;

    private String methodName;

    private Class<?>[] params;

    private Object[] values;
}
