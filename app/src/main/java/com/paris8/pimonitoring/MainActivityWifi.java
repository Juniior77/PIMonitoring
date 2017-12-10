package com.paris8.pimonitoring;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivityWifi extends AppCompatActivity {

    private Socket socket;
    private static final int SERVERPORT = 15006;
    private static final String SERVER_IP = "192.168.1.22";

    EditText et;
    Button btnSend;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wifi);
        et = (EditText) findViewById(R.id.editTextClient);
        btnSend = (Button)findViewById(R.id.button);

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            Client mClient = new Client("192.168.1.22", 15006);
                            mClient.sendDataWithString("Hello World");
                            et.setText(mClient.receiveDataFromServer());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
            }
        });
    }

    public class Client {

        /**
         * Maximum size of buffer
         */
        public static final int BUFFER_SIZE = 2048;
        private Socket socket = null;
        private PrintWriter out = null;
        private BufferedReader in = null;

        private String host = null;
        private int port = 15000;


        /**
         * Constructor with Host, Port and MAC Address
         * @param host
         * @param port
         */
        public Client(String host, int port) {
            this.host = host;
            this.port = port;
        }

        private void connectWithServer() {
            try {
                if (socket == null) {
                    socket = new Socket("192.168.1.22", 15006);
                    out = new PrintWriter(socket.getOutputStream());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void disConnectWithServer() {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void sendDataWithString(String message) {
            if (message != null) {
                connectWithServer();
                out.write(message);
                out.flush();
            }
        }

        public String receiveDataFromServer() {
            try {
                String message = "";
                int charsRead = 0;
                char[] buffer = new char[BUFFER_SIZE];

                while ((charsRead = in.read(buffer)) != -1) {
                    message += new String(buffer).substring(0, charsRead);
                }

                disConnectWithServer(); // disconnect server
                return message;
            } catch (IOException e) {
                return "Error receiving response:  " + e.getMessage();
            }
        }


    }

}
