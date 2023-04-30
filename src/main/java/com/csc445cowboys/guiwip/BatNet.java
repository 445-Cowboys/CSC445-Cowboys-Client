package com.csc445cowboys.guiwip;

import com.csc445cowboys.guiwip.packets.GameState;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.security.GeneralSecurityException;

public class BatNet implements Runnable {

    ByteBuffer buf;
    DatagramChannel channel = DatagramChannel.open().bind(null);
    SocketAddress serverAddr;
    AEAD aead;


    BattleScreenController battleScreenController;
    public BatNet(BattleScreenController battleScreenController, ByteBuffer buf, SocketAddress sa) throws IOException, GeneralSecurityException {
        this.buf = buf.flip();
        this.serverAddr = sa;
        this.battleScreenController = battleScreenController;

    }


}
