package org.goskyer.timeserver.quesion;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.goskyer.timeserver.TimeServerHandler;

import java.net.InetSocketAddress;
import java.util.Date;

/**
 * Created by zzqno on 2017-4-18.
 */
public class QuestionTimeServer {
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
                socketChannel.pipeline().addLast(new ChildChannelHandler());
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

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new QuestionTimeServerHandler());
        }
    }

    private class QuestionTimeServerHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //msg 转换成ByteBuf 类似ByteBuffer 不过提供了更多功能
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] req = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(req);
            String body = new String(req, "UTF-8");
            System.out.println("The time server receive order:" + body);
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "BAD ORDER";
            ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
            //异步发送应答消息给客户端
            ctx.writeAndFlush(resp);
            ctx.close();
        }
    }



    public static void main(String[] args) {
        int port = 10000;
        new QuestionTimeServer().bind(port);
    }

}
