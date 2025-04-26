package com.example.petcare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InicioDeSesion extends AppCompatActivity {

    private EditText txtUsuario, txtContrasenia;
    private Button btnRegistrar, btnIniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_de_sesion);

        txtUsuario = findViewById(R.id.editTextUsuario);
        txtContrasenia = findViewById(R.id.editTextContrasenia);
        btnRegistrar = findViewById(R.id.buttonRegistrar);
        btnIniciar = findViewById(R.id.buttonIniciar);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InicioDeSesion.this, Registro.class));
            }
        });

        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = txtUsuario.getText().toString();
                String contrasenia = txtContrasenia.getText().toString();

                if(validarDatos(usuario, contrasenia)){
                    startActivity(new Intent(InicioDeSesion.this, Inicio.class));
                    iniciarSesion(usuario, contrasenia);
                }
            }
        });
    }

    private boolean validarDatos(String usuario, String contrasenia){

        if (usuario.isEmpty()) {
            txtUsuario.setError("Ingrese un nombre de usuario");
            txtUsuario.requestFocus();
            return false;
        }

        if (contrasenia.isEmpty()) {
            txtContrasenia.setError("Ingrese una contraseña");
            txtContrasenia.requestFocus();
            return false;
        } else if (contrasenia.length() < 8) {
            txtContrasenia.setError("La contraseña debe tener al menos 8 caracteres");
            txtContrasenia.requestFocus();
            return false;
        }
        return true;
    }

    private void iniciarSesion(String usuario, String contrasenia){
    }
}