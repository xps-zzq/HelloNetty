package org.goskyer.http.fileserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * Created by zzqno on 2017-4-20.
 * 服务端
 */
public class HttpFileServer {

    public static final String DEFAULT_URL = "d:";

    public void run(final int port, final String url) {
        EventLoopGroup bossloop = new NioEventLoopGroup();
        EventLoopGroup workloop = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossloop, workloop);
        serverBootstrap.channel(NioServerSocketChannel.class);
        serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel)
                    throws Exception {
                ChannelPipeline p = socketChannel.pipeline();
                p.addLast(new HttpRequestDecoder());
                //将多个消息转换为FullHttpRequest 或者 FullHttpResponse
                p.addLast(new HttpObjectAggregator(65536));
                p.addLast(new HttpResponseEncoder());
                //支持异步发送大的码流不占用过多内存 防止内存溢出
                p.addLast(new ChunkedWriteHandler());
                p.addLast(new HttpFileServerHandler(""));
            }
        });
        ChannelFuture future;
        try {
            future = serverBootstrap.bind("127.0.0.1", port).sync();
            System.out.println("HTTP 文件目录服务器启动...");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            System.out.println("启动服务器异常...");
            e.printStackTrace();
        }finally {
            bossloop.shutdownGracefully();
            workloop.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new HttpFileServer().run(8080,HttpFileServer.DEFAULT_URL);
    }
}
