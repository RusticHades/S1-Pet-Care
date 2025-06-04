package com.example.petcare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditarVacunas extends AppCompatActivity {
    private String idMascota;
    private RecyclerView recyclerView;
    private VacunasAdapter adapter;
    private List<Vacuna> vacunasList = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_vacunas);

        idMascota = getIntent().getStringExtra("id_mascota");
        if (idMascota == null || idMascota.isEmpty()) {
            Toast.makeText(this, "Error: ID de mascota no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewVacunas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VacunasAdapter(vacunasList, this::eliminarVacuna);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnAgregarVacuna).setOnClickListener(v -> mostrarDialogoAgregarVacuna());
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> guardarCambios());

        requestQueue = Volley.newRequestQueue(this);
        cargarVacunas();
    }

    private void cargarVacunas() {
        String url = getString(R.string.url_servidor) + "/miapp/obtener_vacunas.php?id_mascota=" + idMascota;
        Log.d("API_CALL", "URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("API_RESPONSE", "Respuesta: " + response.toString());

                        if (response.has("success") && response.getBoolean("success")) {
                            JSONArray vacunasArray = response.optJSONArray("vacunas");

                            vacunasList.clear();
                            if (vacunasArray != null && vacunasArray.length() > 0) {
                                for (int i = 0; i < vacunasArray.length(); i++) {
                                    JSONObject vacunaJson = vacunasArray.getJSONObject(i);
                                    Vacuna vacuna = new Vacuna(
                                            vacunaJson.getInt("id_vacuna"),
                                            vacunaJson.getString("nombre"),
                                            vacunaJson.getString("fecha_aplicacion"),
                                            vacunaJson.optString("fecha_proximo_refuerzo", ""),
                                            vacunaJson.optString("veterinario", ""),
                                            vacunaJson.optString("notas", "")
                                    );
                                    vacunasList.add(vacuna);
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, "No se encontraron vacunas registradas", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String error = response.optString("message", "Error al cargar vacunas");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error al parsear JSON: " + e.getMessage());
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Error desconocido";
                    Log.e("NETWORK_ERROR", "Error: " + errorMsg);
                    Toast.makeText(this, "Error de conexión: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    private void mostrarDialogoAgregarVacuna() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar nueva vacuna");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_vacuna, null);
        final EditText inputNombre = viewInflated.findViewById(R.id.inputNombre);
        final TextInputEditText inputFechaAplicacion = viewInflated.findViewById(R.id.inputFechaAplicacion);
        final TextInputEditText inputFechaRefuerzo = viewInflated.findViewById(R.id.inputFechaRefuerzo);
        final EditText inputVeterinario = viewInflated.findViewById(R.id.inputVeterinario);
        final EditText inputNotas = viewInflated.findViewById(R.id.inputNotas);

        // Configurar el calendario y formateador de fecha
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        // Configurar DatePicker para fecha de aplicación
        inputFechaAplicacion.setFocusable(false);
        inputFechaAplicacion.setClickable(true);
        inputFechaAplicacion.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                inputFechaAplicacion.setText(dateFormatter.format(calendar.getTime()));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Configurar DatePicker para fecha de refuerzo
        inputFechaRefuerzo.setFocusable(false);
        inputFechaRefuerzo.setClickable(true);
        inputFechaRefuerzo.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                inputFechaRefuerzo.setText(dateFormatter.format(calendar.getTime()));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(viewInflated);
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString().trim();
            String fechaAplicacion = inputFechaAplicacion.getText().toString().trim();
            String fechaRefuerzo = inputFechaRefuerzo.getText().toString().trim();
            String veterinario = inputVeterinario.getText().toString().trim();
            String notas = inputNotas.getText().toString().trim();

            if (!nombre.isEmpty() && !fechaAplicacion.isEmpty()) {
                agregarVacuna(nombre, fechaAplicacion, fechaRefuerzo, veterinario, notas);
            } else {
                Toast.makeText(this, "Debe ingresar al menos el nombre y fecha de aplicación", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void agregarVacuna(String nombre, String fechaAplicacion, String fechaRefuerzo, String veterinario, String notas) {
        String url = getString(R.string.url_servidor) + "/miapp/agregar_vacuna.php";
        Log.d("API_CALL", "URL: " + url);

        Map<String, String> params = new HashMap<>();
        params.put("id_mascota", idMascota);
        params.put("nombre", nombre);
        params.put("fecha_aplicacion", fechaAplicacion);
        params.put("fecha_proximo_refuerzo", fechaRefuerzo);
        params.put("veterinario", veterinario);
        params.put("notas", notas);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d("API_RESPONSE", "Respuesta: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Vacuna agregada correctamente", Toast.LENGTH_SHORT).show();
                            cargarVacunas();
                        } else {
                            String error = jsonResponse.optString("message", "Error al agregar vacuna");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Error desconocido";
                    if (error.networkResponse != null) {
                        errorMsg += " - Código: " + error.networkResponse.statusCode;
                    }
                    Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        requestQueue.add(request);
    }

    private void eliminarVacuna(int position) {
        Vacuna vacuna = vacunasList.get(position);
        String url = getString(R.string.url_servidor) + "/miapp/eliminar_vacuna.php";

        Map<String, String> params = new HashMap<>();
        params.put("id_vacuna", String.valueOf(vacuna.getId()));

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            vacunasList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(this, "Vacuna eliminada correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = jsonResponse.optString("message", "Error al eliminar vacuna");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = error.getMessage() != null ? error.getMessage() : "Error desconocido";
                    Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }
        };

        requestQueue.add(request);
    }

    private void guardarCambios() {
        finish();
    }
}