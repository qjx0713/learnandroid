package indi.qjx.learnandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import indi.qjx.nativelib.CCallJava;
import indi.qjx.nativelib.JavaCallC;
import indi.qjx.nativelib.NativeLib;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn1 =  findViewById(R.id.btn_hello);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CCallJava cCallJava = new CCallJava();
//                cCallJava.callbackHelloFromJava();
//                cCallJava.callbackPrintString();
                cCallJava.callbackSayHello();

            }
        });
    }
}