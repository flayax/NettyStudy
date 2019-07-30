package com.mashibing.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * 单线程异步IO，异步IO两阶段皆由操作系统内核完成，处理结果通过回调函数告知客户端
 */
public class Server {
    public static void main(String[] args) throws Exception {
        // 构建AIO通道，绑定服务端端口
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress(8888));
        // 客户端与服务端建立通道连接
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            // 通道建立成功处理逻辑
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                serverChannel.accept(null, this);
                try {
                    System.out.println(client.getRemoteAddress());
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    // 通道读写操作
                    client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {

                        // 读写操作成功处理逻辑
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            System.out.println(new String(attachment.array(), 0, result));
                            client.write(ByteBuffer.wrap("HelloClient".getBytes()));
                        }

                        // 读写操作失败处理逻辑
                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 通道建立失败处理逻辑
            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();
            }
        });

        // 此处死循环是防止主线程直接结束
        while (true) {
            Thread.sleep(1000);
        }

    }
}
