package com.csc445cowboys.guiwip.packets;

import java.nio.ByteBuffer;

public class CourtesyLeave extends Packet {

    private final byte[] data;

    private final int opcode;
    private final int portNum;

    public CourtesyLeave(ByteBuffer buffer){

        //This code readies the bytebuffer data to be read
        int totalLength = buffer.limit();
        this.data = new byte[totalLength];
        buffer.get(data, 0, data.length);
        buffer.rewind();

        //This pulls the only byte of the array to the opcode constant
        this.opcode = buffer.get(0);
        this.portNum = buffer.getInt(1);
    }

    @Override
    public int getOpcode(){
        return -5;
    }

    public int getPortNum() {
        return portNum;
    }
}
