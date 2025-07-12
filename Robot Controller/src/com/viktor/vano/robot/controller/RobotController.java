package com.viktor.vano.robot.controller;

import com.sun.jna.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.hid4java.*;
import org.hid4java.event.HidServicesEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.viktor.vano.robot.controller.FileManager.readOrCreateFile;
import static com.viktor.vano.robot.controller.Variables.*;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class RobotController extends Application implements HidServicesListener{
    private static final String version = "20250712";
    private Pane pane;
    private int length = 0;
    private Label androidLabel;
    private Timeline timeline, timelineSend;
    private AndroidCamera myAndroidCamera;
    private AndroidBatteryClient androidBatteryClient;
    private ImageView imageViewCamera;
    private Image imageCamera;
    private boolean updateImage = false;
    private final int cameraPort = 7770;
    private boolean forward = false;
    private boolean backward = false;
    private boolean right = false;
    private boolean left = false;
    private Slider slider;
    private int speed = 0;
    private Label labelSpeed;
    private Label labelDirection;
    private Label labelSTM32Status;
    private String messageOut = "--F0\n";
    private ClientSender stm32ClientRemoteControl;
    private ImageView imageViewCarLogo;
    private ProgressBar[] progressBarsDistance;
    private STM32Status stm32Status;
    private CheckBox checkBoxDrivingAssistance;
    private CheckBox checkBoxLights;
    private byte [] byteArray;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    int[] fanatecPids = {0xABC1, 0xABC2, 0xDEF3, 0x0EB7, 0x0020};  // List of Fanatec Product IDs you want
    int fanatecVid = 0x0EB7;   // Example Fanatec VID (replace with yours)

    private final ArrayList<HidDevice> fanatecDevices = new ArrayList<>();

    private float steeringWheel = 0.0f;
    private int gas = 0;
    private int brake = 0;
    private int gear = 1;// 1 is forward
    private boolean seqUp;
    private boolean seqDown;
    private final int upperGearLimit = 1;// 1 is forward
    private final int lowerGearLimit = 0;// 0 is backward

    @Override
    public void start(Stage stage){
        final int width = 1600;
        final int height = 1000;

        stringSTM32IP = readOrCreateFile("IP_STM32.txt");
        stringAndroidIP = readOrCreateFile("AndroidIP.txt");

        pane = new Pane();

        stm32ClientRemoteControl = new ClientSender(stringSTM32IP, 80, 1800);
        stm32ClientRemoteControl.start();

        stm32Status = new STM32Status(stm32StatusUpdatePeriod, stringSTM32IP);
        stm32Status.start();

        androidLabel = new Label("");
        androidLabel.setFont(Font.font("Arial", 24));
        androidLabel.setStyle("-fx-background-color: #FFFFFF");
        androidLabel.setLayoutX(1300);
        androidLabel.setLayoutY(50);
        pane.getChildren().add(androidLabel);

        Scene scene = new Scene(pane, width, height);

        stage.setTitle("Robot Controller " + version);
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
        stage.setMaxWidth(stage.getWidth());
        stage.setMaxHeight(stage.getHeight());
        stage.setResizable(false);

        imageViewCamera = new ImageView();
        imageViewCamera.setFitWidth(900);
        imageViewCamera.setFitHeight(1200);
        imageViewCamera.setRotate(270);
        imageViewCamera.setLayoutX(200);
        imageViewCamera.setLayoutY(-100);
        imageViewCamera.setPreserveRatio(true);
        pane.getChildren().add(imageViewCamera);

        myAndroidCamera = new AndroidCamera(cameraPort);
        myAndroidCamera.start();

        androidBatteryClient = new AndroidBatteryClient(stringAndroidIP,cameraPort+1);
        androidBatteryClient.start();

        try{
            imageViewCarLogo = new ImageView(new Image("com/viktor/vano/robot/controller/images/car.jpg"));
            imageViewCarLogo.setPreserveRatio(true);
            imageViewCarLogo.setFitHeight(200);
            imageViewCarLogo.setFitWidth(100);
            imageViewCarLogo.setLayoutX(1375);
            imageViewCarLogo.setLayoutY(750);
            pane.getChildren().add(imageViewCarLogo);
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("Couldn't load car.png");
        }

        progressBarsDistance = new ProgressBar[5];
        for (int i=0; i<5; i++)
        {
            progressBarsDistance[i] = new ProgressBar();
            progressBarsDistance[i].setLayoutX(1275+50*i);
            progressBarsDistance[i].setLayoutY(750 - 75*Math.sin( (i)*(Math.PI/4) ));
            progressBarsDistance[i].setRotate(180+45*i);
            pane.getChildren().add(progressBarsDistance[i]);
        }


        slider = new Slider();
        slider.setValue(speed/2.55);
        slider.valueProperty().addListener(observable -> {
            speed = (int)Math.round(slider.getValue()*2.55);
            labelSpeed.setText("Speed: " + (int)((double)speed/2.55) + "%");
            System.out.println("Slider Value: " + slider.getValue());
        });
        slider.setLayoutX(1400);
        slider.setLayoutY(300);
        slider.setValue((double)speed/2.55);
        slider.setPrefWidth(250);
        slider.setRotate(270);
        pane.getChildren().add(slider);

        labelSpeed = new Label("Speed: " + (int)((double)speed/2.55) + "%");
        labelSpeed.setLayoutX(1400);
        labelSpeed.setLayoutY(330);
        pane.getChildren().add(labelSpeed);

        labelDirection = new Label("Forward: " + forward +
                                        "\nBackward: " + backward +
                                        "\nRight: " + right +
                                        "\nLeft: " + left);
        labelDirection.setLayoutX(1400);
        labelDirection.setLayoutY(500);
        pane.getChildren().add(labelDirection);

        labelSTM32Status = new Label("");
        labelSTM32Status.setLayoutX(1300);
        labelSTM32Status.setLayoutY(100);
        labelSTM32Status.setFont(Font.font("Arial", 20));
        pane.getChildren().add(labelSTM32Status);

        checkBoxDrivingAssistance = new CheckBox("Driving Assistance");
        checkBoxDrivingAssistance.setLayoutX(1300);
        checkBoxDrivingAssistance.setLayoutY(200);
        pane.getChildren().add(checkBoxDrivingAssistance);

        checkBoxLights = new CheckBox("Lights");
        checkBoxLights.setLayoutX(1300);
        checkBoxLights.setLayoutY(260);
        pane.getChildren().add(checkBoxLights);

        //changePaneColor("000000");
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode keyCode = event.getCode();
                if(keyCode == KeyCode.W)
                {
                    forward = true;
                    backward = false;
                }

                if(keyCode == KeyCode.S)
                {
                    backward = true;
                    forward = false;
                }

                if(keyCode == KeyCode.A)
                {
                    left = true;
                    right = false;
                }

                if(keyCode == KeyCode.D)
                {
                    right = true;
                    left = false;
                }

                if(keyCode == KeyCode.R)
                {
                    checkBoxDrivingAssistance.setSelected(!checkBoxDrivingAssistance.isSelected());
                }

                if(keyCode == KeyCode.L)
                {
                    checkBoxLights.setSelected(!checkBoxLights.isSelected());
                }

                if(keyCode == KeyCode.DIGIT1)
                    slider.setValue(10.0);

                if(keyCode == KeyCode.DIGIT2)
                    slider.setValue(20.0);

                if(keyCode == KeyCode.DIGIT3)
                    slider.setValue(30.0);

                if(keyCode == KeyCode.DIGIT4)
                    slider.setValue(40.0);

                if(keyCode == KeyCode.DIGIT5)
                    slider.setValue(50.5);

                if(keyCode == KeyCode.DIGIT6)
                    slider.setValue(60.0);

                if(keyCode == KeyCode.DIGIT7)
                    slider.setValue(70.0);

                if(keyCode == KeyCode.DIGIT8)
                    slider.setValue(80.0);

                if(keyCode == KeyCode.DIGIT9)
                    slider.setValue(90.5);

                if(keyCode == KeyCode.DIGIT0)
                    slider.setValue(100.0);
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                KeyCode keyCode = event.getCode();
                if(keyCode == KeyCode.W)
                {
                    forward = false;
                    backward = false;
                }

                if(keyCode == KeyCode.S)
                {
                    backward = false;
                    forward = false;
                }

                if(keyCode == KeyCode.A)
                {
                    left = false;
                    right = false;
                }

                if(keyCode == KeyCode.D)
                {
                    right = false;
                    left = false;
                }
            }
        });

        // Configure to use custom specification
        HidServicesSpecification hidServicesSpecification = new HidServicesSpecification();

        // Use the v0.7.0 manual start feature to get immediate attach events
        hidServicesSpecification.setAutoStart(false);

        // Get HID services using custom specification
        HidServices hidServices = HidManager.getHidServices(hidServicesSpecification);
        hidServices.addHidServicesListener(this);

        // Manually start the services to get attachment event
        hidServices.start();

        // Provide a list of attached devices
        /*System.out.println("Listing all devices:");
        for (HidDevice hidDevice : hidServices.getAttachedHidDevices()) {
            System.out.println(hidDevice);
        }*/

        System.out.println("Listing Fanatec devices:");
        for (HidDevice device : hidServices.getAttachedHidDevices()) {
            if (device.getVendorId() == fanatecVid) {
                for (int pid : fanatecPids) {
                    if (device.getProductId() == pid) {
                        System.out.println("✅ Found Fanatec Device: " + device);
                        fanatecDevices.add(device);
                    }
                }
            }
        }

        for(HidDevice fanDevice : fanatecDevices)
        {
            if (fanDevice != null && !fanDevice.isOpen())
            {
                fanDevice.open();
            }
        }

        timeline = new Timeline(new KeyFrame(Duration.millis(10), event ->{
            updateImage();
            if(androidBatteryClient.isMessageReceived())
            {
                androidLabel.setText(androidBatteryClient.getMessage());
            }

            labelDirection.setText("Forward: " + forward +
                    "\nBackward: " + backward +
                    "\nRight: " + right +
                    "\nLeft: " + left);
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        //timeline.play();

        timelineSend = new Timeline(new KeyFrame(Duration.millis(200), event ->{
            if(stm32Status.isMessageAvailable())
            {
                String message = stm32Status.getStm32Message();
                System.out.println("MESSAGE IS AVAILABLE: " + message);
                labelSTM32Status.setText(message);
                try
                {
                    String[] strings = message.split("Distances: ");
                    strings[1] = strings[1].substring(0, strings[1].length()-1);
                    strings = strings[1].split(",");
                    for(int i=0; i<progressBarsDistance.length; i++)
                        progressBarsDistance[i].setProgress(Double.parseDouble(strings[i])/distanceProgressRange);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            readFanatecSimulator();

            messageOut = "";

            if(forward)
                messageOut += "F";
            else if(backward)
                messageOut += "B";
            else
                messageOut += "-";

            if(right)
                messageOut += "R";
            else if(left)
                messageOut += "L";
            else
                messageOut += "-";

            if(checkBoxDrivingAssistance.isSelected())
                messageOut += "T";
            else
                messageOut += "F";

            if(checkBoxLights.isSelected())
                messageOut += "T";
            else
                messageOut += "F";

            messageOut += speed + "\n";

            stm32ClientRemoteControl.sendDataToServer(messageOut);
        }));
        timelineSend.setCycleCount(Timeline.INDEFINITE);
        timelineSend.play();
    }

    private void readFanatecSimulator()
    {
        byte[] buffer = new byte[64];  // Adjust buffer if needed
        for (HidDevice fanDevice : fanatecDevices)
        {
            if (fanDevice != null && fanDevice.isOpen())
            {
                try {
                    int val = fanDevice.read(buffer, 100);
                    if (val > 0)
                    {
                        System.out.println("✅ Data from device: " + fanDevice);
                        System.out.print("Received Data: ");
                        for (int i = 0; i < val; i++)
                        {
                            //System.out.printf("0x%02X ", buffer[i]);
                            System.out.printf("%d ", buffer[i] & 0xFF);
                        }
                        System.out.println();
                        if (val >= 19)
                        {
                            short steeringValue = (short)(((buffer[18] & 0xFF) << 8) | (buffer[17] & 0xFF));
                            float centered_value = 0;
                            if(steeringValue > 0)
                            {
                                centered_value = 1.0f - (steeringValue / 32767.0f);
                            }else if(steeringValue < 0)
                            {
                                centered_value = -(1.0f + (steeringValue / 32767.0f));
                            }
                            steeringWheel = centered_value;
                            System.out.println("Steering wheel: " + steeringWheel);
                            gas = 255 - (buffer[20] & 0xFF);
                            System.out.println("Gas Pedal: " + gas);
                            if((buffer[2] & 0xFF) == 1 && !seqUp)
                            {
                                seqUp = true;
                            }else if((buffer[2] & 0xFF) == 2 && !seqDown)
                            {
                                seqDown = true;
                            }

                            if((buffer[2] & 0xFF) == 0 && seqUp)
                            {
                                if(gear < upperGearLimit)
                                {
                                    gear++;
                                }
                                seqUp = false;
                            }

                            if((buffer[2] & 0xFF) == 0 && seqDown)
                            {
                                if(gear > lowerGearLimit)
                                {
                                    gear--;
                                }
                                seqDown = false;
                            }
                            System.out.println("Gear:" + gear);

                            brake = 255 - (buffer[22] & 0xFF);
                            System.out.println("Brake: " + brake);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error reading from Fanatec device: " + ex.getMessage());
                }
            }
        }

        if(Math.abs(steeringWheel) < 0.08)
        {
            right = false;
            left = false;
        } else if (steeringWheel >= 0.8)
        {
            right = true;
            left = false;
        } else if (steeringWheel <= -0.8)
        {
            right = false;
            left = true;
        }

        if(gas >= 32 && brake == 0 && gear == 1)
        {
            forward = true;
            speed = gas;
            backward = false;
        }else if(gas >= 32 && brake == 0 && gear == 0)
        {
            forward = false;
            speed = gas;
            backward = true;
        }else if(gas == 0 && brake >= 32)
        {
            forward = false;
            speed = 0;
            backward = false;
        }
        else if(gas == 0 && brake == 0)
        {
            if(speed >= 10)// slowly slow down
            {
                speed -= 10;
            }else
            {
                speed = 0;
            }
        }

        slider.setValue((double)speed/2.55);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        myAndroidCamera.stopServer();
        androidBatteryClient.stopServer();
        stm32ClientRemoteControl.stopClient();
        stm32Status.stopSTM32Status();
        System.out.println("Closing the application.");
    }

    public static void main(String[] args){
        launch(args);
    }

    private void changePaneColor(String color)
    {
        pane.setStyle("-fx-background-color: #" + color);
    }

    private void updateImage()
    {
        if(updateImage)
        {
            updateImage = false;
            System.out.println("some data: " + length);
            try{
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(byteArray));
                imageCamera = SwingFXUtils.toFXImage(img, null);
                imageViewCamera.setImage(imageCamera);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(length > 5000)
                    updateImage = true;

            }
            catch(IOException i)
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
            catch(IOException i)
            {
                System.out.println(i);
            }
        }
    }

    @Override
    public void hidDeviceAttached(HidServicesEvent event) {

        System.out.println(ANSI_BLUE + "Device attached: " + event + ANSI_RESET);

    }

    @Override
    public void hidDeviceDetached(HidServicesEvent event) {

        System.out.println(ANSI_YELLOW + "Device detached: " + event + ANSI_RESET);

    }

    @Override
    public void hidFailure(HidServicesEvent event) {

        System.out.println(ANSI_RED + "HID failure: " + event + ANSI_RESET);

    }

    @Override
    public void hidDataReceived(HidServicesEvent event) {

        System.out.printf(ANSI_PURPLE + "Data received:%n");
        byte[] dataReceived = event.getDataReceived();

        if (dataReceived.length >= 3) {
            int steering = dataReceived[0] & 0xFF;  // Unsigned byte
            int gas = dataReceived[1] & 0xFF;
            int brake = dataReceived[2] & 0xFF;

            System.out.println("Steering: " + steering);
            System.out.println("Gas: " + gas);
            System.out.println("Brake: " + brake);
        }

        printAsHex(dataReceived);

    }

    public void printPlatform() {

        // System info to assist with library detection
        System.out.println("Platform architecture: " + Platform.ARCH);
        System.out.println("Resource prefix: " + Platform.RESOURCE_PREFIX);
        System.out.println("Libusb activation: " + Platform.isLinux());

    }

    public static void printAsHex(byte[] dataReceived) {
        System.out.printf("< [%02x]:", dataReceived.length);
        for (byte b : dataReceived) {
            System.out.printf(" %02x", b);
        }
        System.out.println(ANSI_RESET);
    }

    public void waitAndShutdown(HidServices hidServices) {

        System.out.printf(ANSI_YELLOW + "Waiting 30s to demonstrate attach/detach handling. Watch for slow response after write if configured.%n" + ANSI_RESET);

        // Stop the main thread to demonstrate attach and detach events
        sleepNoInterruption();

        // Shut down and rely on auto-shutdown hook to clear HidApi resources
        System.out.printf(ANSI_YELLOW + "Triggering shutdown...%n" + ANSI_RESET);
        hidServices.shutdown();
    }

    /**
     * Invokes {@code unit.}{@link TimeUnit#sleep(long) sleep(sleepFor)}
     * uninterruptibly.
     */
    public static void sleepNoInterruption() {
        boolean interrupted = false;
        try {
            long remainingNanos = TimeUnit.SECONDS.toNanos(30);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    // TimeUnit.sleep() treats negative timeouts just like zero.
                    NANOSECONDS.sleep(remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
