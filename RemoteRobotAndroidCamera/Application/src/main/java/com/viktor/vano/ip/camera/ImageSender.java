package com.viktor.vano.ip.camera;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ImageSender extends Thread{
    private int port;
    private byte[] data;
    private boolean run = true;
    private boolean send = false;
    private int timeout;

    public ImageSender(int port, int timeout)
    {
        this.port = port;
        this.timeout = timeout;
    }

    public void stopThread()
    {
        run = false;
    }

    @Override
    public void run() {
        super.run();
        while (run)
        {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(send)
            {
                send = false;
                try
                {
                    // need host and port, we want to connect to the ServerSocket at port 7777
                    Socket socket = new Socket();
                    socket.setSoTimeout(230);
                    socket.connect(new InetSocketAddress(Variables.stringIP, port), timeout);
                    System.out.println("Connected!");

                    // get the output stream from the socket.
                    OutputStream outputStream = socket.getOutputStream();
                    // create a data output stream from the output stream so we can send data through it
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    System.out.println("Sending string to the ServerSocket");

                    // write the message we want to send
                    InputStream inn = new ByteArrayInputStream(data);
                    dataOutputStream.writeInt(data.length);
                    int len = 0;
                    //dataOutputStream.write(data);
                    byte [] b = new byte [1024];
                    while ((len = inn.read(b)) != -1)
                    {
                        dataOutputStream.write(b,0,len);
                    }
                    dataOutputStream.flush(); // send the message
                    dataOutputStream.close(); // close the output stream when we're done.

                    System.out.println("Closing socket.");
                    socket.close();
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Server thread stopped.");
    }

    public void sendImageToServer(byte data[])
    {
        System.out.println("Array length to be send: " + data.length);
        this.data = data;
        send = true;
    }
}
