package org.goskyer.timeserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by zzqno on 2017-4-18.
 * <p>
 * <p>
 * 当服务端与客户端建立TCP连接时
 * NIO线程会调用channelActive() 发送查询时间的指令给服务端
 * 调用ChannelHandlerContext.writeAndFlush将消息发送给服务端
 */
public class TimeClientHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf firstMessage;

    public TimeClientHandler() {
        byte[] req = "Query time order".getBytes();
        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(firstMessage);
    }

    /**
     * 当服务端返回应答时 调用此方法 并从ByteBuf中读取应答信息
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] res = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(res);
        String resBody = new String(res,"UTF-8");
        System.out.println("now is:" + resBody);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause.getMessage());
        ctx.close();
    }
}
