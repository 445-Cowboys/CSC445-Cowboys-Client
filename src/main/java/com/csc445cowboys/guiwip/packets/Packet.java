package com.csc445cowboys.guiwip.packets;

public abstract class Packet implements Runnable{
    public abstract int getOpcode();


    @Override
    public void run() {
        System.out.println("Packet received");
    }
}
