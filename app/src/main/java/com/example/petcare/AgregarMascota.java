package com.example.petcare;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

public class AgregarMascota extends BaseActivity {

    private static final int CODIGO_PERMISOS_CAMARA = 100;
    private static final int CODIGO_PERMISOS_ALMACENAMIENTO = 101;
    private static final int CODIGO_TOMAR_FOTO = 102;
    private static final int CODIGO_SELECCIONAR_IMAGEN = 103;

    private ImageView imageViewMascota;
    private EditText editTextNombre, editTextEspecie, editTextRaza, editTextFechaNacimiento, editTextEdad, editTextPeso;
    private Spinner spinnerSexo;
    private Switch switchEsterilizado;
    private Button btnGuardar, btnCancelar;
    private byte[] fotoMascotaBytes;
    private Uri fotoMascotaUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_mascota);

        // Inicializar vistas
        inicializarVistas();
        configurarListeners();
    }

    private void inicializarVistas() {
        imageViewMascota = findViewById(R.id.imageViewMascota1);
        editTextNombre = findViewById(R.id.editTextNombreMascota);
        editTextEspecie = findViewById(R.id.editTextEspecie);
        editTextRaza = findViewById(R.id.editTextRaza);
        editTextFechaNacimiento = findViewById(R.id.editTextFechaNacimiento);
        editTextEdad = findViewById(R.id.editTextEdadMascota);
        editTextPeso = findViewById(R.id.editTextPeso);
        spinnerSexo = findViewById(R.id.spinnerSexo);
        switchEsterilizado = findViewById(R.id.switchEsterilizado);
        btnGuardar = findViewById(R.id.buttonGuardar);
        btnCancelar = findViewById(R.id.buttonCancelar);

        // Configurar DatePicker para fecha de nacimiento
        editTextFechaNacimiento.setOnClickListener(v -> mostrarDatePicker());
    }

    private void configurarListeners() {
        findViewById(R.id.imageViewCamara1).setOnClickListener(v -> seleccionarFotoMascota());
        findViewById(R.id.btnCambiarFoto).setOnClickListener(v -> seleccionarFotoMascota());

        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> validarYRegistrarMascota());
    }

    private void seleccionarFotoMascota() {
        mostrarDialogoSeleccionImagen();
    }

    private void mostrarDialogoSeleccionImagen() {
        final CharSequence[] opciones = {"Tomar foto", "Elegir de galería", "Cancelar"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar foto de mascota");
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
                fotoMascotaUri = data.getData();
                imageViewMascota.setImageURI(fotoMascotaUri);
                imageViewMascota.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imageViewMascota.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                try {
                    // Convertir la imagen seleccionada a bytes
                    InputStream inputStream = getContentResolver().openInputStream(fotoMascotaUri);
                    fotoMascotaBytes = getBytes(inputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                }

            } else if (requestCode == CODIGO_TOMAR_FOTO && data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imageViewMascota.setImageBitmap(imageBitmap);
                imageViewMascota.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                imageViewMascota.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                fotoMascotaBytes = stream.toByteArray();
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

    private void mostrarDatePicker() {
        Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, añoSeleccionado, mesSeleccionado, diaSeleccionado) -> {
                    String fecha = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", añoSeleccionado, mesSeleccionado + 1, diaSeleccionado);
                    editTextFechaNacimiento.setText(fecha);

                    // Calcular edad automáticamente
                    calcularEdad(añoSeleccionado, mesSeleccionado, diaSeleccionado);
                },
                año, mes, dia
        );
        datePicker.show();
    }

    private void calcularEdad(int añoNacimiento, int mesNacimiento, int diaNacimiento) {
        Calendar hoy = Calendar.getInstance();
        Calendar nacimiento = Calendar.getInstance();
        nacimiento.set(añoNacimiento, mesNacimiento, diaNacimiento);

        int edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR);
        if (hoy.get(Calendar.DAY_OF_YEAR) < nacimiento.get(Calendar.DAY_OF_YEAR)) {
            edad--;
        }

        editTextEdad.setText(String.valueOf(edad));
    }

    private void validarYRegistrarMascota() {
        String nombre = editTextNombre.getText().toString().trim();
        String especie = editTextEspecie.getText().toString().trim();
        String sexo = spinnerSexo.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            editTextNombre.setError("Nombre requerido");
            return;
        }

        if (especie.isEmpty()) {
            editTextEspecie.setError("Especie requerida");
            return;
        }

        // Obtener datos del usuario
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
        String idUsuario = prefs.getString("usuario_id", "");

        // Crear objeto JSON con los datos
        JSONObject jsonMascota = new JSONObject();
        try {
            jsonMascota.put("id_usuario", idUsuario);
            jsonMascota.put("nombre", nombre);
            jsonMascota.put("especie", especie);
            jsonMascota.put("raza", editTextRaza.getText().toString().trim());
            jsonMascota.put("fecha_nacimiento", editTextFechaNacimiento.getText().toString().trim());
            jsonMascota.put("edad", editTextEdad.getText().toString().trim());
            jsonMascota.put("sexo", sexo);
            jsonMascota.put("peso", editTextPeso.getText().toString().trim());
            jsonMascota.put("esterilizado", switchEsterilizado.isChecked());

            if (fotoMascotaBytes != null) {
                jsonMascota.put("foto_mascota", Base64.encodeToString(fotoMascotaBytes, Base64.NO_WRAP));
            }

            new RegistrarMascotaTask().execute(jsonMascota.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al preparar datos", Toast.LENGTH_SHORT).show();
        }
    }

    private class RegistrarMascotaTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/guardar_mascota.php");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(params[0].getBytes());
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
            if (result != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(result);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    if (success) {
                        Toast.makeText(AgregarMascota.this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(AgregarMascota.this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(AgregarMascota.this, "Error al procesar respuesta", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(AgregarMascota.this, "Error de conexión", Toast.LENGTH_LONG).show();
            }
        }
    }
}