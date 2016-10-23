package com.example.q335.tracker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.w3c.dom.Text;

public class Settings extends AppCompatActivity {

    private Button cancel_Button;
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

        cancel_Button= (Button) findViewById(R.id.button_cancel);
        cancel_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = Events.edit();
                editor.putString("A",TextA.getText().toString());
                editor.putString("B",TextB.getText().toString());
                editor.putString("C",TextC.getText().toString());
                editor.putString("D",TextD.getText().toString());

                finish();
            }
        });


    }

}
