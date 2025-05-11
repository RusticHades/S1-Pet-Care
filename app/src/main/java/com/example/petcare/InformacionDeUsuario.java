package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;

public class InformacionDeUsuario extends BaseActivity {

    private TextView txtUsuario, txtTipoUsuario, txtCorreo, txtTelefono, txtDireccion, txtMensaje;
    private ImageView imgFotoPerfil;
    private Button btnEditarPerfil, btnAgregrarMascota;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_informacion_de_usuario, contentFrame, true);

        // Configuración del NavigationView
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerLayout = headerView.findViewById(R.id.headerLayout);
        if (headerLayout != null) {
            headerLayout.setSelected(true);
        }

        // Inicializar vistas
        txtUsuario = findViewById(R.id.textViewUsuario);
        txtTipoUsuario = findViewById(R.id.textViewTipoUsuario);
        txtCorreo = findViewById(R.id.textViewcorreo);
        txtTelefono = findViewById(R.id.textViewTelefono);
        txtDireccion = findViewById(R.id.textViewdireccion);
        txtMensaje = findViewById(R.id.textViewMensaje);

        imgFotoPerfil = findViewById(R.id.imageViewFotoPerfil);

        btnEditarPerfil = findViewById(R.id.buttonEditarPerfil);
        btnAgregrarMascota = findViewById(R.id.buttonAgregarMascota);

        txtCorreo.setSelected(true);

        // Cargar datos del usuario
        cargarDatosUsuario();

        EdgeToEdge.enable(this);

        btnEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InformacionDeUsuario.this, EditarPerfil.class));
            }
        });

        btnAgregrarMascota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InformacionDeUsuario.this, AgregarMascota.class));
            }
        });
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);

        // Obtener datos dl php
        String nombre = prefs.getString("usuario_nombre", "No disponible");
        String correo = prefs.getString("usuario_correo", "No disponible");
        String tipoUsuario = "Tipo de usuario: " + prefs.getString("usuario_tipo", "No disponible");

        String telefono = prefs.getString("usuario_telefono", "");
        telefono = (telefono == null || telefono.isEmpty()) ? "No disponible" : telefono;

        String direccion = prefs.getString("usuario_direccion", "");
        direccion = (direccion == null || direccion.isEmpty()) ? "No disponible" : direccion;

        String fotoBase64 = prefs.getString("usuario_foto", null);
        // Mostrar datos en los TextView
        txtUsuario.setText(nombre);
        txtCorreo.setText(correo);
        txtTipoUsuario.setText(tipoUsuario);
        txtTelefono.setText(telefono);
        txtDireccion.setText(direccion);

        if(!telefono.equals("No disponible") || !direccion.equals("No disponible")){
            txtMensaje.setVisibility(View.GONE);
        }

        // Cargar foto de perfil solo si existe
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgFotoPerfil.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                imgFotoPerfil.setImageResource(R.drawable.usuario_predeterminado);
            }
        } else {
            imgFotoPerfil.setImageResource(R.drawable.usuario_predeterminado);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Restaurar estado del header al salir, esto es para q se cambie el backgroun de color
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerLayout = headerView.findViewById(R.id.headerLayout);
        if (headerLayout != null) {
            headerLayout.setSelected(false);
        }
    }
}