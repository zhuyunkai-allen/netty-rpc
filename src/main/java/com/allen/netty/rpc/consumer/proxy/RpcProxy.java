package com.allen.netty.rpc.consumer.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.allen.netty.rpc.protocol.InvokerProtocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * TODO
 *
 * @author zhuyunkai
 * @create 2021/9/15 10:00
 */
public class RpcProxy {

    public static <T> T create(Class<?> clazz){
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?> [] in = clazz.isInterface()?new Class[]{clazz}:clazz.getInterfaces();
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),in,proxy);
    }

    private static class MethodProxy implements InvocationHandler{

        private Class<?> clazz;

        public MethodProxy(Class<?> clazz){
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(Object.class.equals(method.getDeclaringClass())){
                try {
                    return method.invoke(this,args);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else {
                return rpcInvoke(proxy,method,args);
            }
            return null;
        }

        public Object rpcInvoke(Object proxy, Method method, Object[] args){
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setValues(args);
            msg.setMethodName(method.getName());
            msg.setParams(method.getParameterTypes());

            final RpcProxyHandler consumerHandler = new RpcProxyHandler();
            EventLoopGroup group = new NioEventLoopGroup();
            try{
                Bootstrap b = new Bootstrap();
                b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY,true).handler(
                        new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel)
                                    throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                pipeline.addLast("frameDecoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                                pipeline.addLast("frameEncoder",new LengthFieldPrepender(4));
                                pipeline.addLast("encoder",new ObjectEncoder());
                                pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE,
                                        ClassResolvers.cacheDisabled(null)));
                                pipeline.addLast("handler",consumerHandler);

                            }
                        });
                ChannelFuture future = b.connect("localhost",8080).sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                group.shutdownGracefully();
            }
            return consumerHandler.getResponse();

        }
    }
}
