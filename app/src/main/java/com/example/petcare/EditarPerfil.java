package com.example.petcare;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;

public class EditarPerfil extends AppCompatActivity {

    private EditText txtUsuario, txtCorreo, txtTelefono, txtDireccion;
    private ImageView imgFotoPerfil;
    private Button btnGuardarCambios, btnCancelarCambios;

    private static final int CODIGO_SELECCIONAR_IMAGEN = 100;
    private static final int CODIGO_TOMAR_FOTO = 200;
    private static final int CODIGO_PERMISOS_ALMACENAMIENTO = 300;
    private static final int CODIGO_PERMISOS_CAMARA = 400;
    private Uri fotoPerfilUri;
    byte[] fotoPerfilBytes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_perfil);

        // Inicializar vistas
        txtUsuario = findViewById(R.id.eeditTextNombreMascota);
        txtCorreo = findViewById(R.id.editTextCorreoElectronicoEditar);
        txtTelefono = findViewById(R.id.editTextTelefonoEditar);
        txtDireccion = findViewById(R.id.editTextFechaNacimientoMascota);

        imgFotoPerfil = findViewById(R.id.imageViewFotoPerfilEditar);

        btnGuardarCambios = findViewById(R.id.buttonGuardarCambios);
        btnCancelarCambios = findViewById(R.id.buttonCancelarCambios);

        cargarDatosUsuario();

        btnGuardarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarDatos(txtUsuario.getText().toString().trim(), txtCorreo.getText().toString().trim(), txtTelefono.getText().toString().trim(), txtDireccion.getText().toString().trim())) {
                    editarPerfil();
                }
            }
        });

        btnCancelarCambios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditarPerfil.this, InformacionDeUsuario.class));
                finish();
            }
        });

        imgFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoSeleccionImagen();
            }
        });
    }

    private void editarPerfil() {
        String url = "http://192.168.0.192:8080/miapp/actualizar_perfil.php"; // Cambia por tu endpoint de actualización

        // Obtener datos del SharedPreferences para el ID del usuario
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
        String idUsuario = prefs.getString("usuario_id", "");

        // Convertir imagen a Base64 si hay cambios
        String aux = "";
        if (fotoPerfilBytes != null && fotoPerfilBytes.length > 0) {
            aux = Base64.encodeToString(fotoPerfilBytes, Base64.NO_WRAP);

            // Limitar tamaño si es muy grande (opcional)
            if (aux.length() > 500000) {
                aux = aux.substring(0, 500000);
                Log.w("EDICION", "Imagen demasiado grande, se recortó");
            }
        }

        String fotoPerfilBase64 = aux;

        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL urlObj = new URL(url);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Preparar datos para enviar
                String postData = "id=" + URLEncoder.encode(idUsuario, "UTF-8") +
                        "&usuario=" + URLEncoder.encode(txtUsuario.getText().toString().trim(), "UTF-8") +
                        "&correo=" + URLEncoder.encode(txtCorreo.getText().toString().trim(), "UTF-8") +
                        "&telefono=" + (txtTelefono.getText().toString().trim().isEmpty() ?
                        "&foto_usuario=" + URLEncoder.encode(fotoPerfilBase64, "UTF-8") +
                        "NULL" : URLEncoder.encode(txtTelefono.getText().toString().trim(), "UTF-8")) +
                        "&direccion=" + (txtDireccion.getText().toString().trim().isEmpty() ?
                        "NULL" : URLEncoder.encode(txtDireccion.getText().toString().trim(), "UTF-8"));

                // Enviar datos
                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                }

                // Procesar respuesta
                int responseCode = urlConnection.getResponseCode();
                InputStream inputStream = responseCode == HttpURLConnection.HTTP_OK ?
                        urlConnection.getInputStream() : urlConnection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Analizar respuesta JSON
                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(() -> {
                        if (success) {
                            // Actualizar SharedPreferences con los nuevos datos
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("usuario_nombre", txtUsuario.getText().toString().trim());
                            editor.putString("usuario_correo", txtCorreo.getText().toString().trim());
                            editor.putString("usuario_telefono", txtTelefono.getText().toString().trim());
                            editor.putString("usuario_direccion", txtDireccion.getText().toString().trim());

                            if (!fotoPerfilBase64.isEmpty()) {
                                editor.putString("usuario_foto", fotoPerfilBase64);
                            }

                            editor.apply();

                            Toast.makeText(EditarPerfil.this, message, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(EditarPerfil.this, InformacionDeUsuario.class));
                            finish();
                        } else {
                            Toast.makeText(EditarPerfil.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(EditarPerfil.this,
                                "Error en formato de respuesta: " + response.toString(),
                                Toast.LENGTH_LONG).show();
                    });
                    e.printStackTrace();
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(EditarPerfil.this,
                            "Error de conexión: " + obtenerMensajeError(e),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private String obtenerMensajeError(Exception e) {
        if (e instanceof ConnectException || e instanceof UnknownHostException) {
            return "No se pudo conectar al servidor. Verifica tu conexión a Internet.";
        } else if (e instanceof SocketTimeoutException) {
            return "Tiempo de espera agotado. Intenta nuevamente.";
        } else {
            return "Error al registrar: " + e.getMessage();
        }
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);

        // Obtener datos dl php
        String nombre = prefs.getString("usuario_nombre", "No disponible");
        String correo = prefs.getString("usuario_correo", "No disponible");
        String telefono = prefs.getString("usuario_telefono", "");
        String direccion = prefs.getString("usuario_direccion", "");
        String fotoBase64 = prefs.getString("usuario_foto", null);

        // Mostrar datos en los TextView
        txtUsuario.setText(nombre);
        txtCorreo.setText(correo);
        txtTelefono.setText(telefono);
        txtDireccion.setText(direccion);

        // Cargar foto de perfil solo si existe
        if (fotoBase64 != null && !fotoBase64.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(fotoBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                imgFotoPerfil.setImageBitmap(bitmap);
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            } catch (Exception e) {
                e.printStackTrace();
                mostrarFotoPorDefecto();
            }
        } else {
            mostrarFotoPorDefecto();
        }
    }

    private void mostrarFotoPorDefecto() {
        imgFotoPerfil.setImageResource(R.drawable.usuario_predeterminado);
    }

    //Este crea la ventana para seleccionar entre foto de perfil  de archivos
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    CODIGO_PERMISOS_ALMACENAMIENTO);
        } else {
            seleccionarImagenDeGaleria();
        }
    }

    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
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

    //Se actualiza la activity para cargar la imagen, ya sea de la galeria o de la camara y despues se convierte a bytes base64 para guardarla en la base de datos
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CODIGO_SELECCIONAR_IMAGEN && data != null) {
                fotoPerfilUri = data.getData();
                imgFotoPerfil.setImageURI(fotoPerfilUri);
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                try {
                    // Convertir la imagen seleccionada a bytes
                    InputStream inputStream = getContentResolver().openInputStream(fotoPerfilUri);
                    fotoPerfilBytes = getBytes(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == CODIGO_TOMAR_FOTO && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgFotoPerfil.setImageBitmap(imageBitmap);
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                fotoPerfilBytes = stream.toByteArray();
            }
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private boolean validarDatos(String usuario, String correoElectronico, String telefono, String direccion) {
        // Validar usuario
        if (usuario.isEmpty()) {
            txtUsuario.setError("Ingrese un nombre de usuario");
            txtUsuario.requestFocus();
            return false;
        } else if (usuario.length() < 4) {
            txtUsuario.setError("El usuario debe tener al menos 4 caracteres");
            txtUsuario.requestFocus();
            return false;
        }

        // Validar correo
        if (correoElectronico.isEmpty()) {
            txtCorreo.setError("Ingrese un correo electrónico");
            txtCorreo.requestFocus();
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoElectronico).matches()) {
            txtCorreo.setError("Correo electrónico inválido");
            txtCorreo.requestFocus();
            return false;
        }

        // Validar teléfono
        if (!telefono.isEmpty()) {
            if (telefono.length() < 10) {
                txtTelefono.setError("El teléfono debe tener al menos 10 dígitos");
                txtTelefono.requestFocus();
                return false;
            } else if (!telefono.matches("\\d+")) { // Solo números
                txtTelefono.setError("Solo se permiten números");
                txtTelefono.requestFocus();
                return false;
            }
        }

        // Validar dirección
        if (!direccion.isEmpty()) {
            if (direccion.length() < 10) {
                txtDireccion.setError("La dirección es muy corta");
                txtDireccion.requestFocus();
                return false;
            }
        }
        // Si pasa todas las validaciones
        return true;
    }
}