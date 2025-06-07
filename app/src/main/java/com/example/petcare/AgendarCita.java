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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AgendarCita extends AppCompatActivity {
    private String idMascota;
    private RecyclerView recyclerView;
    private CitasAdapter adapter;
    private List<Citas> citasList = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agendar_cita);

        idMascota = getIntent().getStringExtra("id_mascota");
        if (idMascota == null || idMascota.isEmpty()) {
            Toast.makeText(this, "Error: ID de mascota no válido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView = findViewById(R.id.recyclerViewCitas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CitasAdapter(citasList, this::eliminarCita);
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
        tvIdVeterinario.setText("1"); // Ejemplo, reemplazar con el valor real

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
        Log.d("API_CALL", "URL: " + url);

        // Obtén el ID del veterinario (debes tenerlo disponible)

        Map<String, String> params = new HashMap<>();
        params.put("id_mascota", idMascota);
        params.put("id_veterinario", "1");
        params.put("fecha_hora", fechaHora); // Envía el formato directamente como lo recoges
        params.put("motivo", motivo);
        params.put("notas", notas);

        Log.d("REQUEST_PARAMS", params.toString());

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d("API_RESPONSE", "Respuesta: " + response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            Toast.makeText(this, "Cita agendada correctamente", Toast.LENGTH_SHORT).show();
                            cargarCitas();
                        } else {
                            String error = jsonResponse.optString("message", "Error al agendar cita");
                            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("JSON_ERROR", "Error: " + e.getMessage());
                        Toast.makeText(this, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = "Error de conexión";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            errorMsg = new String(error.networkResponse.data, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else if (error.getMessage() != null) {
                        errorMsg = error.getMessage();
                    }
                    Log.e("NETWORK_ERROR", "Error: " + errorMsg);
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);
    }

    private String convertirFormatoFecha(String fechaOriginal) {
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date date = originalFormat.parse(fechaOriginal);
            return targetFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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