package com.example.petcare;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;

public class AgregarMascota extends BaseActivity {

    private ImageView imageViewMascota;
    private EditText editTextNombre, editTextEspecie, editTextRaza, editTextFechaNacimiento, editTextEdad, editTextPeso;
    private Spinner spinnerSexo;
    private Switch switchEsterilizado;
    private Button btnGuardar, btnCancelar;
    private byte[] fotoMascotaBytes;

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