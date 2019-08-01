package com.mashibing.netty.other.server;

import com.mashibing.netty.other.model.Request;
import com.mashibing.netty.other.model.Response;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	// 用于获取客户端发送的信息
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		// 用于获取客户端发来的数据信息
		Request body = (Request) msg;
		System.out.println("Server接受的客户端的信息 :" + body.toString());
 
		// 会写数据给客户端
		Response response = new Response(Integer.parseInt(body.getUrl()),
				"xiaoming");
		// 当服务端完成写操作后，关闭与客户端的连接
		ctx.writeAndFlush(response);
		// .addListener(ChannelFutureListener.CLOSE);
 
		// 当有写操作时，不需要手动释放msg的引用
		// 当只有读操作时，才需要手动释放msg的引用
	}
 
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// cause.printStackTrace();
		ctx.close();
	}
}