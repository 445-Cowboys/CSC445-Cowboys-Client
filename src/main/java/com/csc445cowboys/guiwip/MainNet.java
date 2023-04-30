package com.csc445cowboys.guiwip;

import javafx.scene.chart.PieChart;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class MainNet implements  Runnable{

    MainLobbyController mainLobbyController;
    ByteBuffer receivedData;
    DatagramChannel channel;
    SocketAddress sa;
    public MainNet(MainLobbyController mainLobbyController) throws IOException {
        this.mainLobbyController = mainLobbyController;
        receivedData = ByteBuffer.allocate(1024);
        channel = DatagramChannel.open().bind(null);
        channel.configureBlocking(true);

    }


    @Override
    public void run() {
        System.out.println("Running");
        // for
       try {
           for (;;){
               System.out.println("Waiting for packet, Timeout: " + channel.socket().getSoTimeout());


           }

       }catch (RuntimeException e){
           e.printStackTrace();
       } catch (SocketException e) {
           throw new RuntimeException(e);
       }
    }
}
