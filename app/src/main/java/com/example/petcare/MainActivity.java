package com.example.petcare;

import android.content.Intent;
import android.graphics.Insets;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Esto ya llama setContentView en BaseActivity

        // Inflar el contenido específico dentro del FrameLayout
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        LayoutInflater.from(this).inflate(R.layout.activity_main, contentFrame);

        // Marcar el item del menu como seleccionado
        navigationView.setCheckedItem(R.id.navMenuInicio);

        EdgeToEdge.enable(this);

        startActivity(new Intent(MainActivity.this, Inicio.class));
        finish();
    }
}