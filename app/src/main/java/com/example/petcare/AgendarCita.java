package com.example.petcare;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgendarCita extends AppCompatActivity {
    private String idMascota, idVeterinario, inicioVeterinario;
    private RecyclerView recyclerView;
    private CitasAdapter adapter;
    private List<Citas> citasList = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_cita);

        idMascota = getIntent().getStringExtra("id_mascota");
        idVeterinario = getIntent().getStringExtra("id_veterinario");
        inicioVeterinario = getIntent().getStringExtra("inicio_o_veterinario");
        if (inicioVeterinario == null || inicioVeterinario.isEmpty()) {
            inicioVeterinario = "veterinario"; // Valor por defecto
        }

        if (idMascota == null || idMascota.isEmpty()) {
            Toast.makeText(this, "Error: ID de mascota no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (inicioVeterinario.equals("inicio")) {
            MaterialButton btnAgregarCitaInicio = findViewById(R.id.btnAgregarCita);
            btnAgregarCitaInicio.setVisibility(View.GONE);
            btnAgregarCitaInicio.setEnabled(false);

            MaterialButton btnGuardarCambiosInicio = findViewById(R.id.btnGuardarCambios);
            btnGuardarCambiosInicio.setVisibility(View.GONE);
            btnGuardarCambiosInicio.setEnabled(false);
        }

        recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitasAdapter(citasList, new CitasAdapter.OnCitaClickListener() {
            @Override
            public void onCitaClick(int position) {
            }

            @Override
            public void onDeleteClick(int position) {
                eliminarCita(position);
            }
        }, inicioVeterinario);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnAgregarCita).setOnClickListener(v -> mostrarDialogoAgregarCita());
        findViewById(R.id.btnGuardarCambios).setOnClickListener(v -> guardarCambios());

        requestQueue = Volley.newRequestQueue(this);
        cargarCitas();
    }

    private void cargarCitas() {
        String url = getString(R.string.url_servidor) + "/miapp/obtener_citas.php?id_mascota=" + idMascota;
        Log.d("API_CALL", "URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        Log.d("API_RESPONSE", "Respuesta: " + response.toString());

                        if (response.has("success") && response.getBoolean("success")) {
                            JSONArray citasArray = response.optJSONArray("citas");

                            citasList.clear();
                            if (citasArray != null && citasArray.length() > 0) {
                                for (int i = 0; i < citasArray.length(); i++) {
                                    JSONObject citaJson = citasArray.getJSONObject(i);
                                    Citas citas = new Citas(
                                            citaJson.getInt("id_cita"),
                                            citaJson.getString("fecha_hora"),
                                            citaJson.getString("motivo"),
                                            citaJson.optString("notas", ""),
                                            citaJson.getString("estado")
                                    );
                                    citasList.add(citas);
                                }
                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(this, "No se encontraron citas registradas", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String error = response.optString("message", "Error al cargar citas");
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

    private void mostrarDialogoAgregarCita() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agendar nueva cita");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialogo_agregar_citas, null);
        final TextInputEditText inputFechaHora = viewInflated.findViewById(R.id.et_fecha_cita);
        final EditText inputMotivo = viewInflated.findViewById(R.id.et_motivo);
        final EditText inputNotas = viewInflated.findViewById(R.id.et_notas);
        final TextView tvIdMascota = viewInflated.findViewById(R.id.tv_id_mascota);
        final TextView tvIdVeterinario = viewInflated.findViewById(R.id.tv_id_veterinario);

        tvIdMascota.setText(idMascota);
        // Aquí deberías obtener el id_veterinario de alguna manera (quizás del Intent o SharedPreferences)
        tvIdVeterinario.setText(idVeterinario); // Ejemplo, reemplazar con el valor real

        // Configurar el calendario y formateador de fecha y hora
        final Calendar calendar = Calendar.getInstance();
        final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

        // Configurar DatePicker y TimePicker para fecha y hora
        inputFechaHora.setFocusable(false);
        inputFechaHora.setClickable(true);
        inputFechaHora.setOnClickListener(v -> {
            // Primero seleccionar fecha
            new DatePickerDialog(this, (datePicker, year, month, day) -> {
                calendar.set(year, month, day);
                // Luego seleccionar hora
                new TimePickerDialog(this, (timePicker, hour, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    inputFechaHora.setText(dateTimeFormatter.format(calendar.getTime()));
                },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true).show();
            },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        builder.setView(viewInflated);
        builder.setPositiveButton("Agendar", (dialog, which) -> {
            String fechaHora = inputFechaHora.getText().toString().trim();
            String motivo = inputMotivo.getText().toString().trim();
            String notas = inputNotas.getText().toString().trim();

            if (!fechaHora.isEmpty() && !motivo.isEmpty()) {
                agregarCita(fechaHora, motivo, notas);
            } else {
                Toast.makeText(this, "Debe ingresar fecha/hora y motivo de la cita", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void agregarCita(String fechaHora, String motivo, String notas) {
        String url = getString(R.string.url_servidor) + "/miapp/agregar_cita.php";

        // Convertir parámetros a JSON
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("id_mascota", idMascota);
            jsonBody.put("id_veterinario", idVeterinario); // Reemplaza con valor real
            jsonBody.put("fecha_cita", fechaHora);
            jsonBody.put("motivo", motivo);
            jsonBody.put("notas", notas);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            Toast.makeText(this, "Cita agendada", Toast.LENGTH_SHORT).show();
                            cargarCitas();
                        } else {
                            String error = response.getString("message");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Mostrar el error real del servidor
                    String errorMsg = "Error de conexión";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            errorMsg = new String(error.networkResponse.data, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void eliminarCita(int position) {
        Citas citas = citasList.get(position);
        String url = getString(R.string.url_servidor) + "/miapp/eliminar_cita.php";

        Map<String, String> params = new HashMap<>();
        params.put("id_cita", String.valueOf(citas.getId()));

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            citasList.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(this, "Cita eliminada correctamente", Toast.LENGTH_SHORT).show();
                        } else {
                            String error = jsonResponse.optString("message", "Error al eliminar cita");
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