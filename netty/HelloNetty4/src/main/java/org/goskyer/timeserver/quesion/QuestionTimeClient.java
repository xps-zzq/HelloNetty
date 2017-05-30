package org.goskyer.timeserver.quesion;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.goskyer.timeserver.*;

import java.net.InetSocketAddress;

/**
 * Created by zzqno on 2017-5-30.
 */
public class QuestionTimeClient {

    public void connect(int port, String host) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.remoteAddress(host, port);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel)
                        throws Exception {
                    socketChannel.pipeline().addLast(new QuestionTimeClientHandler());
                }
            });
            ChannelFuture future;
            try {
                future = bootstrap.connect(host, port).sync();
                if (future.isSuccess()) {
                    System.out.println("----------------connect server success----------------");
                }
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }
    public static void main(String[] args) {
        int port = 10000;
        new QuestionTimeClient().connect(port,"localhost");
    }


    private class QuestionTimeClientHandler extends ChannelInboundHandlerAdapter {
        private int counter;
        private byte[] req;

        public QuestionTimeClientHandler() {
            req = ("QUERY TIME ORDER" + System.getProperty("line.separator")).getBytes();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ByteBuf message;
            for (int i = 0; i < 100; i++) {
                message = Unpooled.buffer(req.length);
                message.writeBytes(req);
                ctx.writeAndFlush(message);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf buf = (ByteBuf) msg;
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);
            String body = new String(req, "UTF-8");
            System.out.println("Now is :" + body + ";the counter is:" + ++counter);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("Unexpected exception from downStream:"+ctx.close());
            ctx.close();
        }
    }


}
