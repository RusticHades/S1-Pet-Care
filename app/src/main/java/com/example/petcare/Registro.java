package com.example.petcare;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
import java.util.Base64;

public class Registro extends AppCompatActivity {

    private TextView txtInicioSesion;

    private EditText txtUsuario, txtCorreoElectronico, txtContrasenia;
    private Button btnGuardar;
    private ImageView imgFotoPerfil;

    private static final int CODIGO_SELECCIONAR_IMAGEN = 100;
    private static final int CODIGO_TOMAR_FOTO = 200;
    private static final int CODIGO_PERMISOS_ALMACENAMIENTO = 300;
    private static final int CODIGO_PERMISOS_CAMARA = 400;
    private Uri fotoPerfilUri;
    byte [] fotoPerfilBytes;

    private String tipoUsuario = "usuario";

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
                    registrarUsuario(usuario, correoElectronico, contrasenia, tipoUsuario, fotoPerfilBytes);
                }
            }
        });
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

    private void registrarUsuario(String usuario, String correoElectronico, String contrasenia, String tipoUsuario, byte[] fotoPerfil) {
        String url = "http://192.168.0.192:8080/miapp/guardar_usuario.php";

        String aux;

        if (fotoPerfil == null || fotoPerfil.length == 0) {
            // Convertir drawable a bitmap
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.usuario_predeterminado);

            // Convertir bitmap a bytes
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] defaultImageBytes = stream.toByteArray();

            // Convertir a Base64 (usando android.util.Base64)
            aux = android.util.Base64.encodeToString(defaultImageBytes, android.util.Base64.NO_WRAP);
        } else {
            // Usar la foto proporcionada
            aux = android.util.Base64.encodeToString(fotoPerfil, android.util.Base64.NO_WRAP);

            // Limitar tamaño si es muy grande
            if (aux.length() > 500000) {
                aux = aux.substring(0, 500000);
                Log.w("REGISTRO", "Imagen demasiado grande, se recortó");
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

                String postData = "usuario=" + URLEncoder.encode(usuario, "UTF-8") +
                        "&correo=" + URLEncoder.encode(correoElectronico, "UTF-8") +
                        "&contrasenia=" + URLEncoder.encode(contrasenia, "UTF-8") +
                        "&tipo_usuario=" + URLEncoder.encode(tipoUsuario, "UTF-8") +
                        "&foto_usuario=" + URLEncoder.encode(fotoPerfilBase64, "UTF-8");

                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                }

                int responseCode = urlConnection.getResponseCode();
                InputStream inputStream = responseCode == HttpURLConnection.HTTP_OK ?
                        urlConnection.getInputStream() : urlConnection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(Registro.this, message, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Registro.this, InicioDeSesion.class));
                            finish();
                        } else {
                            Toast.makeText(Registro.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(Registro.this,
                                "Error en formato de respuesta: " + response.toString(),
                                Toast.LENGTH_LONG).show();
                    });
                    e.printStackTrace();
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(Registro.this,
                            "Error de conexión: " + e.getMessage(),
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
}