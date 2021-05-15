import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static java.lang.Thread.sleep;

public class RobotController extends Application {
    private Pane pane;
    private String message = "";
    private byte[] data;
    private int length = 0;
    private Label label;
    private Timeline timeline;
    private MyServer myCameraServer;
    private ImageView imageView;
    private Image image;
    private boolean updateImage = false;
    private final int cameraPort = 7770;

    @Override
    public void start(Stage stage){
        final int width = 1800;
        final int height = 900;

        pane = new Pane();

        label = new Label("PORT: 7770");
        label.setFont(Font.font("Arial", 24));
        label.setStyle("-fx-background-color: #FFFFFF");
        pane.getChildren().add(label);

        Scene scene = new Scene(pane, width, height);

        stage.setTitle("Robot Controller");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
        stage.setMaxWidth(stage.getWidth());
        stage.setMaxHeight(stage.getHeight());
        stage.setResizable(false);

        imageView = new ImageView();
        imageView.setFitWidth(720);
        imageView.setFitHeight(960);
        imageView.setLayoutX(300);
        imageView.setLayoutY(0);
        imageView.setRotate(270);
        pane.getChildren().add(imageView);

        timeline = new Timeline(new KeyFrame(Duration.millis(20), event ->{
            if(updateImage)
                parseMessage();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        //changePaneColor("000000");
        myCameraServer = new MyServer(cameraPort);
        myCameraServer.start();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        myCameraServer.stopServer();
        System.out.println("Closing the application.");
    }

    public static void main(String[] args){
        launch(args);
    }

    private void changePaneColor(String color)
    {
        pane.setStyle("-fx-background-color: #" + color);
    }

    private void parseMessage()
    {
        label.setText("PORT: 7770\n" + message);
        //TODO: Add imageView
        if(updateImage)
        {
            updateImage = false;
            System.out.println("some data: " + length);
            /*BufferedImage bufferedImage = new BufferedImage(960, 720, BufferedImage.TYPE_INT_RGB);
            int color;
            for(int width = 0; width < bufferedImage.getWidth(); width++)
                for(int height = 0; height < bufferedImage.getHeight(); height++) {
                    color = data[width*height + width]*65536 + data[width*height + width + 1]*256 + data[width*height + width + 2];
                    bufferedImage.setRGB(width, height, color);
                }
            image = SwingFXUtils.toFXImage(bufferedImage, null);
            imageView.setImage(image);*/
            try{
                BufferedImage img = ImageIO.read(new File("image.jpg"));
                image = SwingFXUtils.toFXImage(img, null);
                imageView.setImage(image);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        message = "";
    }

    private void sendDataToServer(String message)
    {
        try
        {
            // need host and port, we want to connect to the ESP8266 ServerSocket at port 80
            Socket socket = new Socket();
            socket.setSoTimeout(300);
            socket.connect(new InetSocketAddress("192.168.2.239", 80), 300);
            System.out.println("Connected!");

            // get the output stream from the socket.
            OutputStream outputStream = socket.getOutputStream();
            // create a data output stream from the output stream so we can send data through it
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            System.out.println("Sending string to the ServerSocket: " + message);

            // write the message we want to send
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush(); // send the message
            dataOutputStream.close(); // close the output stream when we're done.

            System.out.println("Closing socket.");
            socket.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    class MyServer extends Thread{
        private boolean active = true;
        private int port = 0;
        private ServerSocket ss;

        public void stopServer(){
            this.active = false;
            try {
                ss.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public MyServer(int port){
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            ss = null;
            try {
                ss = new ServerSocket(port);
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
                        //length = dataInputStream.read(data);
                        length = dataInputStream.readInt();
                        System.out.println("Got the Size");
                        int bytesRead ;
                        int len = 0;
                        byte[] buffer = new byte[1000000];
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        while (len<length)
                        {
                            bytesRead = inputStream.read(buffer, 0, (int)Math.min(buffer.length, length-len));
                            len = len + bytesRead;
                            baos.write(buffer, 0, bytesRead);
                        }
                        byte [] byteArray = new byte [length];
                        byteArray = baos.toByteArray();
                        File file = new File ("image.jpg");
                        if (!file.exists())
                        {
                            file.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream (file);
                        fos.write(byteArray);
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Received message:\n" + message + "\n");

                    if(length > 1000)
                        updateImage = true;
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
}
