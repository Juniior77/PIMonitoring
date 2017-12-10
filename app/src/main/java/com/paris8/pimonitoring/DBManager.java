package com.paris8.pimonitoring;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Guillaume on 08/12/2017.
 */

public class DBManager {

    private MySQLite maBaseSQLite;
    private SQLiteDatabase db;

    //Constructeur
    public DBManager(Context context){
        maBaseSQLite = MySQLite.getInstance(context);
    }
    public void open(){
        db = maBaseSQLite.getWritableDatabase();
    }
    public void close(){
        db.close();
    }

    public void addStep(Monitoring monitoring){
        Date d = new Date();
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        String Date = f.format(d);
        monitoring.DATE = Date;
        //db.rawQuery(, null);
        db.execSQL("INSERT INTO MONITORING(" +
                "ID, NOM, DATE, N_STEP, CPU, RAM, TEMP) " +
                "VALUES(" + monitoring.ID +
                ",'" + monitoring.NOM +
                "'," + monitoring.DATE +
                "," + monitoring.N_STEP +
                "," + monitoring.CPU +
                "," + monitoring.RAM +
                "," + monitoring.TEMP +");");
    }
    public Monitoring getStep(String ID, int step){
        Monitoring mMonitoring = new Monitoring();

        Cursor mCursor = db.rawQuery("SELECT * FROM MONITORING WHERE ID = "+ ID + " AND N_STEP = "+ step, null);
        if(mCursor.moveToFirst()){
            mMonitoring.ID = mCursor.getString(mCursor.getColumnIndex("ID"));
            mMonitoring.NOM = mCursor.getString(mCursor.getColumnIndex("NOM"));
            mMonitoring.DATE = mCursor.getString(mCursor.getColumnIndex("DATE"));
            mMonitoring.N_STEP = mCursor.getInt(mCursor.getColumnIndex("N_STEP"));
            mMonitoring.CPU = mCursor.getInt(mCursor.getColumnIndex("CPU"));
            mMonitoring.RAM = mCursor.getInt(mCursor.getColumnIndex("RAM"));
            mMonitoring.TEMP = mCursor.getInt(mCursor.getColumnIndex("TEMP"));
            mCursor.close();
        }
        return mMonitoring;
    }
    public String getLastIdMonitoring(){
        String ID = null;
        Cursor mCursorID = db.rawQuery("SELECT ID FROM MONITORING WHERE ID = (SELECT max(ID) FROM MONITORING)", null);
        if(mCursorID.moveToFirst()){
             ID = mCursorID.getString(mCursorID.getColumnIndex("ID"));
            mCursorID.close();
        }
        return ID;
    }
    public List<Monitoring> getMonitoringByID(String ID){
        //ArrayList<Monitoring> listMonitoring = new ArrayList<Monitoring>();
        List<Monitoring> myListMonitoring = new ArrayList<Monitoring>();
        Cursor mCursor = db.rawQuery("SELECT * FROM MONITORING WHERE ID = '" + ID +"' ORDER BY N_STEP ASC ", null);
        if(mCursor.moveToFirst()){
            int nb_line = mCursor.getCount();
            for(int i = 0; i < nb_line; i++)
            {
                Monitoring mMonitoring = new Monitoring();
                mMonitoring.ID = mCursor.getString(mCursor.getColumnIndex("ID"));
                mMonitoring.NOM = mCursor.getString(mCursor.getColumnIndex("NOM"));
                mMonitoring.DATE = mCursor.getString(mCursor.getColumnIndex("DATE"));
                mMonitoring.N_STEP = mCursor.getInt(mCursor.getColumnIndex("N_STEP"));
                mMonitoring.CPU = mCursor.getInt(mCursor.getColumnIndex("CPU"));
                mMonitoring.RAM = mCursor.getInt(mCursor.getColumnIndex("RAM"));
                mMonitoring.TEMP = mCursor.getInt(mCursor.getColumnIndex("TEMP"));
                mCursor.moveToNext();
                myListMonitoring.add(mMonitoring);
                //listMonitoring.iterator().next();
            }
            mCursor.close();
        }
        return myListMonitoring;
    }
    public String[] getListMonitoringByDate(String Date, String Nom){

        String ReqSql = new String();
        if(Date != null && Nom == null)
        {
            //On cherche a partir d'une date
            ReqSql = "SELECT DISTINCT ID, NOM FROM MONITORING WHERE ID LIKE '" + Date +"%'";
        }
        if(Date == null && Nom != null)
        {
            //On cherche a partir d'un nom
            ReqSql = "SELECT DISTINCT ID, NOM FROM MONITORING WHERE NOM LIKE '%" + Nom +"%'";
        }
        if(Date != null && Nom != null)
        {
            //On cherche a partir d'une date et d'un nom
            ReqSql = "SELECT DISTINCT ID, NOM FROM MONITORING WHERE ID LIKE '" + Date +"%' AND NOM LIKE '%" + Nom +"%'";
        }

        Cursor mCursor = db.rawQuery(ReqSql, null);
        String[] listMoni = new String[mCursor.getCount()];
        if(mCursor.moveToFirst()){
            int nb_line = mCursor.getCount();
            for(int i = 0; i < nb_line; i++)
            {
                Monitoring mMonitoring = new Monitoring();
                mMonitoring.ID = mCursor.getString(mCursor.getColumnIndex("ID"));
                mMonitoring.NOM = mCursor.getString(mCursor.getColumnIndex("NOM"));
                mCursor.moveToNext();
                listMoni[i] = mMonitoring.NOM + " : " + mMonitoring.ID;
            }
            mCursor.close();
        }
        return listMoni;
    }
}
