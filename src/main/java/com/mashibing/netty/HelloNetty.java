package com.mashibing.netty;

import com.mashibing.io.aio.Server;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * Netty封装了NIO，对外提供了类似AIO格式的接口，封装了原生态NIO接口的ByteBuffer，使其更加友好
 * 未封装AIO是由于Linux底层NIO和AIO均使用epoll轮循方式，效率相同
 */
public class HelloNetty {
    public static void main(String[] args) {
        new NettyServer(8888).serverStart();
    }
}

class NettyServer {


    int port = 8888;

    public NettyServer(int port) {
        this.port = port;
    }

    public void serverStart() {
        //
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new Handler());
                    }
                });

        try {
            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }
}

class Handler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        System.out.println("server: channel read");
        ByteBuf buf = (ByteBuf)msg;

        System.out.println(buf.toString(CharsetUtil.UTF_8));

        ctx.writeAndFlush(msg);

        ctx.close();

        //buf.release();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
