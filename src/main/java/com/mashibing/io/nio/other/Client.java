package com.mashibing.io.nio.other;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 8080));

        String newData = "New String to write to file..." + System.currentTimeMillis();

        ByteBuffer buf = ByteBuffer.allocate(48);
        buf.clear();
        buf.put(newData.getBytes());

        buf.flip();

        while (buf.hasRemaining()) {
            socketChannel.write(buf);
        }

//        socketChannel.configureBlocking(false);
//        socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));
//
//        while(! socketChannel.finishConnect() ){
//            //wait, or do something else...    
//        }
        
        socketChannel.close();
    }
}