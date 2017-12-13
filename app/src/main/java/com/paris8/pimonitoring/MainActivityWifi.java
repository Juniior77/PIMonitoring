package com.paris8.pimonitoring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivityWifi extends AppCompatActivity {

    public String adr = null;
    public int port = 0;
    public String receive = "empty";
    public String inputCpu;
    public String inputRam;
    public String inputTemp;
    public int nbStep = 0;
    public String mNom = "";
    public boolean connect = false;
    public boolean toastEtat = false;
    DBManager bdd = new DBManager(this);

    TextView TextInfo;
    Button btnSend;
    ArcProgress arcCpu;
    ArcProgress arcRam;
    ArcProgress arcTemp;
    EditText inputIpNom;
    EditText inputPortNbStep;
    ProgressBar progressBarWifi;
    TextView textProgressWifi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wifi);
        TextInfo = (TextView) findViewById(R.id.infoWifi);
        btnSend = (Button)findViewById(R.id.btnConnectStart);
        arcCpu = (ArcProgress)findViewById(R.id.arc_progressCpu);
        arcCpu.setBottomText("CPU");
        arcRam = (ArcProgress)findViewById(R.id.arc_progressRam);
        arcRam.setBottomText("RAM");
        arcTemp = (ArcProgress)findViewById(R.id.arc_progressTemp);
        arcTemp.setBottomText("TEMP");
        inputIpNom = (EditText)findViewById(R.id.editTextIPNom);
        inputPortNbStep = (EditText)findViewById(R.id.editTextPortNbStep);
        progressBarWifi = (ProgressBar)findViewById(R.id.progressBarWifi);
        textProgressWifi = (TextView)findViewById(R.id.textProgressWifi);
        bdd.open();

        btnSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(connect == false){
                    //On demande l'ip et le port pour tester une conexion
                    try{
                        adr = inputIpNom.getText().toString();
                        port = Integer.parseInt(inputPortNbStep.getText().toString());
                        if(!adr.isEmpty() && port > 0){
                            connectServer();
                            receive = receive.trim();
                            if(receive.contains("Hello")){
                                inputIpNom.setText("");
                                inputPortNbStep.setText("");
                                inputIpNom.setHint("Nom");
                                inputPortNbStep.setHint("Nombre de requettes");
                                btnSend.setText("Start !");
                                TextInfo.setText("Connecté à " + adr.toString() +":"+port);
                                connect = true;
                            }
                        }
                    }catch (Exception ex){
                        if(adr.isEmpty()){
                            Toast.makeText(MainActivityWifi.this, "Veuillez renseigner une IP !", Toast.LENGTH_LONG).show();
                        }
                        if(port <= 0){
                            Toast.makeText(MainActivityWifi.this, "Veuillez renseigner un PORT d'écoute !", Toast.LENGTH_LONG).show();
                        }
                    }


                }
                else{

                    try{
                        mNom = inputIpNom.getText().toString();
                        nbStep = Integer.parseInt(inputPortNbStep.getText().toString());
                    }catch (Exception ex){
                        if(mNom.isEmpty()){
                            Toast.makeText(MainActivityWifi.this, "Veuillez renseigner un Nom !", Toast.LENGTH_LONG).show();
                        }
                        if(nbStep <= 0){
                            Toast.makeText(MainActivityWifi.this, "Veuillez renseigner un nombre de requettes ! " + ex.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        toastEtat = true;
                    }
                    if(nbStep > 0 && !mNom.isEmpty()) {
                        Toast.makeText(MainActivityWifi.this, "Le monitoring "+ mNom + " est en cours d'execution !", Toast.LENGTH_LONG).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int stepAct = 0;
                                Date dId = new Date();
                                SimpleDateFormat fId = new SimpleDateFormat("yyyyMMddHHmmss");
                                String DateID = fId.format(dId);

                                Monitoring mMonitoring = new Monitoring();
                                mMonitoring.ID = DateID;
                                mMonitoring.NOM = mNom;
                                progressBarWifi.setMax(nbStep);
                                while (stepAct != nbStep+1) {
                                    if(stepAct == nbStep){
                                        //Tu reinitialise
                                        try {
                                            //Log.i("CPU: ", "" + mMonitoring.CPU);
                                            progressBarWifi.setProgress(0);
                                            textProgressWifi.setText("");
                                        } catch (Exception ex) {
                                            continue;
                                        }
                                    }
                                    else{
                                        sendAndReceiveData();
                                        try {
                                            Thread.sleep(5000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        try{
                                            textProgressWifi.setText((stepAct+1)+"/"+nbStep);
                                        }catch (Exception ex){
                                            progressBarWifi.setProgress(stepAct+1);
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
                                receive = "Hello";
                            }
                        }).start();
                    }

                    else{
                        if(toastEtat == false)
                            Toast.makeText(MainActivityWifi.this, "Veuillez renseigner un Nom et un nombre de requettes !", Toast.LENGTH_LONG).show();
                    }
                    toastEtat = false;
                }
            }
        });
    }

    public void connectServer(){
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    ClientTcp mClient = new ClientTcp(adr, port);
                    mClient.sendDataWithString("0");
                    receive=mClient.receiveDataFromServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.interrupted();
            }
        });mThread.start();
    }

    public void sendAndReceiveData(){
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    ClientTcp mClient = new ClientTcp(adr, port);
                    mClient.sendDataWithString("1");
                    receive=mClient.receiveDataFromServer();
                    try{
                        String[] parts = receive.split(";");
                        inputCpu = parts[0];
                        inputRam = parts[1];
                        inputTemp = parts[2];
                    }catch (Exception ex){
                        Log.i("VALUE CATCH:", "" + receive);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.interrupted();
            }
        });mThread.start();
    }
}