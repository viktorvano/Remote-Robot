package eu.cyberpunktech.remoterobotcontroller;

import org.jetbrains.annotations.NotNull;

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
            if(in != null)
                in.close();

            if(socket != null)
                socket.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
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
            catch(Exception i)
            {
                System.out.println(i);
            }

            // close the connection
            try
            {
                if(in != null)
                    in.close();
                if(socket != null)
                    socket.close();
            }
            catch(Exception i)
            {
                i.printStackTrace();
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
