package com.example.braillecranwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

public class MainActivity extends Activity {

    EditText editText;

    FromWearToMobileService fromWearToMobileService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);

        Intent intent = new Intent();
        
    }
}
