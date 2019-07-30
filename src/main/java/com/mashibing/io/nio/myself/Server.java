package com.mashibing.io.nio.myself;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class Server {
    /*
     * 处理器类
     */
    static class Handler {
        private int bufferSize = 1024; // 缓冲器容量
        private String localCharset = "UTF-8"; // 编码格式

        public Handler() {
        }

        public Handler(int bufferSize) {
            this(bufferSize, null);
        }

        public Handler(String localCharset) {
            this(-1, localCharset);
        }

        public Handler(int bufferSize, String localCharset) {
            if (bufferSize > 0) {
                this.bufferSize = bufferSize;
            }
            if (localCharset != null) {
                this.localCharset = localCharset;
            }
        }

        /*
         * 连接请求处理方法
         */
        public void handleAccept(SelectionKey selectionKey) throws IOException {
            // 通过选择器键获取服务器套接字通道，通过accept()方法获取套接字通道连接
            SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
            // 设置套接字通道为非阻塞模式
            socketChannel.configureBlocking(false);
            // 为套接字通道注册选择器，该选择器为服务器套接字通道的选择器，即选择到该SocketChannel的选择器
            // 设置选择器关心请求为读操作，设置数据读取的缓冲器容量为处理器初始化时候的缓冲器容量
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(bufferSize));
        }

        public void handleRead(SelectionKey selectionKey) throws IOException {
            // 获取套接字通道
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            // 获取缓冲器并进行重置,selectionKey.attachment()为获取选择器键的附加对象
            ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
            byteBuffer.clear();
            // 没有内容则关闭通道
            if (socketChannel.read(byteBuffer) == -1) {
                socketChannel.close();
            } else {
                // 将缓冲器转换为读状态
                byteBuffer.flip();
                // 将缓冲器中接收到的值按localCharset格式编码保存
                String receivedRequestData = Charset.forName(localCharset).newDecoder().decode(byteBuffer).toString();
                System.out.println("接收到客户端的请求数据：" + receivedRequestData);
                // 返回响应数据给客户端
                String responseData = "已接收到你的请求数据，响应数据为：(响应数据)";
                byteBuffer = ByteBuffer.wrap(responseData.getBytes(localCharset));
                socketChannel.write(byteBuffer);
                // 关闭通道
                socketChannel.close();
            }
        }
    }

    public static void main(String[] args) {
        try {
            // 创建ServerSocketChannel通道，绑定监听端口为8080
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(8080));
            // 设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 注册选择器,设置选择器选择的操作类型
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            // 创建处理器
            Handler handler = new Handler(1204);
            while (true) {
                // 等待请求，每次等待阻塞3s，超过时间则向下执行，若传入0或不传值，则在接收到请求前一直阻塞
                if (selector.select(3000) == 0) {
                    System.out.println("等待请求超时......");
                    continue;
                }
                System.out.println("-----处理请求-----");
                // 获取待处理的选择键集合
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    try {
                        // 如果是连接请求，调用处理器的连接处理方法
                        if (selectionKey.isAcceptable()) {
                            handler.handleAccept(selectionKey);
                        }
                        // 如果是读请求，调用对应的读方法
                        if (selectionKey.isReadable()) {
                            handler.handleRead(selectionKey);
                        }
                    } catch (IOException e) {
                        keyIterator.remove();
                        continue;
                    }
                }
                // 处理完毕从待处理集合移除该选择键
                keyIterator.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}