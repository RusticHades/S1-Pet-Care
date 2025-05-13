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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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

    private void registrarUsuario(String usuario, String correoElectronico, String contrasenia, String tipoUsuario, byte[] fotoPerfilBytes) {
        new Thread(() -> {
            try {
                // Configurar la conexión
                URL url = new URL("http://192.168.0.192:8080/miapp/guardar_usuario.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Crear los parámetros
                String boundary = "Boundary-" + System.currentTimeMillis();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

                // Añadir parámetros normales
                addFormField(writer, "usuario", usuario, boundary);
                addFormField(writer, "correo", correoElectronico, boundary);
                addFormField(writer, "contrasenia", contrasenia, boundary);
                addFormField(writer, "tipo_usuario", tipoUsuario, boundary);

                // Añadir la imagen si existe
                if (fotoPerfilBytes != null && fotoPerfilBytes.length > 0) {
                    addFilePart(writer, outputStream, "foto_usuario", fotoPerfilBytes, "perfil.jpg", boundary);
                }

                // Finalizar la solicitud
                writer.append("--").append(boundary).append("--").append("\r\n");
                writer.flush();
                writer.close();

                // Obtener la respuesta
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    // Procesar la respuesta JSON
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(() -> {
                        Toast.makeText(Registro.this, message, Toast.LENGTH_SHORT).show();
                        if (success) {
                            startActivity(new Intent(Registro.this, InicioDeSesion.class));
                            finish();
                        }
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(Registro.this, "Error en el servidor: " + responseCode, Toast.LENGTH_SHORT).show());
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(Registro.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Métodos auxiliares para construir la solicitud multipart
    private void addFormField(PrintWriter writer, String name, String value, String boundary) {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n");
        writer.append("\r\n");
        writer.append(value).append("\r\n");
        writer.flush();
    }

    private void addFilePart(PrintWriter writer, OutputStream outputStream, String fieldName, byte[] fileData, String fileName, String boundary) throws IOException {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"\r\n");
        writer.append("Content-Type: image/jpeg\r\n");
        writer.append("Content-Transfer-Encoding: binary\r\n");
        writer.append("\r\n");
        writer.flush();

        outputStream.write(fileData);
        outputStream.flush();

        writer.append("\r\n");
        writer.flush();
    }

}