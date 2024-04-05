package com.example.noisens;

import android.Manifest;
import android.app.IntentService;
import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.noisens.CreaCanale.CHANNEL_ID;


public class Rilevazione extends IntentService {

    boolean shouldStop=false;
    private PowerManager.WakeLock wakeLock;
    final String sharedPrefs = "sharedPrefs";
    SharedPreferences sharedPreferences;
    int valIntervallo;
    MediaRecorder mRecorder=null;
    double valRumore;
    FusedLocationProviderClient fusedLocationProviderClient;
    List<Address> posizioni;
    double lat;
    double longi;
    int gp;
    String AK="xYzMaOPlFhrOt353:256hgf;214LRTGI£&((&LFURjs984";
    String json;
    double amplitudeDb;
    public static Listener listener_obj;
    String post;
    String p;
    int reale;




    public Rilevazione() {
        super("Rilevazione");
    }

    public Rilevazione (Listener listener) {
        super("Rilevazione");
        listener_obj=listener;
        setIntentRedelivery(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("MYAPP","ONDESTROY");
        wakeLock.release();
        stop();
        shouldStop=true;
        stopSelf();
    }

    public void start() {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new RecorderTask(mRecorder), 0, 5000);
            mRecorder.setOutputFile(getFilePath());
            try {
                mRecorder.prepare();
                mRecorder.start();
            } catch (IllegalStateException e) {
                Log.v("AUDIO","Eccezione");
                e.printStackTrace();
            }
            catch (IOException e) {
                Log.v("AUDIO","Eccezione");
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (mRecorder != null) {
            try{
                mRecorder.stop();
            }catch (Exception e){
                Log.v("AUDIO","eccezione");
                e.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }
    }

    private class RecorderTask extends TimerTask {
        private MediaRecorder recorder;

        public RecorderTask(MediaRecorder recorder) {
            this.recorder = recorder;
        }
        @Override
        public void run() {
            if(shouldStop){
                return;
            }
            valRumore = recorder.getMaxAmplitude();
            amplitudeDb = 20 * Math.log10((double)Math.abs(valRumore));
            Log.v("RUMORE F:","R: "+amplitudeDb);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager=(PowerManager)getSystemService(POWER_SERVICE);
        wakeLock=powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MYAPP:WAKELOCK");
        wakeLock.acquire(60000);
        sharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        Log.v("MYAPP","Wakelock OK");
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            Log.v("MYAPP","CIAONEE");
            Notification notification=new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_android)
                    .setContentTitle("NoiSens")
                    .setContentText("In uso in background")
                    .build();
            startForeground(1,notification);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        while(true){
            if(shouldStop) {
                stopSelf();
                return;
            }
            sharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
            //Log.v("MYAPP","HandleIntent");
            valIntervallo=sharedPreferences.getInt("valIntervallo",20);
          //  Log.v("INTERVALLO","INT: "+sharedPreferences.getInt("valIntervallo",20));
            start();
            if(valRumore>0){
                getLocation();
                Log.v("POSIZIONE FINALE","LAT: "+lat+" LONG: "+ longi);
                Log.v("RUMORE","VAL: "+valRumore);
                valIntervallo=sharedPreferences.getInt("valIntervallo",20);
                SystemClock.sleep(valIntervallo*1000);
            }
        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                Log.v("LOCATION","L: "+location);
                if (location != null) {
                    try {
                        gp=3;
                        Log.v("LOG","ENTRO");
                        p= sharedPreferences.getString("Privacy","Nessuna");
                        Geocoder geocoder = new Geocoder(Rilevazione.this, Locale.getDefault());
                        posizioni = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Log.v("POSIZIONE","LAT: "+posizioni.get(0).getLatitude()+" LONG: "+ posizioni.get(0).getLongitude());
                        lat=posizioni.get(0).getLatitude();
                        longi=posizioni.get(0).getLongitude();
                        switch (p){
                            case "Nessuna":
                                post="/api/postInsertRilevazioniNoPrivacy/";
                                json=
                                        "{\"geojson\":\"{\\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+longi+", "+lat+"]"+
                                                "},\\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+"}}\"}";
                                Log.v("JSON","j: "+json);
                                postRequest(json);
                                break;
                            case "Dummy Updates":
                                post="/api/postInsertRilevazioniDUMMY/";
                                Random random = new Random();
                                Location lm=new Location(LocationManager.GPS_PROVIDER);
                                float valPrivacyD= sharedPreferences.getFloat("valPrivacyD",0);
                                ArrayList<Location> cords=randPos(longi,lat,3000,5);
                                lm.setLatitude(lat);
                                lm.setLongitude(longi);
                                cords.add(lm);
                                int r=cords.size();
                                ArrayList<Integer> indiciFissi=new ArrayList<Integer>();
                                ArrayList<Integer> indici=new ArrayList<Integer>();
                                ArrayList<Integer> indiciUsati=new ArrayList<Integer>();
                                boolean valReale=false;
                                for(int i=0;i<r;i++){
                                    indiciFissi.add(i);
                                }
                                int d=indiciFissi.size();
                                while(indici.size()!=6){
                                    int r1=indiciFissi.size();
                                    int ind=random.nextInt(r1);
                                    //Log.v("VALORE IND ","I: "+ind);
                                    if(!indiciUsati.contains(ind)){
                                        indici.add(indiciFissi.get(ind));
                                        indiciUsati.add(ind);
                                    }
                                    if(cords.get(ind).getLatitude()==lat && cords.get(ind).getLongitude()==longi){
                                        reale=indici.indexOf(ind);
                                    }
                                }
                                Log.v("INDICE REALE","I: "+reale);
                                //CREAZIONE GEOJSON CON ASSEGNAZIONE CASUALE DELLE POSIZIONI
                                for(int i=0;i<d;i++){
                                    if(i==0){
                                        if(i==reale){
                                            valReale=true;
                                            json="{\"geojson\":\"{\\\"type\\\": \\\"FeatureCollection\\\",\\\"features\\\":[" +
                                                    "{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]"+
                                                    "},\\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                        else{
                                            valReale=false;
                                            json="{\"geojson\":\"{\\\"type\\\": \\\"FeatureCollection\\\",\\\"features\\\":[" +
                                                    "{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]"+
                                                    "},\\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                    }
                                    else if(i==d-1){
                                        if(i==reale){
                                            valReale=true;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{" +
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]" +
                                                    "}, \\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\""+valReale+"\\\"}}]}\"}";
                                        }
                                        else{
                                            valReale=false;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{" +
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]" +
                                                    "}, \\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\""+valReale+"\\\"}}]}\"}";
                                        }
                                    }
                                    else{
                                        if(i==reale){
                                            valReale=true;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]"+
                                                    "},\\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                        else{
                                            valReale=false;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]"+
                                                    "},\\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                    }
                                }
                                float distanzaD=mPrivacyD(cords);
                                listener_obj.sendDD(distanzaD);
                                Log.v("Distanza D","D: "+distanzaD);
                                Log.v("JSON","j: "+json);
                                postRequest(json);
                                break;
                            case "GPS Perturbation":
                                post="/api/postInsertRilevazioniGPSP/";
                                double latF;
                                double longiF;
                                String lattext = Double.toString(Math.abs(lat));
                                String longitext=Double.toString(Math.abs(longi));
                                int latintegerPlaces = lattext.indexOf('.');
                                int nlat = lattext.length() - latintegerPlaces - 1;
                                Log.v("Decimali LAT", "N: "+nlat);
                                int longiintegerPlaces = longitext.indexOf('.');
                                int nlongi = longitext.length() - longiintegerPlaces - 1;
                                Log.v("Decimali LONGI", "N: "+nlongi);
                                float fgp=Math.round(sharedPreferences.getFloat("alfa",0)*10);
                                fgp/=10;
                                Log.v("FGP","FP: "+fgp);
                                float gpLat=Math.round(nlat*fgp);
                                float gpLongi=Math.round(nlongi*fgp);
                                Log.v("GP??","GPLAT: "+gpLat);
                                Log.v("GP??","GPLONGI: "+gpLongi);
                                latF=lat*Math.pow(10,gpLat);
                                latF=Math.floor(latF);
                                latF=latF/Math.pow(10,gpLat);
                                Log.v("GP","LATF: "+latF);
                                longiF=longi*Math.pow(10,gpLongi);
                                longiF=Math.floor(longiF);
                                longiF=longiF/Math.pow(10,gpLongi);
                                Log.v("GP","LONGIF: "+longiF);
                                Location lF = new Location(LocationManager.GPS_PROVIDER);
                                lF.setLatitude(latF);
                                lF.setLongitude(longiF);
                                float distanzaP=mPrivacyP(lF,location);
                                listener_obj.sendDP(distanzaP);
                                Log.v("Distanza:","D: "+distanzaP);
                                json=
                                        "{\"geojson\":\"{\\\"type\\\": \\\"FeatureCollection\\\",\\\"features\\\":[" +
                                                "{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+longiF+", "+latF+"]"+
                                                "},\\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\"false\\\"}}," +
                                                "{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{" +
                                                "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+longi+", "+lat+"]" +
                                                "}, \\\"properties\\\": {\\\"Rumore\\\": "+amplitudeDb+",\\\"Reale\\\": \\\"true\\\"}}]}\"}";
                                Log.v("JSON","j: "+json);
                                Log.v("METRICA PRIVACY","P: "+distanzaP);
                                postRequest(json);
                                break;
                        }
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        return;
    }

    public void postRequest(String json){
        final String savedata=json;
        RequestQueue requestQueue= Volley.newRequestQueue(getApplicationContext());
        String sito="https://d9ab-2-37-166-163.ngrok.io";
        String url=sito+post;
        StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject obj = null;
                double res = 0;
                switch (p) {
                    case ("Nessuna"):
                        try {
                            obj = new JSONObject(response);
                            res = obj.getDouble("rumore_medio");
                            Log.v("R", "VAL R:" + res);
                            listener_obj.onResultReceived(Double.toString(res),Double.toString(amplitudeDb));
                            listener_obj.sendQosN(obj.getDouble("qos"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ("Dummy Updates"):
                        try {
                            obj = new JSONObject(response);
                            JSONArray listaRM = obj.getJSONArray("rumore_medio");
                            JSONArray listaPos = obj.getJSONArray("posizione");
                            JSONArray pos;
                            for (int i = 0; i < listaPos.length(); i++) {
                                pos = listaPos.getJSONArray(i);
                                double valLat = pos.getDouble(1);
                                double valLongi = pos.getDouble(0);
                                Log.v("VAL","V: "+valLat+" "+valLongi);
                                if (valLat == lat && valLongi == longi) {
                                    double rumore = listaRM.getDouble(i);
                                    Log.v("Rumore", "RM: " + rumore);
                                    listener_obj.onResultReceived(Double.toString(rumore),Double.toString(amplitudeDb));
                                    listener_obj.sendQosD(obj.getDouble("qos"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ("GPS Perturbation"):
                        res = 0;
                        try {
                            obj = new JSONObject(response);
                            res = obj.getDouble("rumore_medio");
                            listener_obj.sendQosP(obj.getDouble("qos"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.v("R", "VAL R:" + res);
                        listener_obj.onResultReceived(Double.toString(res),Double.toString(amplitudeDb));
                        break;
                }
                    Toast.makeText(getApplicationContext(),"Rilevazione inviata con successo!",Toast.LENGTH_LONG).show();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("Volley Errore","errore: "+error.getMessage());
                //Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        })
        {
            @Override
            public String getBodyContentType(){
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try{
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee){
                    Log.v("POST","Formato non supportato");
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api-key", AK);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    public static ArrayList<Location> randPos(double x0, double y0, int radius,int rip) {
        Random random = new Random();

        ArrayList<Location> l=new ArrayList<>();
        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;
        for(int i=0;i< rip;i++){
            double u = random   .nextDouble();
            double v = random.nextDouble();
            double w = radiusInDegrees * Math.sqrt(u);
            double t = 2 * Math.PI * v;
            double x = w * Math.cos(t);
            double y = w * Math.sin(t);

            // Adjust the x-coordinate for the shrinking of the east-west distances
            double new_x = x / Math.cos(Math.toRadians(y0));

            double foundLongitude = new_x + x0;
            double foundLatitude = y + y0;
            Location lm=new Location(LocationManager.GPS_PROVIDER);
            lm.setLongitude(foundLongitude);
            lm.setLatitude(foundLatitude);
            l.add(lm);

            Log.d("RAND POS PER DUMMIES","Longitude: " + foundLongitude + "  Latitude: " + foundLatitude );
        }
        //System.out.println("Longitude: " + foundLongitude + "  Latitude: " + foundLatitude );
        for(int i=0;i<l.size();i++){
            Log.d("ELEMENTI DI ARRAYLIST PER DUMMIES","L"+"Longitude: " + l.get(i).getLatitude() + "  Latitude: " + l.get(i).getLongitude());
        }
        return l;
    }

    private String getFilePath(){
        ContextWrapper contextWrapper=new ContextWrapper(getApplicationContext());
        File md=contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File f= new File(md,"test"+".mp3");
        return f.getPath();
    }

    public float mPrivacyP(Location lF,Location l){
        float distanza=l.distanceTo(lF);
        return distanza;
    }

    public float mPrivacyD(ArrayList<Location> l){
        float distanza=0;
        for(int i=0;i<l.size();i++){
            if(i!=reale){
                distanza+=l.get(reale).distanceTo(l.get(i));
            }
        }
        distanza/=l.size()-1;
        return distanza;
    }

}
