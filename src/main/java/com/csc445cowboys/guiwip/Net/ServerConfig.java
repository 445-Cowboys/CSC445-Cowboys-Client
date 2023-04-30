package com.csc445cowboys.guiwip.Net;

public final class ServerConfig {

    public final String SERVER1_NAME;
    public final String SERVER1_IP;
    public final int SERVER1_PORT;
    public final String SERVER2_NAME;
    public final String SERVER2_IP;
    public final int SERVER2_PORT;
    public final String SERVER3_NAME;
    public final String SERVER3_IP;
    public final int SERVER3_PORT;

    public ServerConfig() {

        String[] serverNames = {
                "Moxie",
                "Pi",
                "Rho"
        };
        String[] serverIPs = {
                "https://moxie.cs.oswego.edu",
                "https://pi.cs.oswego.edu",
                "https://rho.cs.oswego.edu"
        };
        int[] serverPorts = {
                7806,
                7806,
                7806
        };

        SERVER1_NAME = serverNames[0];
        SERVER1_IP = serverIPs[0];
        SERVER1_PORT = serverPorts[0];
        SERVER2_NAME = serverNames[1];
        SERVER2_IP = serverIPs[1];
        SERVER2_PORT = serverPorts[1];
        SERVER3_NAME = serverNames[2];
        SERVER3_IP = serverIPs[2];
        SERVER3_PORT = serverPorts[2];
    }
}
