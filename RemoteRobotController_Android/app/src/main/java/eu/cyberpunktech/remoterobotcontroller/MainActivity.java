package eu.cyberpunktech.remoterobotcontroller;

import static eu.cyberpunktech.remoterobotcontroller.Variables.*;

import androidx.appcompat.app.AppCompatActivity;


import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private Button buttonForward, buttonBackward, buttonRight, buttonLeft;
    private String messageOut = "--FF0\n";
    private STM32Status stm32Status;
    private ClientSender stm32ClientRemoteControl;
    private AndroidCamera myAndroidCamera;
    private AndroidBatteryClient androidBatteryClient;
    private int length = 0;
    private boolean updateImage = false;
    private byte [] byteArray;
    private TextView textViewVersion, textViewAndroidBattery, textViewSTM32Status;
    private ImageView imageViewCamera;
    private Switch switchLights, switchDrivingAssistance;
    private SeekBar seekBarSpeed;
    private int speed = 51;
    private Button buttonSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        buttonForward = findViewById(R.id.buttonForward);
        buttonBackward = findViewById(R.id.buttonBackward);
        buttonRight = findViewById(R.id.buttonRight);
        buttonLeft = findViewById(R.id.buttonLeft);
        imageViewCamera = findViewById(R.id.imageView);

        textViewVersion = findViewById(R.id.textViewVersion);
        textViewVersion.setText(version);

        textViewAndroidBattery = findViewById(R.id.textViewAndroidBattery);
        textViewSTM32Status = findViewById(R.id.textViewSTM32Status);
        switchLights = findViewById(R.id.switchLights);
        switchDrivingAssistance = findViewById(R.id.switchDrivingAssistance);
        seekBarSpeed = findViewById(R.id.seekBarSpeed);
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                speed = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        buttonSettings = findViewById(R.id.buttonSettings);
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });

        stm32ClientRemoteControl = new ClientSender(stringSTM32IP, 80, 1800);
        stm32ClientRemoteControl.start();

        stm32Status = new STM32Status(stm32StatusUpdatePeriod, stringSTM32IP);
        stm32Status.start();

        myAndroidCamera = new AndroidCamera(cameraPort);
        myAndroidCamera.start();

        androidBatteryClient = new AndroidBatteryClient(stringAndroidIP,cameraPort+1);
        androidBatteryClient.start();

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                updateImage();
                if(androidBatteryClient.isMessageReceived())
                {
                    textViewAndroidBattery.setText(androidBatteryClient.getMessage());
                }
                handler.postDelayed(this, 10);
            }
        };
        handler.postDelayed(r, 10);

        Handler handler2 = new Handler();
        final Runnable r2 = new Runnable() {
            public void run() {
                if(stm32Status.isMessageAvailable())
                {
                    String message = stm32Status.getStm32Message();
                    textViewSTM32Status.setText(message);
                }
                sendRemoteCommand();
                handler2.postDelayed(this, 200);
            }
        };
        handler2.postDelayed(r2, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myAndroidCamera.stopServer();
        androidBatteryClient.stopServer();
        stm32ClientRemoteControl.stopClient();
        stm32Status.stopSTM32Status();
        System.out.println("Closing the application.");
    }

    private void sendRemoteCommand()
    {
        messageOut = "";

        if(buttonForward.isPressed())
            messageOut += "F";
        else if(buttonBackward.isPressed())
            messageOut += "B";
        else
            messageOut += "-";

        if(buttonRight.isPressed())
            messageOut += "R";
        else if(buttonLeft.isPressed())
            messageOut += "L";
        else
            messageOut += "-";

        if(switchDrivingAssistance.isChecked())
            messageOut += "T";
        else
            messageOut += "F";

        if(switchLights.isChecked())
            messageOut += "T";
        else
            messageOut += "F";

        messageOut += speed + "\n";

        stm32ClientRemoteControl.sendDataToServer(messageOut);
    }

    private void updateImage()
    {
        if(updateImage)
        {
            updateImage = false;
            System.out.println("some data: " + length);
            try{
                Bitmap bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArray));
                imageViewCamera.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    class AndroidCamera extends Thread{
        private boolean active = true;
        private int port = 0;

        // initialize socket and input output streams
        private Socket socket		 = null;
        private DataInputStream in	 = null;

        public void stopServer(){
            this.active = false;
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public AndroidCamera(int port){
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("ServerSocket awaiting connections...");
            while (active)
            {
                getData(stringAndroidIP, port);
                try
                {
                    Thread.sleep(50);
                }catch (Exception e)
                {
                    System.out.println("Cannot sleep 50 millis");
                }
            }
            System.out.println("Server stopped successfully.");
        }

        public void getData(String address, int port)
        {
            // establish a connection
            try
            {
                socket = new Socket(address, port);
                System.out.println("Connected");
            }
            catch(Exception u)
            {
                System.out.println(u);
            }

            try
            {
                in = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                // read the message from the socket
                try
                {
                    //length = dataInputStream.read(data);
                    length = in.readInt();
                    System.out.println("Got the Size: " + length);
                    int bytesRead ;
                    int len = 0;
                    byte[] buffer = new byte[1000000];
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    while (len<length)
                    {
                        bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, length-len));
                        len = len + bytesRead;
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }
                    byteArray = byteArrayOutputStream.toByteArray();
                    System.out.println("PHOTO DATA LENGTH: " + byteArray.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(length > 5000)
                    updateImage = true;

            }
            catch(Exception i)
            {
                System.out.println(i);
            }

            // close the connection
            try
            {
                //input.close();
                in.close();
                socket.close();
            }
            catch(Exception i)
            {
                System.out.println(i);
            }
        }
    }
}