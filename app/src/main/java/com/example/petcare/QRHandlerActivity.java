package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class QRHandlerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();

        // Obtener el ID del usuario que está escaneando (de SharedPreferences, SessionManager, etc.)
        int idUsuario = obtenerIdUsuario(); // Implementa este método según tu lógica de autenticación

        if (data != null) {
            String path = data.getPath(); // Ejemplo: "/mascota/3"
            if (path != null && path.startsWith("/mascota/")) {
                String mascotaId = path.replace("/mascota/", "");
                redirectToVeterinarioMascota(mascotaId, idUsuario);
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private int obtenerIdUsuario() {
        // Ejemplo con SharedPreferences:
        SharedPreferences prefs = getSharedPreferences("MisPreferencias", MODE_PRIVATE);
        return prefs.getInt("id_usuario", 0); // 0 si no existe
    }

    private void redirectToVeterinarioMascota(String mascotaId, int idUsuario) {
        Intent vistaIntent = new Intent(this, VistaVeterinarioEnMascotas.class);
        vistaIntent.putExtra("id_mascota", mascotaId);
        vistaIntent.putExtra("id_usuario", idUsuario); // Pasamos el ID del usuario
        startActivity(vistaIntent);
        finish();
    }
}