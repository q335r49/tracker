package com.example.q335.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextA = (EditText) findViewById(R.id.editTextA);
        TextB = (EditText) findViewById(R.id.editTextB);
        TextC = (EditText) findViewById(R.id.editTextC);
        TextD = (EditText) findViewById(R.id.editTextD);

        cancel_Button= (Button) findViewById(R.id.button_cancel);
        cancel_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.putExtra("A",TextA.getText().toString());
                intent.putExtra("B",TextB.getText().toString());
                intent.putExtra("C",TextC.getText().toString());
                intent.putExtra("D",TextD.getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

}
