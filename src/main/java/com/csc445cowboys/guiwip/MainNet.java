package com.csc445cowboys.guiwip;

import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class MainNet implements  Runnable{

    ByteBuffer receivedData;
    DatagramChannel channel = DatagramChannel.open().bind(null);
    SocketAddress sa = channel.receive(receivedData);

    public MainNet(ByteBuffer buf, SocketAddress sa) throws IOException {
        this.receivedData = buf;
        this.sa = sa;
        this.receivedData.flip();
        // TODO IMPLEMENT Tink and decrypt the data
    }


    @Override
    public void run() {


    }
}
