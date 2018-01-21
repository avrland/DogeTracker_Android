package kowoof.dogetracker;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

public class wallet_qr_read extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {
    private QRCodeReaderView qrCodeReaderView;
    String readed_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_qr_read);
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        qrCodeReaderView = findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        // Use this function to enable/disable decoding
        qrCodeReaderView.setQRDecodingEnabled(true);
        // Use this function to change the autofocus interval (default is 5 secs)
        qrCodeReaderView.setAutofocusInterval(2000L);
        // Use this function to set back camera preview
        qrCodeReaderView.setBackCamera();
    }

    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    @Override
    public void onQRCodeRead(String text, PointF[] points) {
        Intent i = new Intent(getApplicationContext(), wallet_add.class);
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

}
