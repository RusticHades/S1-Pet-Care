package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
    private ImageView imageViewFotoMascota;
    private RecyclerView recyclerViewHistorialMedico;
    private TextView textViewSinHistorial;

    // Adaptador
    private HistorialMedicoAdapter historialMedicoAdapter;

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

        // Configurar RecyclerView
        setupRecycler();

        // Cargar datos de la mascota
        if (idMascota != null && idUsuario != null) {
            cargarDatosMascota();
            cargarHistorialMedico();
        } else {
            Toast.makeText(this, "Error: Datos faltantes", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configurar listeners de botones
        setupButtonListeners();
    }

    private void initViews() {
        textViewNombreMascota = findViewById(R.id.textViewNombreMascota);
        textViewEspecie = findViewById(R.id.textViewEspecie);
        textViewRaza = findViewById(R.id.textViewRaza);
        textViewEdad = findViewById(R.id.textViewEdad);
        textViewSexo = findViewById(R.id.textViewSexo);
        textViewPeso = findViewById(R.id.textViewPeso);
        imageViewFotoMascota = findViewById(R.id.imageViewFotoMascota);

        recyclerViewHistorialMedico = findViewById(R.id.recyclerViewHistorialMedico);
        textViewSinHistorial = findViewById(R.id.textViewSinHistorial);
    }

    private void setupRecycler() {
        // Configurar RecyclerView para historial médico
        recyclerViewHistorialMedico.setLayoutManager(new LinearLayoutManager(this));
        historialMedicoAdapter = new HistorialMedicoAdapter(new ArrayList<>());
        recyclerViewHistorialMedico.setAdapter(historialMedicoAdapter);
    }

    private void setupButtonListeners() {
        // Botón para agregar historial médico
        findViewById(R.id.cardViewAgregarHistorial).setOnClickListener(v -> {
            // Implementar lógica para agregar historial médico
        });

        // Botones de edición avanzada
        findViewById(R.id.buttonEditarVacunas).setOnClickListener(v -> {
            // Implementar lógica para editar vacunas
        });

        findViewById(R.id.buttonEditarAlergias).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarAlergias.class);
            intent.putExtra("id_mascota", idMascota);
            startActivity(intent);
        });

        findViewById(R.id.buttonEditarMedicamentos).setOnClickListener(v -> {
            // Implementar lógica para editar medicamentos
        });

        // Botón para editar información completa
        findViewById(R.id.buttonEditarInformacionCompleta).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditarMascota.class);
            intent.putExtra("id_mascota", idMascota);
            startActivity(intent);
        });

        // Botón para cambiar foto
        findViewById(R.id.buttonCambiarFoto).setOnClickListener(v -> {
            // Implementar lógica para cambiar foto
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
                        textViewEdad.setText(response.optString("edad", "Desconocida") + " años");
                        textViewSexo.setText(response.getString("sexo"));
                        textViewPeso.setText(response.optString("peso", "Desconocido") + " kg");

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

    private void cargarHistorialMedico() {
        String url = getString(R.string.url_servidor) + "/miapp/historial_medico.php?id_mascota=" + idMascota;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        List<HistorialMedico> historiales = new ArrayList<>();

                        for (int i = 0; i < response.length(); i++) {
                            JSONObject item = response.getJSONObject(i);
                            HistorialMedico historial = new HistorialMedico(
                                    item.getString("fecha_consulta"),
                                    item.getString("diagnostico"),
                                    item.optString("tratamiento", ""),
                                    item.optString("observaciones", ""),
                                    item.optDouble("peso_actual", 0)
                            );
                            historiales.add(historial);
                        }

                        if (historiales.isEmpty()) {
                            textViewSinHistorial.setVisibility(View.VISIBLE);
                            recyclerViewHistorialMedico.setVisibility(View.GONE);
                        } else {
                            textViewSinHistorial.setVisibility(View.GONE);
                            recyclerViewHistorialMedico.setVisibility(View.VISIBLE);
                            historialMedicoAdapter.actualizarDatos(historiales);
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

    private String obtenerToken() {
        SharedPreferences sharedPreferences = getSharedPreferences("PetCarePrefs", MODE_PRIVATE);
        return sharedPreferences.getString("token", "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refrescar datos cuando la actividad se reanuda
        if (idMascota != null && idUsuario != null) {
            cargarDatosMascota();
            cargarHistorialMedico();
        }
    }
}

// Clase modelo para el historial médico
class HistorialMedico {
    private String fechaConsulta, diagnostico, tratamiento, observaciones;
    private double pesoActual;

    public HistorialMedico(String fechaConsulta, String diagnostico, String tratamiento,
                           String observaciones, double pesoActual) {
        this.fechaConsulta = fechaConsulta;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
        this.pesoActual = pesoActual;
    }

    // Getters
    public String getFechaConsulta() { return fechaConsulta; }
    public String getDiagnostico() { return diagnostico; }
    public String getTratamiento() { return tratamiento; }
    public String getObservaciones() { return observaciones; }
    public double getPesoActual() { return pesoActual; }
}