package com.viktor.vano.robot.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class FileManager {
    public static String readOrCreateFile(String filename)
    {
        File file = new File(filename);

        try
        {
            //Create the file
            if (file.createNewFile())
            {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists.");
            }

            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String data;
            data = reader.readLine();
            reader.close();
            System.out.println("Reading successful.");

            if(data==null && filename.equals("IP_STM32.txt"))
            {
                data="192.168.1.3";
                writeToFile(filename, data);
            }

            if(data==null && filename.equals("AndroidIP.txt"))
            {
                data="192.168.1.26";
                writeToFile(filename, data);
            }

            return data;
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;
        }
    }

    public static boolean writeToFile(String filename, String data)
    {
        File file = new File(filename);

        try
        {
            //Create the file
            if (file.createNewFile())
            {
                System.out.println("File is created!");
            } else {
                System.out.println("File already exists.");
            }

            //Write Content
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.close();
            System.out.println("File write successful.");
            return true;
        }
        catch (Exception e)
        {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return false;
        }
    }
}
