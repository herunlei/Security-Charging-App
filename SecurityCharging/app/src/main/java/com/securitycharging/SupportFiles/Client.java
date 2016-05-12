package com.securitycharging.SupportFiles;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private static final String IP_ADRE = "172.20.10.3";    //This IP address should change to yours
    private static final int PORT = 8888;                   //This port can keep the same or change to yours
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread runningThread;
    private boolean running;
    private String msg;
    private String tempMsg;

    /**
     * @param message
     */
    //use for create customer and save customer information
    public Client(String message) {

        this.msg = message;
        this.tempMsg = "404";

        try {
            //The server IP address could be changed when use different computer to server
            //Creating a new socket that will be used to do the connection with server
            socket = new Socket(IP_ADRE, PORT);
            //Creating input and output stream
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //state of Client
            running = true;

            runningThread = new Thread(this);
            runningThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }

    //This is used to disconnect with the server
    public void disconnect() {
        running = false;
        if (runningThread != null)
            runningThread.interrupt();
        runningThread = null;

        try {
            reader.close();
        } catch (Exception ignored) {
        }
        reader = null;

        try {
            writer.close();
        } catch (Exception ignored) {
        }
        writer = null;
        try {
            socket.close();
        } catch (Exception ignored) {
        }
        socket = null;
    }

    /**
     * @param message
     */
    //This is used to send message to server
    public void sendMessage(String message) {
        if (running) {
            writer.println(message);
            writer.flush();
        }
    }

    //The cord part of Client that do the all communication work
    public void run() {

        sendMessage(this.msg);

        try {
            String message;
            while ((message = reader.readLine()) != null && running) {
                tempMsg = message.split("//")[1];
            }
            System.out.println("The message in client is: " + tempMsg);
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }

        disconnect();
    }

    /**
     * @return
     */
    public String getMsg() {
        return this.tempMsg;
    }
}

