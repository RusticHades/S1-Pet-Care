package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VistaVeterinarioEnMascotas extends AppCompatActivity {

    private String idMascota, idUsuario;
    private RequestQueue requestQueue;

    // Views
    private TextView textViewNombreMascota, textViewEspecie, textViewRaza, textViewEdad, textViewSexo, textViewPeso;
    private RecyclerView recyclerViewHistorialMedico;
    private TextView textViewSinHistorial;

    private HistorialMedicoAdapter historialAdapter;
    private List<HistorialMedicoMascota> listaHistorial = new ArrayList<>();

    // Adaptador

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_veterinario_en_mascotas);

        // Inicializar Volley
        requestQueue = Volley.newRequestQueue(this);

        // Obtener IDs del Intent
        idMascota = getIntent().getStringExtra("id_mascota");
        idUsuario = getIntent().getStringExtra("id_usuario");

        // Inicializar vistas
        initViews();

        configurarRecyclerView();

        // Cargar datos de la mascota y su historial
        if (idMascota != null && idUsuario != null) {
            cargarDatosMascota();
            cargarHistorialMedico();
            setupButtonListeners();
        }
    }
    private void configurarRecyclerView() {
        recyclerViewHistorialMedico.setLayoutManager(new LinearLayoutManager(this));
        historialAdapter = new HistorialMedicoAdapter(listaHistorial, historial -> {
            // Mostrar detalles completos del historial
            Intent intent = new Intent(this, DetalleHistorial.class);

            // Pasar cada dato individualmente
            intent.putExtra("id_historial", historial.getIdHistorial());
            intent.putExtra("fecha_consulta", historial.getFechaConsulta());
            intent.putExtra("motivo_consulta", historial.getMotivoConsulta());
            intent.putExtra("diagnostico", historial.getDiagnostico());
            intent.putExtra("tratamiento", historial.getTratamiento());
            intent.putExtra("observaciones", historial.getObservaciones());

            // Pasar valores numéricos solo si no son null
            if (historial.getPesoActual() != null) {
                intent.putExtra("peso_actual", historial.getPesoActual());
            }
            if (historial.getTemperatura() != null) {
                intent.putExtra("temperatura", historial.getTemperatura());
            }

            intent.putExtra("vacunas_aplicadas", historial.getVacunasAplicadas());

            startActivity(intent);
        });
        recyclerViewHistorialMedico.setAdapter(historialAdapter);
    }

    private void cargarHistorialMedico() {
        String url = getString(R.string.url_servidor) + "/miapp/obtener_historial_mascota.php?id_mascota=" + idMascota;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listaHistorial.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject jsonObject = response.getJSONObject(i);
                            HistorialMedicoMascota historial = new HistorialMedicoMascota(
                                    jsonObject.getInt("id_historial"),
                                    jsonObject.getString("fecha_consulta"),
                                    jsonObject.getString("motivo_consulta"),
                                    jsonObject.getString("diagnostico"),
                                    jsonObject.optString("tratamiento", ""),
                                    jsonObject.optString("observaciones", ""),
                                    jsonObject.has("peso_actual") && !jsonObject.isNull("peso_actual")
                                            ? jsonObject.getDouble("peso_actual") : null,
                                    jsonObject.has("temperatura") && !jsonObject.isNull("temperatura")
                                            ? jsonObject.getDouble("temperatura") : null,
                                    jsonObject.optString("vacunas_aplicadas", "")
                            );
                            listaHistorial.add(historial);
                        }

                        historialAdapter.notifyDataSetChanged();

                        // Mostrar u ocultar mensaje de "sin historial"
                        if (listaHistorial.isEmpty()) {
                            textViewSinHistorial.setVisibility(View.VISIBLE);
                            recyclerViewHistorialMedico.setVisibility(View.GONE);
                        } else {
                            textViewSinHistorial.setVisibility(View.GONE);
                            recyclerViewHistorialMedico.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar el historial", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al cargar historial médico", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + obtenerToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void initViews() {
        textViewNombreMascota = findViewById(R.id.textViewNombreMascota);
        textViewEspecie = findViewById(R.id.textViewEspecie);
        textViewRaza = findViewById(R.id.textViewRaza);
        textViewEdad = findViewById(R.id.textViewEdad);
        textViewSexo = findViewById(R.id.textViewSexo);
        textViewPeso = findViewById(R.id.textViewPeso);

        recyclerViewHistorialMedico = findViewById(R.id.recyclerViewHistorialMedico);
        textViewSinHistorial = findViewById(R.id.textViewSinHistorial);
    }



    private void setupButtonListeners() {
        findViewById(R.id.cardViewAgregarHistorial).setOnClickListener(v -> {
            Intent intent = new Intent(this, AgregarHistorialMedico.class);
            intent.putExtra("id_mascota", idMascota);
            intent.putExtra("id_usuario", idUsuario);
            startActivity(intent);
        });

        findViewById(R.id.buttonEditarVacunas).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarVacunas.class);
            intent.putExtra("id_mascota", idMascota);
            startActivity(intent);
        });

        findViewById(R.id.buttonEditarAlergias).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarAlergias.class);
            intent.putExtra("id_mascota", idMascota);
            startActivity(intent);
        });

        findViewById(R.id.buttonEditarMedicamentos).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarMedicamentos.class);
            intent.putExtra("id_mascota", idMascota);
            startActivity(intent);
        });

        findViewById(R.id.buttonEditarInformacionCompleta).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarMascota.class);
            intent.putExtra("id_mascota", idMascota);
            startActivity(intent);
        });
    }

    private void cargarDatosMascota() {
        String url = getString(R.string.url_servidor) + "/miapp/obtener_mascota_veterinario.php?id_mascota=" + idMascota;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Actualizar UI con datos de la mascota
                        textViewNombreMascota.setText(response.getString("nombre"));
                        textViewEspecie.setText(response.getString("especie"));
                        textViewRaza.setText(response.optString("raza", "Desconocida"));
                        textViewEdad.setText(response.optString("edad", "Desconocida"));
                        textViewSexo.setText(response.getString("sexo"));
                        textViewPeso.setText(response.optString("peso", "Desconocido"));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Error al cargar datos de la mascota", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + obtenerToken());
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private String obtenerToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("PetCarePrefs", MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }
}