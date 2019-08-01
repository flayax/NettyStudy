package com.mashibing.netty;

import com.mashibing.io.aio.Server;
import com.mashibing.netty.model.Demo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
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
        // 相当于多路复用器selector工作组（线程池）
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 相当于读写工作组（线程池）
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // netty服务端初始化
        ServerBootstrap b = new ServerBootstrap();
        // 关联两个工作组
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class) // 指定通道类型
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    // 当通道成功建立连接后，在通道末端添加自定义处理器new Handler()，通过Handler进行具体逻辑处理
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception { // 客户端通道具体处理逻辑在Handler()里面完成
                        /**添加自定义的编码器和解码器***传输对象需添加此块代码*****start**/
                        // 添加对象解码器 负责对序列化POJO对象进行解码 设置对象序列化最大长度为1M 防止内存溢出
                        // 设置线程安全的WeakReferenceMap对类加载器进行缓存 支持多线程并发访问 防止内存溢出
                        ch.pipeline().addLast(
                                new ObjectDecoder(1024 * 1024, ClassResolvers
                                        .weakCachingConcurrentResolver(this.getClass()
                                                .getClassLoader())));
                        // 添加对象编码器 在服务器对外发送消息的时候自动将实现序列化的POJO对象编码
                        ch.pipeline().addLast(new ObjectEncoder());
                        /**添加自定义的编码器和解码器***传输对象需添加此块代码*****end**/
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

        // 读对象
        Demo demo = (Demo)msg;
        System.out.println(demo);
//        // 读字符串
//        ByteBuf buf = (ByteBuf)msg;
//        System.out.println(buf.toString(CharsetUtil.UTF_8));

        // 写对象
        ctx.writeAndFlush(msg);
//        // 写字符串
//        ctx.writeAndFlush(Unpooled.copiedBuffer("Server Msg".getBytes()));

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
