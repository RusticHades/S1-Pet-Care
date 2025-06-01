package com.example.petcare;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petcare.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class EditarMascota extends AppCompatActivity {
    private String idMascota;
    private TextInputEditText editTextNombre, editTextEspecie, editTextRaza, editTextFechaNacimiento,
            editTextEdad, editTextPeso;
    private Spinner spinnerSexo;
    private Switch switchEsterilizado;
    private Button buttonGuardar, buttonCancelar, buttonEliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_mascota);

        // Obtener ID de la mascota del intent
        idMascota = getIntent().getStringExtra("id_mascota");

        // Inicializar vistas
        initViews();

        // Cargar datos de la mascota
        new CargarMascotaTask().execute(idMascota);

        // Configurar listeners
        setupListeners();
    }

    private void initViews() {
        editTextNombre = findViewById(R.id.editTextNombreMascotaEditar);
        editTextEspecie = findViewById(R.id.editTextEspecieEditar);
        editTextRaza = findViewById(R.id.editTextRazaEditar);
        editTextFechaNacimiento = findViewById(R.id.editTextFechaNacimientoEditar);
        editTextEdad = findViewById(R.id.editTextEdadMascotaEditar);
        editTextPeso = findViewById(R.id.editTextPesoEditar);
        spinnerSexo = findViewById(R.id.spinnerSexoEditar);
        switchEsterilizado = findViewById(R.id.switchEsterilizadoEditar);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        buttonCancelar = findViewById(R.id.buttonCancelar);
        buttonEliminar = findViewById(R.id.buttonEliminar);
    }

    private void setupListeners() {
        buttonCancelar.setOnClickListener(v -> finish());

        buttonGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                new GuardarMascotaTask().execute();
            }
        });

        buttonEliminar.setOnClickListener(v -> mostrarDialogoConfirmacion());
    }

    private boolean validarCampos() {
        if (editTextNombre.getText().toString().trim().isEmpty()) {
            editTextNombre.setError("Nombre es requerido");
            return false;
        }
        if (editTextEspecie.getText().toString().trim().isEmpty()) {
            editTextEspecie.setError("Especie es requerida");
            return false;
        }
        return true;
    }

    private class CargarMascotaTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String id = params[0];
            String urlServer = getString(R.string.url_servidor) + "/miapp/obtener_mascota_por_id.php?id_mascota=" + id;

            try {
                URL url = new URL(urlServer);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject mascota = new JSONObject(result);

                    // Llenar campos con los datos de la mascota
                    editTextNombre.setText(mascota.getString("nombre"));
                    editTextEspecie.setText(mascota.getString("especie"));
                    editTextRaza.setText(mascota.getString("raza"));
                    editTextFechaNacimiento.setText(mascota.getString("fecha_nacimiento"));
                    editTextEdad.setText(mascota.getString("edad"));
                    editTextPeso.setText(mascota.getString("peso"));

                    // Configurar spinner de sexo
                    String sexo = mascota.getString("sexo");
                    if (!sexo.isEmpty()) {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                                EditarMascota.this,
                                R.array.sexo_options,
                                android.R.layout.simple_spinner_item
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerSexo.setAdapter(adapter);

                        int position = adapter.getPosition(sexo);
                        if (position >= 0) {
                            spinnerSexo.setSelection(position);
                        }
                    }

                    // Configurar switch de esterilizado
                    String esterilizado = mascota.getString("esterilizado");
                    switchEsterilizado.setChecked(esterilizado.equalsIgnoreCase("1") ||
                            esterilizado.equalsIgnoreCase("si") ||
                            esterilizado.equalsIgnoreCase("true"));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(EditarMascota.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(EditarMascota.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GuardarMascotaTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/actualizar_mascota.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Preparar datos para enviar
                String nombre = editTextNombre.getText().toString().trim();
                String especie = editTextEspecie.getText().toString().trim();
                String raza = editTextRaza.getText().toString().trim();
                String fechaNacimiento = editTextFechaNacimiento.getText().toString().trim();
                String edad = editTextEdad.getText().toString().trim();
                String sexo = spinnerSexo.getSelectedItem().toString();
                String peso = editTextPeso.getText().toString().trim();
                String esterilizado = switchEsterilizado.isChecked() ? "1" : "0";

                // Crear parámetros
                String parametros = "id_mascota=" + URLEncoder.encode(idMascota, "UTF-8") +
                        "&nombre=" + URLEncoder.encode(nombre, "UTF-8") +
                        "&especie=" + URLEncoder.encode(especie, "UTF-8") +
                        "&raza=" + URLEncoder.encode(raza, "UTF-8") +
                        "&fecha_nacimiento=" + URLEncoder.encode(fechaNacimiento, "UTF-8") +
                        "&edad=" + URLEncoder.encode(edad, "UTF-8") +
                        "&sexo=" + URLEncoder.encode(sexo, "UTF-8") +
                        "&peso=" + URLEncoder.encode(peso, "UTF-8") +
                        "&esterilizado=" + URLEncoder.encode(esterilizado, "UTF-8");

                // Enviar datos
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(parametros);
                writer.flush();
                writer.close();
                os.close();

                // Obtener respuesta
                int responseCode = conn.getResponseCode();
                return responseCode == 200;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditarMascota.this, "Mascota actualizada", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(EditarMascota.this, "Error al actualizar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mostrarDialogoConfirmacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar mascota")
                .setMessage("¿Estás seguro de que quieres eliminar esta mascota?")
                .setPositiveButton("Eliminar", (dialog, which) -> new EliminarMascotaTask().execute(idMascota))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private class EliminarMascotaTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String idMascota = params[0];

            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/eliminar_mascota.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Enviar parámetros
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String data = URLEncoder.encode("id_mascota", "UTF-8") + "=" +
                        URLEncoder.encode(idMascota, "UTF-8");
                writer.write(data);
                writer.flush();
                writer.close();
                os.close();

                // Obtener respuesta
                int responseCode = conn.getResponseCode();
                return responseCode == 200;

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(EditarMascota.this, "Mascota eliminada", Toast.LENGTH_SHORT).show();
                finish(); // Cierra la actividad
            } else {
                Toast.makeText(EditarMascota.this, "Error al eliminar mascota", Toast.LENGTH_SHORT).show();
            }
        }
    }
}