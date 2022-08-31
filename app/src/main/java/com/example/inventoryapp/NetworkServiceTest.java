package com.example.inventoryapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class NetworkServiceTest {

    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;

    private Listener listener;

    private String host = "10.0.1.4";
    private int port = 3000;

    public NetworkServiceTest()
    {
        try {

            if (socket == null)
            {
                socket = new Socket(this.host, this.port);
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }

            if(listener == null)
            {
                listener = new Listener();
                Thread thread = new Thread(listener);
                thread.start();
            }

        } catch (Exception e) {
            // ...
        }
    }

    class Listener implements Runnable
    {

        @Override
        public void run() {

            try {
                String line = null;
/*
            while((line = in.readLine()) != null)
            {
                // Do something. Never gets here

            }

 */
            } catch (Exception e) {
                // ...
            }

        }

    }
}
