package com.example.q335.tracker;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    private Button b3;
    private Button settingsButton;


    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b3= (Button) findViewById(R.id.buttonA);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Long tsLong = System.currentTimeMillis()/1000;
                Log(b3.getText().toString() + " " + tsLong.toString(),"log.txt");
            }
        });

        settingsButton = (Button) findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),Settings.class),1);
            }
        });

        if (isExternalStorageWritable()) {
            Toast.makeText(MainActivity.this, "External Storage is Writeable", Toast.LENGTH_SHORT).show();
        }

        //TODO: Request Permissions

        //TODO: SharedPreferences

    }

    public boolean Log(String data, String fname) {
        File file = new File(path, fname);

        try {
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file, true);

            out.write(data.getBytes());
            out.write(System.getProperty("line.separator").getBytes());
            out.close();
            return true;
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, path, Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    private boolean Save(String data, String path, String fname) {
        File file = new File(path, fname);

        try {
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file, true);

            out.write(data.getBytes());
            out.close();
            return true;

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, path, Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK){
                Bundle extras = data.getExtras();

                Toast.makeText(this, extras.getString("A").toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
