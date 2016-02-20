package com.monklu.bluetooth;

/**
 * Created by montuxxx on 16/02/16.
 */
public class BluetoothDeviceItem {
    private String name = "";
    private String address = "";
    private boolean connectStatus;

    public BluetoothDeviceItem(String name, String address, boolean connectStatus) {
        this.name = name;
        this.address = address;
        this.connectStatus = connectStatus;

    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(boolean connectStatus) {
        this.connectStatus = connectStatus;
    }
}
