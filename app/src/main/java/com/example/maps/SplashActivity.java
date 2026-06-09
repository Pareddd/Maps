package com.example.maps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Menunda selama 2500 milidetik (2.5 detik) lalu pindah menggunakan INTENT
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // SYARAT LAB: Menggunakan Intent untuk berpindah Activity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);

            // Tutup SplashActivity agar tidak bisa dikembalikan dengan tombol Back
            finish();
        }, 2500);
    }
}