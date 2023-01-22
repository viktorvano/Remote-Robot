package com.viktor.vano.ip.camera;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StringSenderServer extends Thread{
    private int port;
    private boolean run = true;
    private String message;

    private Socket socket = null;
    private ServerSocket server = null;
    private DataOutputStream out = null;

    public StringSenderServer(int port){
        this.port = port;
        message = "";
    }

    public void stopServer(){
        this.run = false;
        // close connection
        try{
            if(socket!=null)
                socket.close();
        }catch (Exception e)
        {
            System.out.println(e);
        }

        try{
            if(server!=null)
                server.close();
        }catch (Exception e)
        {
            System.out.println(e);
        }

        try{
            if(out!=null)
                out.close();
        }catch (Exception e)
        {
            System.out.println(e);
        }
    }

    @Override
    public void run() {
        super.run();
        while (run)
        {
            socket = null;
            server = null;
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
                    try
                    {
                        Thread.sleep(5);
                    }catch (Exception e)
                    {
                        System.out.println("Cannot sleep 5 millis");
                    }
                    out = new DataOutputStream(socket.getOutputStream());
                    out.writeUTF(this.message);
                }
                catch(IOException i)
                {
                    System.out.println(i);
                }
                System.out.println("Closing connection");

                // close connection
                try{
                    if(socket!=null)
                        socket.close();
                }catch (Exception e)
                {
                    System.out.println(e);
                }

                try{
                    if(server!=null)
                        server.close();
                }catch (Exception e)
                {
                    System.out.println(e);
                }

                try{
                    if(out!=null)
                        out.close();
                }catch (Exception e)
                {
                    System.out.println(e);
                }
            }
            catch(IOException i)
            {
                System.out.println("Some problem with the server:");
                System.out.println(i);
            }
        }
        System.out.println("StringSender server thread stopped.");
    }

    public void updateResponseMessage(String message){
        this.message = message;
    }
}
