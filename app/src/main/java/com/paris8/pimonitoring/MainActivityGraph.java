package com.paris8.pimonitoring;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.util.ArrayList;
import java.util.List;

public class MainActivityGraph extends AppCompatActivity {

    DBManager bdd = new DBManager(this);
    List<Monitoring> mListMonitoring = new ArrayList<Monitoring>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_graph);

        GraphView graphCpu = (GraphView)findViewById(R.id.graphCpu);
        graphCpu.setTitle("Utilisation du CPU (%)");
        graphCpu.getViewport().setScalable(true);
        GraphView graphRam = (GraphView)findViewById(R.id.graphRam);
        graphRam.setTitle("Utilisation de la RAM (Mo) - Capacité 1Go");
        graphRam.getViewport().setScalable(true);
        GraphView graphTemp = (GraphView)findViewById(R.id.graphTemp);
        graphTemp.setTitle("Temperature (°C) - Seuil critique 90°C");
        graphTemp.getViewport().setScalable(true);

        bdd.open();

        String ID = getIntent().getStringExtra("id");
        mListMonitoring = bdd.getMonitoringByID(ID);
        LineGraphSeries<DataPoint> seriesCpu = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesRam = new LineGraphSeries<>();
        LineGraphSeries<DataPoint> seriesTemp = new LineGraphSeries<>();

        seriesCpu.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(MainActivityGraph.this, "Utilisation CPU [Step/%]: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
        seriesRam.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(MainActivityGraph.this, "Utilisation RAM [Step/Mo]: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });
        seriesTemp.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Toast.makeText(MainActivityGraph.this, "Température [Step/°C]: "+dataPoint, Toast.LENGTH_SHORT).show();
            }
        });

        for(int i = 0; i < mListMonitoring.size(); i++)
        {
            DataPoint dataCpu = new DataPoint(mListMonitoring.get(i).N_STEP, mListMonitoring.get(i).CPU);
            seriesCpu.appendData(dataCpu, false, 100, true);
            DataPoint dataRam = new DataPoint(mListMonitoring.get(i).N_STEP, mListMonitoring.get(i).RAM);
            seriesRam.appendData(dataRam, false, 100, true);
            DataPoint dataTemp = new DataPoint(mListMonitoring.get(i).N_STEP, mListMonitoring.get(i).TEMP);
            seriesTemp.appendData(dataTemp, false, 100, true);
        }

        graphCpu.addSeries(seriesCpu);
        graphRam.addSeries(seriesRam);
        graphTemp.addSeries(seriesTemp);

        bdd.close();

    }
}
