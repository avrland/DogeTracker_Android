package kowoof.dogetracker;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


import com.dlazaro66.qrcodereaderview.QRCodeReaderView;


/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

public class wallet_qr_read extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {
    private QRCodeReaderView qrCodeReaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_qr_read);
        qrCodeReaderView = findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);
        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(1000L);
        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Intent i = new Intent(getApplicationContext(), wallet_add.class);
        if(checkIfQuickScan()){
            i = new Intent(getApplicationContext(), MainActivity.class);
        }
        i.putExtra("wallet_address", text); //show wallet_view what wallet I wanna see
        i.putExtra("readed_qr_code", 1); //show wallet_view what wallet I wanna see
        startActivity(i);
        finish();
    }
    @Override
    protected void onResume() {
        super.onResume();
        qrCodeReaderView.startCamera();
    }
    @Override
    protected void onPause() {
        super.onPause();
        qrCodeReaderView.stopCamera();
    }
    private Boolean checkIfQuickScan(){
            //If we use qr code reader, we insert here scanned address
            Bundle qrReaderMessage = getIntent().getExtras();
            if(qrReaderMessage!=null) {
                Boolean quickScan = qrReaderMessage.getBoolean("quickScan");
                return quickScan;
            }
            return false;
    }

}
