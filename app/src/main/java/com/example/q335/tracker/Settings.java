package com.example.q335.tracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.Map;

public class Settings extends AppCompatActivity {

    private EditText TextA;
    private EditText TextB;
    private EditText TextC;
    private EditText TextD;

    SharedPreferences Events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Events = PreferenceManager.getDefaultSharedPreferences(this);

        TextA = (EditText) findViewById(R.id.editTextA);
        TextB = (EditText) findViewById(R.id.editTextB);
        TextC = (EditText) findViewById(R.id.editTextC);
        TextD = (EditText) findViewById(R.id.editTextD);

        TextA.setText(Events.getString("A", "A: Place Event Text here"));
        TextB.setText(Events.getString("B", "B: Place Event Text here"));
        TextC.setText(Events.getString("C", "C: Place Event Text here"));
        TextD.setText(Events.getString("D", "D: Place Event Text here"));

        Button but_OK = (Button) findViewById(R.id.buttonOK);
        but_OK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = Events.edit();
                editor.putString("A",TextA.getText().toString());
                editor.putString("B",TextB.getText().toString());
                editor.putString("C",TextC.getText().toString());
                editor.putString("D",TextD.getText().toString());
                editor.apply();
                finish();
            }
        });

        Button exportButton = (Button) findViewById(R.id.buttonExport);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dstPath = Environment.getExternalStorageDirectory() + File.separator + "tracker" + File.separator;
                File dst = new File(dstPath,"log.txt");
                File src = new File(getFilesDir(),"log.txt");
                try {
                    exportFile(src, dst);
                } catch (Exception e) {
                    Toast.makeText(Settings.this, "Log Export Failed!", Toast.LENGTH_SHORT).show();
                }

                boolean success = saveSharedPreferencesToFile(new File(dstPath,"prefs.txt"));
                if (!success)
                    Toast.makeText(Settings.this, "Export preferences failed!", Toast.LENGTH_SHORT).show();
            }
        });
        //TODO: import
    }

    private File exportFile(File src, File dst) throws IOException {
        //TODO: Request Permissions

        //if folder does not exist
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

    private boolean saveSharedPreferencesToFile(File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref = Events;
            output.writeObject(pref.getAll());
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    @SuppressWarnings({ "unchecked" })
    private boolean loadSharedPreferencesFromFile(File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = Events.edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                else if (v instanceof Float)
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                else if (v instanceof Integer)
                    prefEdit.putInt(key, ((Integer) v).intValue());
                else if (v instanceof Long)
                    prefEdit.putLong(key, ((Long) v).longValue());
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.commit();
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }
}
