package com.csc445cowboys.guiwip.Net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public class Heartbeat implements Runnable {

    ByteBuffer ack;
    DatagramChannel channel;
    SocketAddress sa;

    public Heartbeat(SocketAddress sa) throws IOException {
        try {
            this.ack = ByteBuffer.allocate(4);
            ack.put((byte) -1);
            ack.flip();
            this.sa = sa;
            channel = DatagramChannel.open().bind(null);
        } catch (IOException e) {
            System.out.println("Issue creating heartbeat");
        }
    }
    @Override
    public void run() {
        for (;;){
            try {
                channel.send(ack, sa);
                Thread.sleep(1000*60);
            } catch (IOException | InterruptedException e) {
                System.out.println("Issue sending heartbeat");
            }
        }
    }
}
