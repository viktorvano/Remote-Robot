package com.viktor.vano.robot.controller;

import com.sun.istack.internal.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

class AndroidBatteryClient extends Thread{
    private boolean active = true;
    private boolean messageReceived = false;
    private int port;
    private String address;
    private String message;

    private Socket socket		 = null;
    private DataInputStream in	 = null;

    public AndroidBatteryClient(@NotNull String address, int port){
        this.port = port;
        this.address = address;
        this.message = "";
    }

    public void stopServer()
    {
        this.active = false;
        try
        {
            in.close();
            socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public boolean isMessageReceived()
    {
        return messageReceived;
    }

    public String getMessage(){
        messageReceived = false;
        return this.message;
    }

    @Override
    public void run() {
        super.run();
        while (active)
        {
            try
            {
                socket = new Socket(address, port);
                System.out.println("Android Battery Client Connected");
            }
            catch(UnknownHostException u)
            {
                System.out.println(u);
            }
            catch(IOException i)
            {
                System.out.println(i);
            }

            try
            {
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                this.message = in.readUTF();
            }
            catch(IOException i)
            {
                System.out.println(i);
            }

            // close the connection
            try
            {
                in.close();
                socket.close();
            }
            catch(IOException i)
            {
                System.out.println(i);
            }

            if(this.message != null && !this.message.equals(""))
                this.messageReceived = true;

            try
            {
                Thread.sleep(1000);
            }catch (Exception e)
            {
                System.out.println("Cannot sleep 1000 millis");
            }
        }
        System.out.println("Server stopped successfully.");
    }
}
