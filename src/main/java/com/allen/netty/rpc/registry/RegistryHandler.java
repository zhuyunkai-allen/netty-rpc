package com.allen.netty.rpc.registry;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.allen.netty.rpc.protocol.InvokerProtocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * TODO
 *
 * @author zhuyunkai
 * @create 2021/9/15 9:26
 */
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    //保存服务
    public static ConcurrentHashMap<String,Object> registryMap = new ConcurrentHashMap<>();

    //保存服务类
    public static List<String> classNames = new ArrayList<>();

    public RegistryHandler(){
        scannerClass("com.allen.netty.rpc.provider");
        doRegister();
    }

    private void scannerClass(String packageName){
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        for (File file:dir.listFiles()){
            if(file.isDirectory()){
                scannerClass(packageName+"."+file.getName());
            }else {
                classNames.add(packageName+"."+file.getName().replace(".class","").trim());
            }
        }
    }

    private void doRegister(){
        if(classNames.size()==0){
            return;
        }
        classNames.forEach(className -> {
            try{
                Class<?> clazz = Class.forName(className);
                Class<?> i = clazz.getInterfaces()[0];
                registryMap.put(i.getName(),clazz.newInstance());
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol) msg;

        if (registryMap.containsKey(request.getClassName())) {
            Object clazz = registryMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(),request.getParams());
            result = method.invoke(clazz,request.getValues());
        }
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
