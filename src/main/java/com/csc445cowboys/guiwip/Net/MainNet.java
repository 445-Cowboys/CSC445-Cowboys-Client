package com.csc445cowboys.guiwip.Net;

import com.csc445cowboys.guiwip.Controllers.MainLobbyController;
import com.csc445cowboys.guiwip.packets.GameRooms;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
//        channel.configureBlocking(true);
        sa = new InetSocketAddress("localhost", 7806);
    }

    public void sendAwake() throws IOException {
        ByteBuffer sendData = ByteBuffer.allocate(4);
        sendData.putInt(20);
        sendData.flip();
        channel.send(sendData, sa);
    }


    // Listens for updates on game rooms
    @Override
    public void run() {
        // for
       try {
           for (;;){
               channel.receive(receivedData);
               receivedData.flip();
               int opcode = receivedData.getInt();
               // Opcode 6 is for game rooms
               if (opcode == 6) {
                   GameRooms gameRooms = new GameRooms(receivedData);
                   mainLobbyController.setGameRooms(gameRooms);
               }


               Thread.sleep(1000);
           }

       }catch (RuntimeException | InterruptedException | IOException e){
           e.printStackTrace();
       }
    }
}
