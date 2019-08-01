package com.mashibing.netty.other.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
 
public class Client {
 
	/**
	 * 连接服务器
	 * 
	 * @param port
	 * @param host
	 * @throws Exception
	 */
	public void connect(int port, String host) throws Exception {
		// 配置客户端NIO线程组
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			// 客户端辅助启动类 对客户端配置
			Bootstrap b = new Bootstrap();
			b.group(group)//
					.channel(NioSocketChannel.class)//
					.option(ChannelOption.TCP_NODELAY, true)//
					.handler(new MyChannelHandler());//
			// 异步链接服务器 同步等待链接成功
			ChannelFuture f = b.connect(host, port).sync();
 
			// 等待链接关闭
			f.channel().closeFuture().sync();

		} finally {
			group.shutdownGracefully();
			System.out.println("客户端优雅的释放了线程资源...");
		}
 
	}
 
	/**
	 * 网络事件处理器
	 */
	private class MyChannelHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			// 添加自定义的编码器和解码器
			// 添加POJO对象解码器 禁止缓存类加载器
			ch.pipeline().addLast(
					new ObjectDecoder(1024, ClassResolvers.cacheDisabled(this
							.getClass().getClassLoader())));
			// 设置发送消息编码器
			ch.pipeline().addLast(new ObjectEncoder());
			// 处理网络IO
			ch.pipeline().addLast(new ClientHandler());// 处理网络IO
		}
 
	}
 
	public static void main(String[] args) throws Exception {
		new Client().connect(9999, "127.0.0.1");
 
	}
 
}