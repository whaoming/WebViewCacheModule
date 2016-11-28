package com.wxxiaomi.ming.webviewcachemodule;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wxxiaomi.ming.webviewcachemodule.version_1.Version_1_Activity;
import com.wxxiaomi.ming.webviewcachemodule.version_2.Version_2_Activity;
import com.wxxiaomi.ming.webviewcachemodule.version_3.Compress_Cache_Base64_Activity;
import com.wxxiaomi.ming.webviewcachemodule.version_4.StreamActivity;

/**
 *
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_1;
    private Button btn_2;
    private Button btn_3;
    private Button btn_4;
    private Button btn_5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_1 = (Button) findViewById(R.id.btn_1);
        btn_1.setOnClickListener(this);
        btn_2 = (Button) findViewById(R.id.btn_2);
        btn_2.setOnClickListener(this);
        btn_3 = (Button) findViewById(R.id.btn_3);
        btn_3.setOnClickListener(this);
        btn_4 = (Button) findViewById(R.id.btn_4);
        btn_4.setOnClickListener(this);
        btn_5 = (Button) findViewById(R.id.btn_5);
        btn_5.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){

            case R.id.btn_1:
                intent = new Intent(this, Version_1_Activity.class);
                startActivity(intent);
                break;
            case R.id.btn_2:
                 intent = new Intent(this, Version_2_Activity.class);
                startActivity(intent);
                break;
            case R.id.btn_3:
               intent = new Intent(this, Compress_Cache_Base64_Activity.class);
                startActivity(intent);
                break;
            case R.id.btn_4:
                intent = new Intent(this, StreamActivity.class);
                startActivity(intent);
                break;
//            case R.id.btn_1:
//                Intent intent = new Intent(this, Version_1_Activity.class);
//                startActivity(intent);
//                break;
        }
    }


}
