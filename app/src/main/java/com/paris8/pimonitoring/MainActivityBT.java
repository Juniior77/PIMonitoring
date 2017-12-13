package com.paris8.pimonitoring;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;


public class MainActivityBT extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final String REQUEST_DATA = "1";
    private static final String CLOSE_SOCKET = "q";
    public String inputCpu = null;
    public String inputRam = null;
    public String inputTemp = null;
    BluetoothAdapter bluetoothAdapter;
    DBManager bdd = new DBManager(this);
    public int nbStep;
    public String NomMonitoring;
    public int stepTmp;
    public int tempsEstime;
    public boolean toastEtat = false;


    ArrayList<BluetoothDevice> pairedDeviceArrayList;

    TextView textInfo, textStatus;
    ListView listViewPairedDevice;
    LinearLayout inputPane;
    EditText inputNameMonitoring;
    EditText inputTempTotal;
    Button btnSend;
    ArcProgress arcCpu;
    ArcProgress arcRam;
    ArcProgress arcTemp;
    TextView TextTempEstime;
    TextView textProgress;
    ProgressBar progressBar;

    //GraphView graphCpu;

    ArrayAdapter<BluetoothDevice> pairedDeviceAdapter;
    private UUID myUUID;
    private final String UUID_STRING_WELL_KNOWN_SPP =
            "00001101-0000-1000-8000-00805F9B34FB";

    ThreadConnectBTdevice myThreadConnectBTdevice;
    ThreadConnected myThreadConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bt);

        textInfo = (TextView)findViewById(R.id.info);
        textStatus = (TextView)findViewById(R.id.status);
        listViewPairedDevice = (ListView)findViewById(R.id.pairedlist);

        inputPane = (LinearLayout)findViewById(R.id.inputpane);
        inputNameMonitoring = (EditText)findViewById(R.id.nameMonitoring);
        inputTempTotal = (EditText)findViewById(R.id.tempTotal);
        btnSend = (Button)findViewById(R.id.send);
        arcCpu = (ArcProgress)findViewById(R.id.arc_progressCpu);
        arcCpu.setBottomText("CPU");
        arcRam = (ArcProgress)findViewById(R.id.arc_progressRam);
        arcRam.setBottomText("RAM");
        arcTemp = (ArcProgress)findViewById(R.id.arc_progressTemp);
        arcTemp.setBottomText("TEMP");
        TextTempEstime = (TextView)findViewById(R.id.textTempEstime);
        textProgress = (TextView)findViewById(R.id.textProgress);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        bdd.open();


        inputTempTotal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try{
                    stepTmp = Integer.parseInt(inputTempTotal.getText().toString());
                    tempsEstime = stepTmp*5;
                    if(tempsEstime < 60){
                        TextTempEstime.setText("Temps estimée: " + tempsEstime + " secondes");
                    }
                    else if(tempsEstime > 59 && tempsEstime < 2599)
                    {
                        int minute = tempsEstime / 60;
                        //On est a plus d'une minute donc on parle de minutes
                        TextTempEstime.setText("Temps estimée: " + minute + " minutes");
                    }
                    else if(tempsEstime > 2599)
                    {
                        //On est a plus d'une heure donc on parle en heures et en minute
                        int heure = tempsEstime / 3600;
                        TextTempEstime.setText("Temps estimée: " + heure + " heures");
                    }
                    else{
                        TextTempEstime.setText("Temps estimée: NC");
                    }
                }catch (Exception ex){
                    TextTempEstime.setText("Temps estimée: NC");
                }


            }
        });

        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {



                    //Log.i("INFO", "ON LANCE UN THREAD POUR MONITORING");
                    if (myThreadConnected != null) {

                        try{
                            NomMonitoring = inputNameMonitoring.getText().toString();
                            nbStep = Integer.parseInt(inputTempTotal.getText().toString());
                        }
                        catch (Exception ex){
                            if(NomMonitoring.isEmpty()){
                                Toast.makeText(MainActivityBT.this, "Le nom n'est pas renseigné", Toast.LENGTH_LONG).show();
                                toastEtat = true;
                            }
                            Toast.makeText(MainActivityBT.this, "Le nombre de requette n'est pas valide: " + ex.getMessage(), Toast.LENGTH_LONG).show();
                            toastEtat = true;
                        }
                        if(!NomMonitoring.isEmpty() && nbStep > 0)
                        {
                            Toast.makeText(MainActivityBT.this, "Le monitoring " + NomMonitoring + " est en cours d'exécution !", Toast.LENGTH_LONG).show();
                            new Thread(new Runnable() {
                                public void run() {


                                    byte[] bytesToSend = REQUEST_DATA.getBytes();
                                    int stepAct = 0;
                                    Date dId = new Date();
                                    SimpleDateFormat fId = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String DateID = fId.format(dId);

                                    Monitoring mMonitoring = new Monitoring();
                                    mMonitoring.ID = DateID;
                                    mMonitoring.NOM = NomMonitoring;
                                    progressBar.setMax(nbStep);
                                    while (stepAct != nbStep+1) {
                                        if(stepAct == nbStep){
                                            //Tu reinitialise
                                            try {
                                                //Log.i("CPU: ", "" + mMonitoring.CPU);
                                                progressBar.setProgress(0);
                                                textStatus.setText("");
                                            } catch (Exception ex) {
                                                continue;
                                            }
                                        }else{


                                        //Log.i("INTERATION", "ON EST AU " + stepAct + " STEP");
                                        myThreadConnected.write(bytesToSend);
                                        try {
                                            textProgress.setText((stepAct + 1) + "/" + nbStep);
                                        } catch (Exception ex) {
                                            progressBar.setProgress(stepAct + 1);
                                        }
                                        try {
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e) {
                                            Log.i("INFO THREAD:", "" + e.toString());
                                            e.printStackTrace();
                                        }
                                        mMonitoring.CPU = 100 - Integer.parseInt(inputCpu.trim());
                                        mMonitoring.RAM = 1024 - (Integer.parseInt(inputRam.trim()) / 1024);
                                        mMonitoring.TEMP = Integer.parseInt(inputTemp.trim()) / 1000;
                                        mMonitoring.N_STEP = stepAct;
                                        bdd.addStep(mMonitoring);
                                        stepAct = stepAct + 1;
                                        try {
                                            //Log.i("CPU: ", "" + mMonitoring.CPU);
                                            arcCpu.setProgress(mMonitoring.CPU);
                                        } catch (Exception ex) {
                                            try {
                                                arcRam.setProgress(mMonitoring.RAM / 10);
                                            } catch (Exception ex2) {
                                                try {
                                                    arcTemp.setProgress((mMonitoring.TEMP * 90) / 100);
                                                } catch (Exception ex3) {
                                                    continue;
                                                }
                                            }
                                        }
                                        }
                                    }
                                    bytesToSend = CLOSE_SOCKET.getBytes();
                                    myThreadConnected.write(bytesToSend);
                                }
                            }).start();
                        }
                        else{
                            if(toastEtat == false)
                                Toast.makeText(MainActivityBT.this, "Veuillez renseigner un Nom et un nombre de requettes !", Toast.LENGTH_LONG).show();
                        }
                        toastEtat = false;
                    }
        }
    });



        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,
                    "FEATURE_BLUETOOTH NOT support",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //using the well-known SPP UUID
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this,
                    "Bluetooth is not supported on this hardware platform",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String stInfo = bluetoothAdapter.getName() + "\n" +
                bluetoothAdapter.getAddress();
        //textInfo.setText(stInfo);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Turn ON BlueTooth if it is OFF
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        setup();
    }

    private void setup() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            pairedDeviceArrayList = new ArrayList<BluetoothDevice>();

            for (BluetoothDevice device : pairedDevices) {
                pairedDeviceArrayList.add(device);
            }

            pairedDeviceAdapter = new ArrayAdapter<BluetoothDevice>(this,
                    android.R.layout.simple_list_item_1, pairedDeviceArrayList);
            listViewPairedDevice.setAdapter(pairedDeviceAdapter);

            listViewPairedDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    BluetoothDevice device =
                            (BluetoothDevice) parent.getItemAtPosition(position);
                    /*Toast.makeText(MainActivityBT.this,
                            "Name: " + device.getName() + "\n"
                                    + "Address: " + device.getAddress() + "\n"
                                    + "BondState: " + device.getBondState() + "\n"
                                    + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                                    + "Class: " + device.getClass(),
                            Toast.LENGTH_LONG).show();*/

                    //textStatus.setText("start ThreadConnectBTdevice");
                    myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
                    myThreadConnectBTdevice.start();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        bdd.close();
        if(myThreadConnected!=null){
            myThreadConnected.write(CLOSE_SOCKET.getBytes());
            myThreadConnected.cancel();
        }
        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode == Activity.RESULT_OK){
                setup();
            }else{
                Toast.makeText(this,
                        "BlueTooth NOT enabled",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                //textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //textStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage);
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;

                runOnUiThread(new Runnable(){

                    @Override
                    public void run() {
                        //textStatus.setText(msgconnected);

                        listViewPairedDevice.setVisibility(View.GONE);
                        inputPane.setVisibility(View.VISIBLE);
                        textInfo.setText("Connexion réussie");
                    }});

                startThreadConnected(bluetoothSocket);
            }else{
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String msgReceived = String.valueOf(bytes) +
                            " bytes received:\n"
                            + strReceived;

                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            //textStatus.setText(msgReceived);
                            String[] parts = strReceived.split(";");
                            inputCpu = parts[0];
                            inputRam = parts[1];
                            inputTemp = parts[2];
                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            //textStatus.setText(msgConnectionLost);
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}