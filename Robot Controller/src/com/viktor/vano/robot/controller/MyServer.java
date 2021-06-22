package com.viktor.vano.robot.controller;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

class MyServer extends Thread{
    private boolean active = true;
    private boolean messageReceived = false;
    private ServerSocket ss;
    private int port;
    private String message;

    public MyServer(int port){
        this.port = port;
        message = "";
    }

    public void stopServer()
    {
        this.active = false;
        try {
            ss.close();
        }catch (Exception e)
        {
            e.printStackTrace();
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
        ss = null;
        try {
            ss = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
            active = false;
        }

        if (ss == null)
        {
            System.exit(-99);
        }
        System.out.println("ServerSocket awaiting connections...");
        Socket socket = null;// = ss.accept(); // blocking call, this will wait until a connection is attempted on this port.
        while (active)
        {
            try {
                socket = ss.accept(); // blocking call, this will wait until a connection is attempted on this port.
                System.out.println("Connection from " + socket + "!");

                // get the input stream from the connected socket
                InputStream inputStream = null;
                try {
                    assert socket != null;
                    inputStream = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // create a DataInputStream so we can read data from it.
                assert inputStream != null;
                DataInputStream dataInputStream = new DataInputStream(inputStream);

                // read the message from the socket
                try {
                    message = dataInputStream.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Received message:\n" + message + "\n");

                if(message != null && !message.equals(""))
                    messageReceived = true;
            }catch (SocketTimeoutException e)
            {
                System.out.println("Socket timed out");
            } catch (Exception e) {
                System.out.println("Something went wrong.");
                e.printStackTrace();
            }
        }
        System.out.println("Server stopped successfully.");
    }
}
