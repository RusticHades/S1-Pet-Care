package com.example.petcare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class QRHandlerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtener datos del intent
        Uri data = getIntent().getData();
        if (data != null && "petcare".equals(data.getScheme())) {
            String path = data.getPath();
            if (path != null && path.startsWith("/mascota/")) {
                String mascotaId = path.replace("/mascota/", "");

                Intent intent = new Intent(this, VistaVeterinarioEnMascotas.class);
                intent.putExtra("id_mascota", mascotaId);
                startActivity(intent);
                finish();
                return;
            }
        }

        // Si no es un QR válido
        Toast.makeText(this, "QR no válido", Toast.LENGTH_SHORT).show();
    }
}