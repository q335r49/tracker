package com.example.q335.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class MainActivity extends AppCompatActivity {

    // List of Events associated with buttons
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
                    Log(Events.getString("A",""),"log.txt");
                    Toast.makeText(MainActivity.this, "Logged:" + Events.getString("A",""), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No Event Associated with Button A", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),Settings.class),1);
            }
        });

        Button exportButton = (Button) findViewById(R.id.buttonExport);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dstPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                File dst = new File(dstPath);
                File src = new File(getFilesDir(),"log.txt");
                try {
                    exportFile(src, dst);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Export Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button visualizeButton = (Button) findViewById(R.id.buttonVisualilze);
        visualizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               //TODO: Visualize
            }
        });
    }

    public boolean Log(String data, String fname) {
        //TODO: Logging Syntax
        File internalFile = new File(getFilesDir(), fname);

        try {
            FileOutputStream out = new FileOutputStream(internalFile,true);
            out.write(data.getBytes());
            out.write(System.getProperty("line.separator").getBytes());
            out.close();
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            Toast.makeText(this, "Internal Storage Write Error!", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private File exportFile(File src, File dst) throws IOException {
        //TODO: Request Permissions
        //TODO: #1 Export shared Preferences
        //TODO: #2 Import shared preferences

        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                return null;
            }
        }

        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return dst;
    }
}