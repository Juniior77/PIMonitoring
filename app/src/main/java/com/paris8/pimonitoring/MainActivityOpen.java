package com.paris8.pimonitoring;

import android.bluetooth.BluetoothDevice;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

public class MainActivityOpen extends AppCompatActivity {

    Button btnLastAnalyse;
    Button btnOtherAnalyse;
    DBManager bdd = new DBManager(this);
    EditText editDate;
    EditText editNom;
    ListView listItem;
    ArrayAdapter<String> listMoniAdapter;
    String idMonitoring = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_open);

        btnLastAnalyse = (Button)findViewById(R.id.btnLastAna);
        btnOtherAnalyse = (Button)findViewById(R.id.btnOtherAna);
        editDate = (EditText)findViewById(R.id.editDate);
        editNom = (EditText)findViewById(R.id.editNom);
        listItem = (ListView)findViewById(R.id.listItem);

        bdd.open();
        idMonitoring = bdd.getLastIdMonitoring();


        btnLastAnalyse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intentGraph = new Intent(MainActivityOpen.this, MainActivityGraph.class);
                intentGraph.putExtra("id", idMonitoring);
                startActivity(intentGraph);
            }
        });

        btnOtherAnalyse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(editDate.getText() != null || editNom.getText() != null){
                    String DateSearch = null;
                    String NomSearch = null;
                    if(editDate.getText() != null)
                    {
                        DateSearch = editDate.getText().toString();
                    }
                    if(editNom.getText() != null)
                    {
                        NomSearch = editNom.getText().toString();
                    }

                    String[] listMonitoring = bdd.getListMonitoringByDate(DateSearch, NomSearch);
                    for(int i = 0; i < listMonitoring.length; i++){
                        String tmp = listMonitoring[i];
                        String strSplit[] = tmp.split(" : ");
                        String nom = strSplit[0];
                        String date = strSplit[1];
                        String annee = date.substring(0, 4);
                        String moi = date.substring(4, 6);
                        String jour = date.substring(6, 8);
                        String heure = date.substring(8, 10);
                        String minute = date.substring(10, 12);
                        String sec = date.substring(12, 14);
                        listMonitoring[i] = "Nom: " + nom + " - Date: " + jour + "/" + moi + "/" + annee + " : " + heure +"h"+ minute + "min" + sec + "sec";
                    }
                    setListView(listMonitoring);
                    //Log.i("DateSearch: " , "" + DateSearch);
                }

            }
        });

        listItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object item = adapterView.getItemAtPosition(i);
                Log.i("ItemList: " , "" + item);
                String itemStr = item.toString();
                String[] tmpSplit = itemStr.split(" - Date: ");
                String Date = tmpSplit[1];
                String[] dateSplit = Date.split(" : ");
                String jour = dateSplit[0];
                String heure = dateSplit[1];
                String mJour = jour.substring(0, 2);
                String mMois = jour.substring(3, 5);
                String mAnnee = jour.substring(6, 10);
                String mHeure = heure.substring(0, 2);
                String mMin = heure.substring(3, 5);
                String mSec = heure.substring(8, 10);
                String ID = mAnnee + mMois + mJour + mHeure + mMin + mSec;

                Intent intentGraph = new Intent(MainActivityOpen.this, MainActivityGraph.class);
                intentGraph.putExtra("id", ID);
                startActivity(intentGraph);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        bdd.close();
        super.onDestroy();
    }
    public void setListView(String[] arrayString){
        listMoniAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayString);
        listItem.setAdapter(listMoniAdapter);
    }

}
