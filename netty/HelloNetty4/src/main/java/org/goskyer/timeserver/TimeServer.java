package org.goskyer.timeserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Created by zzqno on 2017-4-18.
 */
public class TimeServer {
    /**
     * 创建两个EventLoopGroup线程组 一个用于接受客户端连接 另外一个用于SocketChannel的网络读写
     * （EventLoopGroup 包含了一组NIO线程 实际上它们是Reactor线程组）
     *
     * @param port
     */
    public void bind(int port) {
        //配置服务端的NIO 线程组
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //服务端辅助启动类
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boosGroup, workerGroup);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024); //连接数
        // serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true); //长连接
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel)
                    throws Exception {
                socketChannel.pipeline().addLast(new TimeServerHandler());
            }
        });
        try {
            //绑定端口 同步等待成功
            //ChannelFuture 异步操作回调通知
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(port)).sync();
            if (channelFuture.isSuccess()) {
                System.out.println("启动Netty服务成功 端口号：" + port);
            }
            //等待服务端监听端口关闭 等待服务端链路关闭之后main函数才推出
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //释放线程池资源
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        new TimeServer().bind(port);
    }

}
