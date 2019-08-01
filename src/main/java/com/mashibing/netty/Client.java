package com.mashibing.netty;

import com.mashibing.netty.model.Demo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class Client {
    public static void main(String[] args) {
        new Client().clientStart();
    }

    private void clientStart() {
        EventLoopGroup workers = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workers)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("channel initialized!");
                        /**添加自定义的编码器和解码器***传输对象需添加此块代码*****start**/
                        // 添加POJO对象解码器 禁止缓存类加载器
                        ch.pipeline().addLast(
                                new ObjectDecoder(1024, ClassResolvers.cacheDisabled(this
                                        .getClass().getClassLoader())));
                        // 添加对象编码器 在服务器对外发送消息的时候自动将实现序列化的POJO对象编码
                        ch.pipeline().addLast(new ObjectEncoder());
                        /**添加自定义的编码器和解码器***传输对象需添加此块代码*****end**/
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

        try {
            System.out.println("start to connect...");
            ChannelFuture f = b.connect("127.0.0.1", 8888).sync();

            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            workers.shutdownGracefully();
            System.out.println("客户端优雅的释放了线程资源...");
        }

    }


}

class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel is activated.");

        // 写对象
        Demo demo = new Demo(1, "flaya");
        final ChannelFuture f = ctx.writeAndFlush(demo);
//        // 写字符串
//        final ChannelFuture f = ctx.writeAndFlush(Unpooled.copiedBuffer("HelloNetty".getBytes()));
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("msg send!");
                //ctx.close();
            }
        });


    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            // 读对象
            Demo demo = (Demo)msg;
            System.out.println(demo);
//            // 读字符串
//            ByteBuf buf = (ByteBuf)msg;
//            System.out.println(buf.toString(CharsetUtil.UTF_8));

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }
}
