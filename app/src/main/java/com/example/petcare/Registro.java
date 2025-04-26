package com.example.petcare;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Registro extends AppCompatActivity {

    private TextView txtInicioSesion;

    private EditText txtUsuario, txtCorreoElectronico, txtContrasenia;
    private Button btnGuardar;
    private ImageView imgFotoPerfil;

    private static final int CODIGO_SELECCIONAR_IMAGEN = 100;
    private static final int CODIGO_TOMAR_FOTO = 200;
    private static final int CODIGO_PERMISOS_ALMACENAMIENTO = 300;
    private static final int CODIGO_PERMISOS_CAMARA = 400;
    private Uri imagenUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        txtUsuario = findViewById(R.id.editTextNombreUsuario);
        txtCorreoElectronico = findViewById(R.id.editTextCorreoElectronico);
        txtInicioSesion = findViewById(R.id.buttonInicioSesion);
        txtContrasenia = findViewById(R.id.editTextCrearContrasenia);
        btnGuardar = findViewById(R.id.buttonGuardar);
        imgFotoPerfil = findViewById(R.id.imageViewFotoPerfil);

        imgFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoSeleccionImagen();
            }
        });

        txtInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registro.this, InicioDeSesion.class);
                startActivity(intent);
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usuario = txtUsuario.getText().toString().trim();
                String correoElectronico = txtCorreoElectronico.getText().toString().trim();
                String contrasenia = txtContrasenia.getText().toString().trim();

                if(validarDatos(usuario, correoElectronico, contrasenia)){
                    registrarUsuario(usuario, correoElectronico, contrasenia);
                    startActivity(new Intent(Registro.this, InicioDeSesion.class));
                }
            }
        });
    }
    private void mostrarDialogoSeleccionImagen() {
        final CharSequence[] opciones = {"Tomar foto", "Elegir de galería", "Cancelar"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Seleccionar foto de perfil");
        builder.setItems(opciones, (dialog, which) -> {
            if (opciones[which].equals("Tomar foto")) {
                verificarPermisosCamara();
            } else if (opciones[which].equals("Elegir de galería")) {
                verificarPermisosAlmacenamiento();
            } else {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void verificarPermisosAlmacenamiento() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    CODIGO_PERMISOS_ALMACENAMIENTO);
        } else {
            seleccionarImagenDeGaleria();
        }
    }

    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CODIGO_PERMISOS_CAMARA);
        } else {
            tomarFotoConCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISOS_ALMACENAMIENTO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                seleccionarImagenDeGaleria();
            } else {
                Toast.makeText(this, "Se necesitan permisos para acceder a la galería", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CODIGO_PERMISOS_CAMARA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFotoConCamara();
            } else {
                Toast.makeText(this, "Se necesitan permisos para usar la cámara", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void seleccionarImagenDeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), CODIGO_SELECCIONAR_IMAGEN);
    }

    private void tomarFotoConCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CODIGO_TOMAR_FOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CODIGO_SELECCIONAR_IMAGEN && data != null) {
                imagenUri = data.getData();
                imgFotoPerfil.setImageURI(imagenUri);
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            } else if (requestCode == CODIGO_TOMAR_FOTO && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgFotoPerfil.setImageBitmap(imageBitmap);
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }
    private boolean validarDatos(String usuario, String correoElectronico, String contrasenia){

        if (usuario.isEmpty()) {
            txtUsuario.setError("Ingrese un nombre de usuario");
            txtUsuario.requestFocus();
            return false;
        }

        if (correoElectronico.isEmpty()) {
            txtCorreoElectronico.setError("Ingrese un correo electrónico");
            txtCorreoElectronico.requestFocus();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoElectronico).matches()) {
            txtCorreoElectronico.setError("Correo electrónico inválido");
            txtCorreoElectronico.requestFocus();
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
    private void registrarUsuario(String usuario, String correoElectronico, String contrasenia){

    }
}