package com.viktor.vano.ip.camera;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class CameraServer extends Thread{
    private boolean active = true;
    private int port = 0;

    //initialize socket and input stream
    private Socket		 socket = null;
    private ServerSocket server = null;
    private DataInputStream in	 = null;
    private DataOutputStream out	 = null;
    private byte[] data;

    public void stopServer(){
        this.active = false;
        try {
            // close connection
            if(socket!=null)
                socket.close();

            if(server!=null)
                server.close();

            if(in!=null)
                in.close();

            if(out!=null)
                out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CameraServer(int port){
        this.port = port;
    }

    @Override
    public void run() {
        super.run();
        while (active)
        {
            socket = null;
            server = null;
            in	 = null;
            out	 = null;
            try
            {
                server = new ServerSocket(port);
                System.out.println("Server started");

                System.out.println("Waiting for a client ...");

                socket = server.accept();
                System.out.println("Client accepted");

                try
                {
                    // get the output stream from the socket.
                    OutputStream outputStream = socket.getOutputStream();
                    // create a data output stream from the output stream so we can send data through it
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    System.out.println("Sending string to the ServerSocket");

                    // write the message we want to send
                    if(data != null)
                    {
                        InputStream inn = new ByteArrayInputStream(data);
                        dataOutputStream.writeInt(data.length);
                        int len = 0;
                        //dataOutputStream.write(data);
                        byte[] b = new byte[1024];
                        while ((len = inn.read(b)) != -1) {
                            dataOutputStream.write(b, 0, len);
                        }
                        dataOutputStream.flush(); // send the message
                        dataOutputStream.close(); // close the output stream when we're done.
                    }
                }
                catch(Exception e)
                {
                    System.out.println(e);
                }
                System.out.println("Closing connection");

                // close connection
                if(socket!=null)
                    socket.close();

                if(server!=null)
                    server.close();

                if(in!=null)
                    in.close();

                if(out!=null)
                    out.close();
            }
            catch(IOException i)
            {
                System.out.println("Some problem with the server:");
                System.out.println(i);
            }
        }
        System.out.println("Server stopped successfully.");
    }

    public void updateImage(byte data[])
    {
        System.out.println("Image size: " + data.length);
        this.data = data;
    }
}
