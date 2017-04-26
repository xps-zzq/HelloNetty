package org.goskyer.timeserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.Date;

/**
 * Created by zzqno on 2017-4-25.
 */
public class TimeServerHandler extends ChannelInboundHandlerAdapter {


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

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        /**
         * 为了防止频繁的唤醒Selector 进行消息发送 Netty Write()不直接将消息写入SocketChannel中
         * 调用write() 只是把待发送的消息放到缓冲数组中，再通过调用flush()  将缓冲区中的消息写到socketchannel
         */
        //将消息发送队列中的消息写入到SocketChannel中发送给对方
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //发生异常时 关闭ChannelHandlerContext 释放资源
        ctx.close();
    }
}
