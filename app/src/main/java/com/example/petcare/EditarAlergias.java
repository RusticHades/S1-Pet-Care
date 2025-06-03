package com.example.petcare;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarAlergias extends AppCompatActivity {
    private String idMascota;

    private RecyclerView recyclerView;
    private AlergiasAdapter adapter;
    private List<Alergia> alergiasList = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_alergias);

        // Obtener el ID de la mascota del Intent
        idMascota = getIntent().getStringExtra("id_mascota");

        if (idMascota == null || idMascota.isEmpty()) {
            Toast.makeText(this, "Error: ID de mascota no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAlergias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlergiasAdapter(alergiasList, this::eliminarAlergia);
        recyclerView.setAdapter(adapter);

        // Configurar botones
        findViewById(R.id.btnAgregarAlergia).setOnClickListener(v -> mostrarDialogoAgregarAlergia());
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> guardarCambios());

        // Inicializar RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Cargar alergias existentes
        cargarAlergias();
    }

    private void cargarAlergias() {
        String url = getString(R.string.url_servidor) + "/miapp/obtener_alergias.php?id_mascota=" + idMascota;
        Log.d("API_CALL", "URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("API_RESPONSE", "Respuesta: " + response.toString());

                        if (response.has("success") && response.getBoolean("success")) {
                            JSONArray alergiasArray = response.optJSONArray("alergias");

                            alergiasList.clear();
                            if (alergiasArray != null && alergiasArray.length() > 0) {
                                for (int i = 0; i < alergiasArray.length(); i++) {
                                    JSONObject alergiaJson = alergiasArray.getJSONObject(i);
                                    Alergia alergia = new Alergia(
                                            alergiaJson.getInt("id_alergia"),
                                            alergiaJson.getString("alergia"),
                                            alergiaJson.getString("severidad"),
                                            alergiaJson.optString("notas", "")
                                    );
                                    alergiasList.add(alergia);
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, "No se encontraron alergias registradas", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String error = response.optString("message", "Error al cargar alergias");
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

    private void mostrarDialogoAgregarAlergia() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar nueva alergia");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_alergia, null);
        final EditText inputAlergia = viewInflated.findViewById(R.id.inputAlergia);
        final Spinner spinnerSeveridad = viewInflated.findViewById(R.id.spinnerSeveridad);
        final EditText inputNotas = viewInflated.findViewById(R.id.inputNotas);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.severidad_alergias,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeveridad.setAdapter(adapter);

        builder.setView(viewInflated);
        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String alergia = inputAlergia.getText().toString().trim();
            String severidad = spinnerSeveridad.getSelectedItem().toString();
            String notas = inputNotas.getText().toString().trim();

            if (!alergia.isEmpty()) {
                agregarAlergia(alergia, severidad, notas);
            } else {
                Toast.makeText(this, "Debe ingresar el nombre de la alergia", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void agregarAlergia(String alergia, String severidad, String notas) {
        String url = getString(R.string.url_servidor) + "/miapp/agregar_alergia.php";
        Log.d("API_CALL", "URL: " + url);

        Map<String, String> params = new HashMap<>();
        params.put("id_mascota", idMascota);
        params.put("alergia", alergia);
        params.put("severidad", severidad);
        params.put("notas", notas);

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d("API_RESPONSE", "Respuesta: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Alergia agregada correctamente", Toast.LENGTH_SHORT).show();
                            cargarAlergias(); // Recargar la lista
                        } else {
                            String error = jsonResponse.optString("message", "Error al agregar alergia");
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

    private void eliminarAlergia(int position) {
        Alergia alergia = alergiasList.get(position);
        String url = getString(R.string.url_servidor) + "/miapp/eliminar_alergia.php";

        Map<String, String> params = new HashMap<>();
        params.put("id_alergia", String.valueOf(alergia.getId()));

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            alergiasList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(this, "Alergia eliminada correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = jsonResponse.optString("message", "Error al eliminar alergia");
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