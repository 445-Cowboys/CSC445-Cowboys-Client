package com.csc445cowboys.guiwip.packets;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public abstract class Packet {
    public abstract int getOpcode();

}
