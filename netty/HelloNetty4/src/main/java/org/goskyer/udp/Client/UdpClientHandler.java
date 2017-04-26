package org.goskyer.udp.Client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


/**
 * Created by IntelliJ IDEA 14.
 * User: karl.zhao
 * Time: 2016/1/21 0021.
 */
public class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        String response = msg.content().toString(CharsetUtil.UTF_8);

        if (response.startsWith("结果：")) {
            System.out.println(response);
            ctx.close();
        }
    }
}
