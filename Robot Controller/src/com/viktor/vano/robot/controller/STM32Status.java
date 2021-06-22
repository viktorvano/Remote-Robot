package com.viktor.vano.robot.controller;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class STM32Status extends Thread
{
    private boolean active = true;
    private int updatePeriod;
    private OkHttpClient httpClient;
    private String stm32Message = "";
    private boolean messageFlag = false;
    private String stm32IP;

    public STM32Status(int updatePeriodMilliSeconds, String stm32IP){
        this.updatePeriod = updatePeriodMilliSeconds;
        this.stm32IP = stm32IP;
        httpClient = new OkHttpClient();
    }

    public void stopSTM32Status()
    {
        this.active = false;
    }

    public boolean isMessageAvailable()
    {
        return this.messageFlag;
    }

    public String getStm32Message()
    {
        return this.stm32Message;
    }

    @Override
    public void run()
    {
        super.run();
        while (active) {
            System.out.println("STM32 GET call...");
            try {
                Thread.sleep(updatePeriod);
                sendGet();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void sendGet()
    {
        messageFlag = false;
        String url = "http://" + this.stm32IP + ":80/";
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            stm32Message = response.body().string();
            System.out.println("STM32 OUTPUT: " + stm32Message);
            messageFlag = true;
        }catch (Exception e){
            messageFlag = false;
            e.printStackTrace();
        }
    }
}
