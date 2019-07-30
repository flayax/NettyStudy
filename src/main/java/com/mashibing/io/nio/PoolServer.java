package com.mashibing.io.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程池NIO多路复用，此处线程池处理读写数据，selector只用于处理通道连接
 */
public class PoolServer {

    ExecutorService pool = Executors.newFixedThreadPool(50);

    private Selector selector;
    //中文测试

    /**
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        PoolServer server = new PoolServer();
        server.initServer(8000);
        server.listen();
    }

    /**
     *
     * @param port
     * @throws IOException
     */
    public void initServer(int port) throws IOException {
        // NIO通道的概念
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        // 设置通道类型为非阻塞
        serverChannel.configureBlocking(false);
        // 绑定服务端ip和端口号，此处ip缺省
        serverChannel.socket().bind(new InetSocketAddress(port));
        // 开启多路复用器selector
        this.selector = Selector.open();
        // channel注册selector，添加accept监听事件（The interest set for the resulting key）
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端启动成功！");
    }

    /**
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public void listen() throws IOException {
        // 轮询访问selector  
        while (true) {
            // 此处为阻塞方法，selector循环监听所有客户端事件，但并非IO的两个阶段（1、IO准备阶段，2、数据拷贝阶段）
            selector.select();
            // 获取selector的所有监听事件集合（The selected-key set），轮循，触发时处理并从监听集合中移除该事件
            Iterator ite = this.selector.selectedKeys().iterator();
            while (ite.hasNext()) {
                SelectionKey key = (SelectionKey) ite.next();
                ite.remove();
                // 客户端链接事件（accept事件）
                if (key.isAcceptable()) {
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    // 客户端与服务端建立通道
                    SocketChannel channel = server.accept();
                    // 设置通道类型为非阻塞
                    channel.configureBlocking(false);
                    // 通道建立后，此channel再次注册selector，添加读数据监听事件（read监听事件）
                    channel.register(this.selector, SelectionKey.OP_READ);
                    // 读数据事件（read事件）
                } else if (key.isReadable()) {
                    // ?
                    key.interestOps(key.interestOps()&(~SelectionKey.OP_READ));
                    // 放入线程池中处理具体数据读写事件
                    pool.execute(new ThreadHandlerChannel(key));
                }
            }
        }
    }
}

/**
 *
 * @param
 * @throws IOException
 */
class ThreadHandlerChannel extends Thread{
    private SelectionKey key;
    ThreadHandlerChannel(SelectionKey key){
        this.key=key;
    }
    @Override
    public void run() {
        //
        SocketChannel channel = (SocketChannel) key.channel();
        //
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        //
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            int size = 0;
            while ((size = channel.read(buffer)) > 0) {
                buffer.flip();
                baos.write(buffer.array(),0,size);
                buffer.clear();
            }
            baos.close();
            //
            byte[] content=baos.toByteArray();
            ByteBuffer writeBuf = ByteBuffer.allocate(content.length);
            writeBuf.put(content);
            writeBuf.flip();
            channel.write(writeBuf);//
            if(size==-1){

                channel.close();
            }else{
                //
                key.interestOps(key.interestOps()|SelectionKey.OP_READ);
                key.selector().wakeup();
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
