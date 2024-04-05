package com.example.noisens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements Listener {

    final String sharedPrefs = "sharedPrefs";
    SharedPreferences sharedPreferences;
    FusedLocationProviderClient fusedLocationProviderClient;
    Switch r;
    List<Address> posizioni;
    double lat;
    double longi;
    TextView RM;
    TextView RR;
    String AK = "xYzMaOPlFhrOt353:256hgf;214LRTGI£&((&LFURjs984";
    String post;
    String json;
    boolean ril = false;
    String p;
    int reale;
    Button tQoS;
    Button tP;
    Button m;
    float distanzaD;
    float distanzaP;
    double qosP;
    double qosD;
    float fgp;
    double qosN;
    Button orm;

//*******************************Gestione Permessi********************************************************************************************

    public void isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) + checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) + checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) + checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) + checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) + checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("D", "SONO QUI");
                return;
            } else {
                //ActivityCompat.requestPermissions(this, AUDIO, 1);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO}, 1);
                if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    return;
                new AlertDialog.Builder(this)
                        .setTitle("Permesso posizione in background")
                        .setMessage("Fornire i permessi di posizione in background?")
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1);
                return;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            return;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permesso ottenuto", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Permesso rifiutato", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


//******************************************************************************************************************************


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Rilevazione(MainActivity.this);
        sharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Intent is = new Intent(this, Rilevazione.class);
        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);
        if (isFirstTime) {
            startActivity(new Intent(this, SettingsActivity.class));
            isPermissionGranted();
        }
        turnGPSOn();
        getLocation();
        r = findViewById(R.id.SRileva);
        RM = findViewById(R.id.RMedio);
        RR = findViewById(R.id.RR);
        tQoS=findViewById(R.id.mQoS);
        tP=findViewById(R.id.mP);
        m=findViewById(R.id.t);
        orm=findViewById(R.id.OttieniRM);
        orm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
        tP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p= sharedPreferences.getString("Privacy","Nessuna");
                if(p.equals("Dummy Updates")){
                    Toast.makeText(getApplicationContext(),"Metrica privacy: "+distanzaD+" metri",Toast.LENGTH_LONG).show();

                }
                else if(p.equals("GPS Perturbation")){
                    Toast.makeText(getApplicationContext(),"Metrica privacy: "+distanzaP+" metri",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Metrica Privacy: 0 metri",Toast.LENGTH_LONG).show();
                }
            }
        });
        tQoS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p= sharedPreferences.getString("Privacy","Nessuna");
                if(p.equals("Dummy Updates")){
                    if(qosD==-1){
                        Toast.makeText(getApplicationContext(),"Non ci sono abbastanza rilevazioni per calcolare il QoS",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Metrica QoS: "+qosD,Toast.LENGTH_LONG).show();
                    }
                }
                else if(p.equals("GPS Perturbation")){
                    if(qosP==-1){
                        Toast.makeText(getApplicationContext(),"Non ci sono abbastanza rilevazioni per calcolare il QoS",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Metrica QoS: "+qosP,Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    if(qosN==-1){
                        Toast.makeText(getApplicationContext(),"Non ci sono abbastanza rilevazioni per calcolare il QoS",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Metrica QoS: "+qosN,Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        m.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                p= sharedPreferences.getString("Privacy","Nessuna");
                if(p.equals("GPS Perturbation")) {

                    // VADO AD IMPOSTARE UN RANGE [X,Y] UGUALE PER QOS E PRYVACY
                    int qosP_MAX = 35;
                    int distanzaP_MAX = 60000;
                    double qosP_local = (qosP*100)/qosP_MAX;
                    double distanzaP_local = (distanzaP*100)/distanzaP_MAX;
                    if(qosP>qosP_MAX){
                        qosP_local=100;
                    }
                    if(distanzaP>distanzaP_MAX){
                        distanzaP_local=100;
                    }
                    double tradeoff= ((fgp*qosP_local)+((1-fgp)*distanzaP_local));
                    Toast.makeText(getApplicationContext(),"Metrica Tradeoff: "+tradeoff,Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Questa metrica è visualizzabile solo per GPSP",Toast.LENGTH_LONG).show();
                }
            }
        });
        r.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ContextCompat.startForegroundService(MainActivity.this, is);
                    ril = true;
                } else {
                    stopService(is);
                }
            }
        });
    }

    public void turnGPSOn() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MainActivity.this,
                                        LocationRequest.PRIORITY_HIGH_ACCURACY);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LocationRequest.PRIORITY_HIGH_ACCURACY:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.i("TAG", "onActivityResult: GPS Enabled by user");
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Log.i("TAG", "onActivityResult: User rejected GPS request");
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        isPermissionGranted();
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        p= sharedPreferences.getString("Privacy","Nessuna");
                        Log.v("LOG","ENTRO");
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        posizioni = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Log.v("POSIZIONE","LAT: "+posizioni.get(0).getLatitude()+" LONG: "+ posizioni.get(0).getLongitude());
                        lat=posizioni.get(0).getLatitude();
                        longi=posizioni.get(0).getLongitude();
                        Log.v("P","P: "+p);
                        switch (p){
                            case "Nessuna":
                                post="/api/postAVGNoiseNoPrivacy/";
                                json=
                                        "{\"geojson\" : \"{\\\"type\\\": \\\"Point\\\",\\\"coordinates\\\":  [ "+longi+", "+lat+" ]}\"}";
                                Log.v("JSON","j: "+json);
                                postRequest(json);
                                break;
                            case "Dummy Updates":
                                post="/api/postAVGNoiseDummy/";
                                Random random = new Random();
                                Location lm=new Location(LocationManager.GPS_PROVIDER);
                                ArrayList<Location> cords=randPos(longi,lat,3000,5);
                                lm.setLatitude(lat);
                                lm.setLongitude(longi);
                                cords.add(lm);
                                reale=cords.size()-1;
                                int r=cords.size();
                                for (int i=0;i<cords.size();i++){
                                    Log.v("C","Cords: "+cords.get(i).getLongitude());
                                }
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
                                                    "},\\\"properties\\\": {\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                        else{
                                            valReale=false;
                                            json="{\"geojson\":\"{\\\"type\\\": \\\"FeatureCollection\\\",\\\"features\\\":[" +
                                                    "{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]"+
                                                    "},\\\"properties\\\": {\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                    }
                                    else if(i==d-1){
                                        if(i==reale){
                                            valReale=true;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{" +
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]" +
                                                    "}, \\\"properties\\\": {\\\"Reale\\\": \\\""+valReale+"\\\"}}]}\"}";
                                        }
                                        else{
                                            valReale=false;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{" +
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]" +
                                                    "}, \\\"properties\\\": {\\\"Reale\\\": \\\""+valReale+"\\\"}}]}\"}";
                                        }
                                    }
                                    else{
                                        if(i==reale){
                                            valReale=true;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]"+
                                                    "},\\\"properties\\\": {\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                        else{
                                            valReale=false;
                                            json=json+"{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                    "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+cords.get(indici.get(i)).getLongitude()+", "+cords.get(indici.get(i)).getLatitude()+"]"+
                                                    "},\\\"properties\\\": {\\\"Reale\\\": \\\""+valReale+"\\\"}},";
                                        }
                                    }
                                }
                                distanzaD=mPrivacyD(cords);
                                Log.v("Distanza D","D: "+distanzaD);
                                Log.v("JSON","j: "+json);
                                postRequest(json);
                                break;
                            case "GPS Perturbation":
                                post="/api/postAVGNoiseGPSP/";
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
                                fgp=Math.round(sharedPreferences.getFloat("alfa",0)*10);
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
                                distanzaP=mPrivacyP(lF,location);
                                Log.v("Distanza:","D: "+distanzaP);
                                json=
                                        "{\"geojson\":\"{\\\"type\\\": \\\"FeatureCollection\\\",\\\"features\\\":[" +
                                                "{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{"+
                                                "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+longiF+", "+latF+"]"+
                                                "},\\\"properties\\\": {\\\"Reale\\\": \\\"false\\\"}}," +
                                                "{ \\\"type\\\": \\\"Feature\\\", \\\"geometry\\\":{" +
                                                "\\\"type\\\": \\\"Point\\\", \\\"coordinates\\\": ["+longi+", "+lat+"]" +
                                                "}, \\\"properties\\\": {\\\"Reale\\\": \\\"true\\\"}}]}\"}";
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("ERRORE POSIZIONE","E: "+e);
            }
        });
        return;
    }

    public void postRequest(String json){
        final String savedata= json;
        RequestQueue requestQueue=Volley.newRequestQueue(MainActivity.this);
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
                                qosN=obj.getDouble("qos");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.v("R", "VAL R:" + res);
                            if(res==-1){
                                RM.setText("Non ci sono abbastanza rilevazioni per calcolare il rumore medio");
                            }
                            else{
                                RM.setText("Rumore medio: " + res);
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
                                        if(rumore==-1){
                                            RM.setText("Non ci sono abbastanza rilevazioni per calcolare il rumore medio");
                                        }
                                        else{
                                            RM.setText("Rumore medio: " + rumore);
                                        }
                                        qosD=obj.getDouble("qos");
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
                                qosP=obj.getDouble("qos");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.v("R", "VAL R:" + res);
                            if(res==-1){
                                RM.setText("Non ci sono abbastanza rilevazioni per calcolare il rumore medio");
                            }
                            else{
                                RM.setText("Rumore medio: " + res);
                            }
                            break;
                    }
                Toast.makeText(getApplicationContext(),"Richiesta di rumore medio inviata con successo!",Toast.LENGTH_LONG).show();
                Log.v("OBJ",obj.toString());
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),error.getMessage(),Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    Log.v("POST", "Formato non supportato");
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
            double u = random.nextDouble();
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

        return l;
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

    public float mPrivacyP(Location lF,Location l){
        float distanza=l.distanceTo(lF);
        return distanza;
    }

    @Override
    public void onResultReceived(String strRM,String strRR) {
        if(Double.parseDouble(strRM)==-1){
            RM.setText("Non ci sono abbastanza rilevazioni per calcolare il rumore medio");
        }else{
            RM.setText("Rumore medio: "+ strRM);
        }
        RR.setText("Rumore rilevato: "+strRR);
    }

    @Override
    public void sendDP(float dP) {
        distanzaP=dP;
    }

    @Override
    public void sendDD(float dD) {
        distanzaD=dD;
    }

    @Override
    public void sendQosP(double qp) {
        qosP=qp;
    }

    @Override
    public void sendQosD(double qd) {
        qosD=qd;
    }

    @Override
    public void sendQosN(double qn) {
        qosN=qn;
    }
}