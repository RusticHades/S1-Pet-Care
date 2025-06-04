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

public class EditarMedicamentos extends AppCompatActivity {
    private String idMascota;
    private RecyclerView recyclerView;
    private MedicamentosAdapter adapter;
    private List<Medicamento> medicamentosList = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_medicamentos);

        idMascota = getIntent().getStringExtra("id_mascota");
        if (idMascota == null || idMascota.isEmpty()) {
            Toast.makeText(this, "Error: ID de mascota no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewMedicamentos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicamentosAdapter(medicamentosList, this::eliminarMedicamento);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnAgregarMedicamento).setOnClickListener(v -> mostrarDialogoAgregarMedicamento());
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> guardarCambios());

        requestQueue = Volley.newRequestQueue(this);
        cargarMedicamentos();
    }

    private void cargarMedicamentos() {
        String url = getString(R.string.url_servidor) + "/miapp/obtener_medicamentos.php?id_mascota=" + idMascota;
        Log.d("API_CALL", "URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("API_RESPONSE", "Respuesta: " + response.toString());

                        if (response.has("success") && response.getBoolean("success")) {
                            JSONArray medicamentosArray = response.optJSONArray("medicamentos");

                            medicamentosList.clear();
                            if (medicamentosArray != null && medicamentosArray.length() > 0) {
                                for (int i = 0; i < medicamentosArray.length(); i++) {
                                    JSONObject medicamentoJson = medicamentosArray.getJSONObject(i);
                                    Medicamento medicamento = new Medicamento(
                                            medicamentoJson.getInt("id_medicamento"),
                                            medicamentoJson.getString("nombre"),
                                            medicamentoJson.getString("dosis"),
                                            medicamentoJson.getString("frecuencia"),
                                            medicamentoJson.optString("motivo", ""),
                                            medicamentoJson.getString("fecha_inicio"),
                                            medicamentoJson.optString("fecha_fin", ""),
                                            medicamentoJson.optString("notas", "")
                                    );
                                    medicamentosList.add(medicamento);
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, "No se encontraron medicamentos registrados", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String error = response.optString("message", "Error al cargar medicamentos");
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

    private void mostrarDialogoAgregarMedicamento() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar nuevo medicamento");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_medicamento, null);
        final EditText inputNombre = viewInflated.findViewById(R.id.inputNombre);
        final EditText inputDosis = viewInflated.findViewById(R.id.inputDosis);
        final EditText inputFrecuencia = viewInflated.findViewById(R.id.inputFrecuencia);
        final EditText inputMotivo = viewInflated.findViewById(R.id.inputMotivo);
        final TextInputEditText inputFechaInicio = viewInflated.findViewById(R.id.inputFechaInicio);
        final TextInputEditText inputFechaFin = viewInflated.findViewById(R.id.inputFechaFin);
        final EditText inputNotas = viewInflated.findViewById(R.id.inputNotas);

        // Configurar el calendario y formateador de fecha
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());

        // Configurar DatePicker para fecha de inicio
        inputFechaInicio.setFocusable(false);
        inputFechaInicio.setClickable(true);
        inputFechaInicio.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                inputFechaInicio.setText(dateFormatter.format(calendar.getTime()));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Configurar DatePicker para fecha de fin
        inputFechaFin.setFocusable(false);
        inputFechaFin.setClickable(true);
        inputFechaFin.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                inputFechaFin.setText(dateFormatter.format(calendar.getTime()));
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(viewInflated);
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String nombre = inputNombre.getText().toString().trim();
            String dosis = inputDosis.getText().toString().trim();
            String frecuencia = inputFrecuencia.getText().toString().trim();
            String motivo = inputMotivo.getText().toString().trim();
            String fechaInicio = inputFechaInicio.getText().toString().trim();
            String fechaFin = inputFechaFin.getText().toString().trim();
            String notas = inputNotas.getText().toString().trim();

            if (!nombre.isEmpty() && !dosis.isEmpty() && !frecuencia.isEmpty() && !fechaInicio.isEmpty()) {
                agregarMedicamento(nombre, dosis, frecuencia, motivo, fechaInicio, fechaFin, notas);
            } else {
                Toast.makeText(this, "Debe ingresar al menos nombre, dosis, frecuencia y fecha de inicio", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void agregarMedicamento(String nombre, String dosis, String frecuencia, String motivo, String fechaInicio, String fechaFin, String notas) {
        String url = getString(R.string.url_servidor) + "/miapp/agregar_medicamento.php";
        Log.d("API_CALL", "URL: " + url);

        Map<String, String> params = new HashMap<>();
        params.put("id_mascota", idMascota);
        params.put("nombre", nombre);
        params.put("dosis", dosis);
        params.put("frecuencia", frecuencia);
        params.put("motivo", motivo);
        params.put("fecha_inicio", fechaInicio);
        params.put("fecha_fin", fechaFin);
        params.put("notas", notas);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d("API_RESPONSE", "Respuesta: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Medicamento agregado correctamente", Toast.LENGTH_SHORT).show();
                            cargarMedicamentos();
                        } else {
                            String error = jsonResponse.optString("message", "Error al agregar medicamento");
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

    private void eliminarMedicamento(int position) {
        Medicamento medicamento = medicamentosList.get(position);
        String url = getString(R.string.url_servidor) + "/miapp/eliminar_medicamento.php";

        Map<String, String> params = new HashMap<>();
        params.put("id_medicamento", String.valueOf(medicamento.getId()));

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            medicamentosList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(this, "Medicamento eliminado correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = jsonResponse.optString("message", "Error al eliminar medicamento");
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