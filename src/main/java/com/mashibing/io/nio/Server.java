package com.mashibing.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 单线程NIO多路复用
 */
public class Server {
    public static void main(String[] args) throws IOException {
        // 构建NIO通道
        ServerSocketChannel ssc = ServerSocketChannel.open();
        // 绑定服务端ip和端口号
        ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8888));
        ssc.configureBlocking(false); // 设置通道类型为非阻塞

        System.out.println("server started, listening on :" + ssc.getLocalAddress());
        Selector selector = Selector.open(); // 开启多路复用器selector
        ssc.register(selector, SelectionKey.OP_ACCEPT); // channel注册selector，添加accept监听事件（The interest set for the resulting key）

        // selector轮询监听事件
        while(true) {
            selector.select(); // 此处为阻塞方法，selector循环监听所有客户端事件，但并非IO的两个阶段（1、IO准备阶段，2、数据拷贝阶段）
            // 获取selector的所有监听事件集合（The selected-key set），轮循，触发时处理并从监听集合中移除该事件
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while(it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                handle(key);
            }
        }

    }

    private static void handle(SelectionKey key) {
        // 客户端链接事件（accept事件）
        if(key.isAcceptable()) {
            try {
                /**
                 * Returns the channel for which this key was created.  This method will
                 * continue to return the channel even after the key is cancelled.
                 */
                ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                // 客户端与服务端建立通道
                SocketChannel sc = ssc.accept();
                // 设置通道类型为非阻塞
                sc.configureBlocking(false);
                //new Client
                //
                //String hostIP = ((InetSocketAddress)sc.getRemoteAddress()).getHostString();

			/*
			log.info("client " + hostIP + " trying  to connect");
			for(int i=0; i<clients.size(); i++) {
				String clientHostIP = clients.get(i).clientAddress.getHostString();
				if(hostIP.equals(clientHostIP)) {
					log.info("this client has already connected! is he alvie " + clients.get(i).live);
					sc.close();
					return;
				}
			}*/
			    // 通道建立后，此channel再次注册selector，添加read监听事件
                sc.register(key.selector(), SelectionKey.OP_READ );
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            // 读数据事件（read事件）
        } else if (key.isReadable()) { //flip
            SocketChannel sc = null;
            try {
                sc = (SocketChannel)key.channel();
                ByteBuffer buffer = ByteBuffer.allocate(512); // ByteBuffer非常不好用
                buffer.clear();
                int len = sc.read(buffer);

                if(len != -1) {
                    System.out.println(new String(buffer.array(), 0, len));
                }

                ByteBuffer bufferToWrite = ByteBuffer.wrap("HelloClient".getBytes());
                sc.write(bufferToWrite);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(sc != null) {
                    try {
                        sc.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
