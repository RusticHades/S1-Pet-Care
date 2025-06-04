package com.example.petcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class AgregarHistorialMedico extends AppCompatActivity {

    private TextInputEditText editTextFechaConsulta, editTextMotivoConsulta, editTextDiagnostico,
            editTextTratamiento, editTextObservaciones, editTextPesoActual, editTextTemperatura,
            editTextVacunas;

    private String idMascota, idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_historial_medico);

        // Obtener IDs del Intent
        idMascota = getIntent().getStringExtra("id_mascota");
        idUsuario = getIntent().getStringExtra("id_usuario");

        Toast.makeText(this, "User" + idUsuario + "Mascota" + idMascota, Toast.LENGTH_SHORT).show();

        initViews();
        setupListeners();
    }

    private void initViews() {
        editTextFechaConsulta = findViewById(R.id.editTextFechaConsulta);
        editTextMotivoConsulta = findViewById(R.id.editTextMotivoConsulta);
        editTextDiagnostico = findViewById(R.id.editTextDiagnostico);
        editTextTratamiento = findViewById(R.id.editTextTratamiento);
        editTextObservaciones = findViewById(R.id.editTextObservaciones);
        editTextPesoActual = findViewById(R.id.editTextPesoActual);
        editTextTemperatura = findViewById(R.id.editTextTemperatura);
        editTextVacunas = findViewById(R.id.editTextVacunas);
        Button buttonGuardarHistorial = findViewById(R.id.buttonGuardarHistorial);
    }

    private void setupListeners() {
        editTextFechaConsulta.setOnClickListener(v -> mostrarDatePicker());
        findViewById(R.id.buttonGuardarHistorial).setOnClickListener(v -> guardarHistorialMedico());
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String fechaSeleccionada = String.format("%d/%02d/%02d",year, month + 1, day);
                    editTextFechaConsulta.setText(fechaSeleccionada);
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void guardarHistorialMedico() {
        // Validar campos obligatorios
        if (editTextFechaConsulta.getText().toString().isEmpty() ||
                editTextMotivoConsulta.getText().toString().isEmpty() ||
                editTextDiagnostico.getText().toString().isEmpty()) {
            Toast.makeText(this, "Complete fecha, motivo y diagnóstico", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/guardar_historial_medico.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String boundary = "Boundary-" + System.currentTimeMillis();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

                // Añadir parámetros
                addFormField(writer, "id_mascota", idMascota, boundary);
                addFormField(writer, "id_veterinario", idUsuario, boundary);
                addFormField(writer, "fecha_consulta", editTextFechaConsulta.getText().toString(), boundary);
                addFormField(writer, "motivo_consulta", editTextMotivoConsulta.getText().toString(), boundary);
                addFormField(writer, "diagnostico", editTextDiagnostico.getText().toString(), boundary);

                // Campos opcionales
                if (!editTextTratamiento.getText().toString().isEmpty()) {
                    addFormField(writer, "tratamiento", editTextTratamiento.getText().toString(), boundary);
                }
                if (!editTextObservaciones.getText().toString().isEmpty()) {
                    addFormField(writer, "observaciones", editTextObservaciones.getText().toString(), boundary);
                }
                if (!editTextPesoActual.getText().toString().isEmpty()) {
                    addFormField(writer, "peso_actual", editTextPesoActual.getText().toString(), boundary);
                }
                if (!editTextTemperatura.getText().toString().isEmpty()) {
                    addFormField(writer, "temperatura", editTextTemperatura.getText().toString(), boundary);
                }
                if (!editTextVacunas.getText().toString().isEmpty()) {
                    addFormField(writer, "vacunas_aplicadas", editTextVacunas.getText().toString(), boundary);
                }

                writer.append("--").append(boundary).append("--").append("\r\n");
                writer.flush();
                writer.close();

                // Leer la respuesta del servidor para depuración
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Leer el cuerpo de la respuesta
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    runOnUiThread(() -> {
                        Toast.makeText(this, "Respuesta: " + response.toString(), Toast.LENGTH_LONG).show();
                        finish();
                    });
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error en el servidor: " + responseCode, Toast.LENGTH_SHORT).show());
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void addFormField(PrintWriter writer, String name, String value, String boundary) {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        writer.append(value).append("\r\n");
        writer.flush();
    }
}