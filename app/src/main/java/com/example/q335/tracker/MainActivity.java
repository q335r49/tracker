package com.example.q335.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;


public class MainActivity extends AppCompatActivity {

    private SharedPreferences Events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Events = PreferenceManager.getDefaultSharedPreferences(this);

        Button but_A = (Button) findViewById(R.id.buttonA);
        but_A.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Events.contains("A")) {
                    //Long tsLong = System.currentTimeMillis()/1000;
                    //Log(but_A.getText().toString() + " " + tsLong.toString(),"log.txt");
                    Log(Events.getString("A", ""), "log.txt");
                    Toast.makeText(MainActivity.this, "Logged:" + Events.getString("A", ""), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No Event Associated with Button A", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(), Settings.class), 1);
            }
        });
    }

    public boolean Log(String data, String fname) {

        String[] commands = data.split("\\|");
        int comlen = commands.length;
        String entry;

        for (int i = 1; i < comlen; i++) {
            switch (commands[i]) {
                case "time":
                    commands[i] = Long.toString(System.currentTimeMillis() / 1000);
                    break;
                //TODO: more advanced functions
            }
        }

        switch (comlen) {
            case 2:
                entry = String.format(commands[0],commands[1]);
                break;
            case 3:
                entry = String.format(commands[0],commands[1],commands[2]);
                break;
            case 4:
                entry = String.format(commands[0],commands[1],commands[2],commands[3]);
                break;
            case 5:
                entry = String.format(commands[0],commands[1],commands[2],commands[3],commands[4]);
                break;
            default:
                entry = "";
                break;
        }

        File internalFile = new File(getFilesDir(), fname);
        try {
            FileOutputStream out = new FileOutputStream(internalFile, true);
            out.write(entry.getBytes());
            out.write(System.getProperty("line.separator").getBytes());
            out.close();
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            Toast.makeText(this, "Internal Storage Write Error!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}

