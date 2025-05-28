package com.example.petcare;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class EditarPerfil extends AppCompatActivity {

    // Constantes para códigos de solicitud (en español y consistentes)
    private static final int CODIGO_GALERIA = 100;
    private static final int CODIGO_CAMARA = 200;
    private static final int CODIGO_PERMISO_ALMACENAMIENTO = 300;
    private static final int CODIGO_PERMISO_CAMARA = 400;

    // Vistas
    private EditText txtUsuario, txtCorreo, txtTelefono, txtDireccion;
    private ImageView imgFotoPerfil;
    private Button btnGuardarCambios, btnCancelarCambios;

    // Datos de la imagen
    private byte[] fotoPerfilBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        txtUsuario = findViewById(R.id.eeditTextNombreMascota);
        txtCorreo = findViewById(R.id.editTextCorreoElectronicoEditar);
        txtTelefono = findViewById(R.id.editTextTelefonoEditar);
        txtDireccion = findViewById(R.id.editTextFechaNacimientoMascota);
        imgFotoPerfil = findViewById(R.id.imageViewFotoPerfilEditar);
        btnGuardarCambios = findViewById(R.id.buttonGuardarCambios);
        btnCancelarCambios = findViewById(R.id.buttonCancelarCambios);

        cargarDatosUsuario();

        imgFotoPerfil.setOnClickListener(v -> mostrarDialogoSeleccionImagen());
        btnCancelarCambios.setOnClickListener(v -> finish());
        btnGuardarCambios.setOnClickListener(v -> validarYGuardar());

    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);

        txtUsuario.setText(prefs.getString("usuario_nombre", ""));
        txtCorreo.setText(prefs.getString("usuario_correo", ""));
        txtTelefono.setText(prefs.getString("usuario_telefono", ""));
        txtDireccion.setText(prefs.getString("usuario_direccion", ""));

        String fotoUrl = prefs.getString("usuario_foto_url", null);
        if (fotoUrl != null) {
            new CargarImagenTask().execute(fotoUrl);
        }
    }

    private void validarYGuardar() {
        String usuario = txtUsuario.getText().toString().trim();
        String correo = txtCorreo.getText().toString().trim();
        String telefono = txtTelefono.getText().toString().trim();
        String direccion = txtDireccion.getText().toString().trim();

        if (usuario.isEmpty()) {
            txtUsuario.setError("Nombre de usuario requerido");
            return;
        }

        if (correo.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            txtCorreo.setError("Correo electrónico inválido");
            return;
        }

        if (!telefono.isEmpty() && !telefono.matches("\\d{10,15}")) {
            txtTelefono.setError("Teléfono inválido (10-15 dígitos)");
            return;
        }

        new ActualizarPerfilTask().execute(
                usuario,
                correo,
                telefono.isEmpty() ? "NULL" : telefono,
                direccion.isEmpty() ? "NULL" : direccion
        );
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
                    CODIGO_PERMISO_ALMACENAMIENTO);
        } else {
            seleccionarImagenDeGaleria();
        }
    }

    private void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CODIGO_PERMISO_CAMARA);
        } else {
            tomarFotoConCamara();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (requestCode) {
            case CODIGO_PERMISO_ALMACENAMIENTO:
                seleccionarImagenDeGaleria();
                break;
            case CODIGO_PERMISO_CAMARA:
                tomarFotoConCamara();
                break;
        }
    }

    private void seleccionarImagenDeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), CODIGO_GALERIA);
    }

    private void tomarFotoConCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CODIGO_CAMARA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        try {
            if (requestCode == CODIGO_GALERIA) {
                // Procesar imagen de galería
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                fotoPerfilBytes = obtenerBytes(inputStream);
                imgFotoPerfil.setImageBitmap(BitmapFactory.decodeByteArray(fotoPerfilBytes, 0, fotoPerfilBytes.length));
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

            } else if (requestCode == CODIGO_CAMARA) {
                // Procesar foto de cámara
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                fotoPerfilBytes = stream.toByteArray();
                imgFotoPerfil.setImageBitmap(bitmap);
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private byte[] obtenerBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private class CargarImagenTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                return BitmapFactory.decodeStream(connection.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imgFotoPerfil.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imgFotoPerfil.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                imgFotoPerfil.setImageBitmap(result);
            }
        }
    }

    private class ActualizarPerfilTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            try {
                SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
                String idUsuario = prefs.getString("usuario_id", "");

                URL url = new URL(getString(R.string.url_servidor) + "/miapp/actualizar_perfil.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("id", idUsuario);
                jsonParam.put("usuario", params[0]);
                jsonParam.put("correo", params[1]);
                jsonParam.put("telefono", params[2]);
                jsonParam.put("direccion", params[3]);

                if (fotoPerfilBytes != null) {
                    jsonParam.put("foto_usuario", Base64.encodeToString(fotoPerfilBytes, Base64.NO_WRAP));
                }

                OutputStream os = connection.getOutputStream();
                os.write(jsonParam.toString().getBytes());
                os.flush();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    return result.toString("UTF-8");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(EditarPerfil.this, "Error de conexión", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                JSONObject jsonResponse = new JSONObject(result);
                boolean success = jsonResponse.getBoolean("success");
                String message = jsonResponse.getString("message");

                if (success) {
                    // Actualizar SharedPreferences
                    actualizarPreferencias(jsonResponse);

                    // Crear Intent con los datos actualizados
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("usuario_actualizado", true);
                    resultIntent.putExtra("nuevo_nombre", txtUsuario.getText().toString().trim());
                    resultIntent.putExtra("nuevo_correo", txtCorreo.getText().toString().trim());
                    resultIntent.putExtra("nuevo_telefono", txtTelefono.getText().toString().trim());
                    resultIntent.putExtra("nuevo_direccion", txtDireccion.getText().toString().trim());

                    if (jsonResponse.has("foto_url")) {
                        resultIntent.putExtra("nueva_foto_url", jsonResponse.getString("foto_url"));
                    }

                    setResult(RESULT_OK, resultIntent);
                    Toast.makeText(EditarPerfil.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditarPerfil.this, "Error: " + message, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(EditarPerfil.this, "Error al procesar respuesta", Toast.LENGTH_LONG).show();
            }
        }

        private void actualizarPreferencias(JSONObject jsonResponse) throws JSONException {
            SharedPreferences.Editor editor = getSharedPreferences("miapp_prefs", MODE_PRIVATE).edit();
            editor.putString("usuario_nombre", txtUsuario.getText().toString().trim());
            editor.putString("usuario_correo", txtCorreo.getText().toString().trim());
            editor.putString("usuario_telefono", txtTelefono.getText().toString().trim());
            editor.putString("usuario_direccion", txtDireccion.getText().toString().trim());

            if (jsonResponse.has("foto_url")) {
                editor.putString("usuario_foto_url", jsonResponse.getString("foto_url"));
            }

            editor.apply();
        }
    }
}