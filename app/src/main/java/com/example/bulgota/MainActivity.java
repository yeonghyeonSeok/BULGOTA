package com.example.bulgota;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button deviceMapBtn;
    Button breathTestingBtn;
    Button detoxAnalysisBtn;
    Button QRCodeScanBtn;
    Button certCompletionBtn;
    Button splashActivityBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceMapBtn = findViewById(R.id.btn_device_map);
        breathTestingBtn = findViewById(R.id.btn_breath_testing);
        detoxAnalysisBtn = findViewById(R.id.btn_detox_analysis);
        QRCodeScanBtn = findViewById(R.id.btn_QRcode_scan);
        certCompletionBtn = findViewById(R.id.btn_cert_completion);
        splashActivityBtn = findViewById(R.id.btn_splash);

        deviceMapBtn.setOnClickListener(this);
        breathTestingBtn.setOnClickListener(this);
        detoxAnalysisBtn.setOnClickListener(this);
        QRCodeScanBtn.setOnClickListener(this);
        certCompletionBtn.setOnClickListener(this);
        splashActivityBtn.setOnClickListener(this);

    } //view 객체 획득

    @Override
    public void onClick(View v) {

        if(v == deviceMapBtn) {
            Intent intent = new Intent(this,DeviceMapActivity.class);
            startActivity(intent);
        } else if (v == detoxAnalysisBtn) {
            Intent intent = new Intent(this,DetoxAnalysisActivity.class);
            startActivity(intent);
        } else if (v == QRCodeScanBtn) {
            Intent intent = new Intent(this,QRCodeScanActivity.class);
            startActivity(intent);
        } else if (v == certCompletionBtn) {
            Intent intent = new Intent(this,CertCompletionActivity.class);
            startActivity(intent);
        } else if (v == splashActivityBtn) {
            Intent intent = new Intent(this,SplashActivity.class);
            startActivity(intent);
        }



    }
}