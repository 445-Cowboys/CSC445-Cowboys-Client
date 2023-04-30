package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.packets.GameRooms;
import com.csc445cowboys.guiwip.packets.Packet;
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
           }

       }catch (RuntimeException e){
           e.printStackTrace();
       } catch (IOException e) {
           throw new RuntimeException(e);
       } finally {
              try {
                channel.close();
              } catch (IOException e) {
                e.printStackTrace();
              }
       }
    }
}
