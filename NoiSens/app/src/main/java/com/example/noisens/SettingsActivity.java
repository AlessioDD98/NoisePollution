package com.example.noisens;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class SettingsActivity extends AppCompatActivity {

    final String sharedPrefs = "sharedPrefs";
    SharedPreferences sharedPreferences;
    int valIntervallo;
    String p;
    RadioGroup radioGroup;
    RadioButton radioButton;
    EditText Eintervallo;
    AlertDialog.Builder builder;
    Button f;
    SeekBar sk;
    TextView progresso;
    float val;

    public void modificaGrafica(RadioButton radioButton) {
        TextView t=findViewById(R.id.textView);
        if(radioButton.getText().equals("GPS Perturbation")){
            sk.setVisibility(View.VISIBLE);
            progresso.setVisibility(View.VISIBLE);
            t.setVisibility(View.VISIBLE);
        }
        else{
            sk.setVisibility(View.INVISIBLE);
            progresso.setVisibility(View.INVISIBLE);
            t.setVisibility(View.INVISIBLE);
        }
    }

    public void seebar(){
        sharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
        sk=findViewById(R.id.barra);
        progresso=findViewById(R.id.Progress);
        val = sharedPreferences.getFloat("alfa", 0);
        val*=100;
        sk.setProgress(Math.round(val));
        progresso.setText("Valore impostato: "+(float)sk.getProgress()/100+" / "+sk.getMax()/100);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    val =i;
                    progresso.setText("Valore impostato: "+ val /100+" / "+sk.getMax()/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progresso.setText("Valore impostato: "+ val/100+" / "+sk.getMax()/100);
                sharedPreferences.edit().putFloat("alfa", val/100).apply();
                Log.v("SI","SISISI");
            }
        });
    }

    public AlertDialog creaAlert(String titolo, final String messaggio, final String campo,
                                 final String risultato, EditText et, final int defVal, final TextView r) {
        builder = new AlertDialog.Builder(this);
        builder.setTitle(titolo);
        et = new EditText(this);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(et);
        final EditText finalEt = et;
        builder.setPositiveButton("Salva", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int p = Integer.parseInt(finalEt.getText().toString());
                sharedPreferences.edit().putInt(campo, p).apply();
                r.setText(messaggio + sharedPreferences.getInt(campo, defVal));
                Toast.makeText(getApplicationContext(), risultato, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        seebar();
        Button MIntervallo=findViewById(R.id.ModIntervallo);
        radioGroup=findViewById(R.id.radioGroup);
        sharedPreferences = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isFirstTime",false).apply();
        valIntervallo=sharedPreferences.getInt("valIntervallo",20);
        p=sharedPreferences.getString("Privacy","Nessuna");
        f=findViewById(R.id.fatto);
        f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        switch (p){
            case ("Nessuna"):
                RadioButton b=findViewById(R.id.Nessuna);
                b.setChecked(true);
                modificaGrafica(b);
                break;
            case("GPS Perturbation"):
                RadioButton c=findViewById(R.id.GPS_Perturbation);
                c.setChecked(true);
                modificaGrafica(c);
                break;
            case("Dummy Updates"):
                RadioButton d=findViewById(R.id.DummyUpdates);
                d.setChecked(true);
                modificaGrafica(d);
                break;
        }
        TextView intervallo=findViewById(R.id.Intervallo);
        intervallo.setText("Imposta intervallo di rilevamento in secondi: "+valIntervallo);
        final AlertDialog ai=creaAlert("Inserisci intervallo di rilevamento (in secondi)","Imposta intervallo di rilevamento (in secondi): ","valIntervallo","Intervallo modificato",Eintervallo,20,intervallo);
        MIntervallo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ai.show();
            }
        });
    }

    public void checkButton(View v){
        int radioId=radioGroup.getCheckedRadioButtonId();
        radioButton=findViewById(radioId);
        sharedPreferences.edit().putString("Privacy", (String) radioButton.getText()).apply();
        Log.d("Pref:","Pref: "+sharedPreferences.getString("Privacy","Nessuna"));
        modificaGrafica(radioButton);
    }
}