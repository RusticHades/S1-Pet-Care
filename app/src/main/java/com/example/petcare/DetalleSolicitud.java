package com.example.petcare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DetalleSolicitud extends AppCompatActivity {

    private TextView tvNombre, tvEspecialidad, tvUbicacion, tvTelefono, tvCorreo;
    private Button  btnVerCertificado;
    private String certificadoUrl;

    private String solicitudId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_solicitud);

        tvNombre = findViewById(R.id.tvNombre);
        tvEspecialidad = findViewById(R.id.tvEspecialidad);
        tvUbicacion = findViewById(R.id.tvUbicacion);
        tvTelefono = findViewById(R.id.tvTelefono);
        tvCorreo = findViewById(R.id.tvCorreo);
        btnVerCertificado = findViewById(R.id.btnVerCertificado);

        Button btnAprobar = findViewById(R.id.btnAprobar);
        Button btnRechazar = findViewById(R.id.btnRechazar);

        solicitudId = getIntent().getStringExtra("solicitud_id");

        btnAprobar.setOnClickListener(v -> aprobarSolicitud(solicitudId));
        btnRechazar.setOnClickListener(v -> rechazarSolicitud(solicitudId));

        // Obtener datos de la solicitud
        solicitudId = getIntent().getStringExtra("solicitud_id");
        String nombre = getIntent().getStringExtra("nombre");
        String especialidad = getIntent().getStringExtra("especialidad");
        String ubicacion = getIntent().getStringExtra("ubicacion");
        String telefono = getIntent().getStringExtra("telefono");
        String correo = getIntent().getStringExtra("correo");
        certificadoUrl = getString(R.string.url_servidor) + "/miapp/"+ getIntent().getStringExtra("certificado");


        // Mostrar datos
        tvNombre.setText("Nombre: " + nombre);
        tvEspecialidad.setText("Especialidad: " + especialidad);
        tvUbicacion.setText("Ubicación: " + ubicacion);
        tvTelefono.setText("Teléfono: " + telefono);
        tvCorreo.setText("Correo: " + correo);

        btnVerCertificado.setOnClickListener(v -> verCertificado());
    }

    private void verCertificado() {
        if (certificadoUrl != null && !certificadoUrl.isEmpty()) {
            // Abrir el certificado en un navegador o visor de PDF
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(certificadoUrl));
            startActivity(browserIntent);
        } else {
            Toast.makeText(this, "No hay certificado disponible", Toast.LENGTH_SHORT).show();
        }
    }


    // Agregar estos métodos a tu Activity DetalleSolicitud
    private void aprobarSolicitud(String solicitudId) {
        new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/procesar_solicitud.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("solicitud_id", solicitudId);
                jsonParam.put("accion", "aprobar");

                OutputStream os = connection.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    runOnUiThread(() -> {
                        try {
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                finish(); // Cerrar la actividad después de aprobar
                            } else {
                                Toast.makeText(this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al procesar solicitud", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void rechazarSolicitud(String solicitudId) {
        new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/procesar_solicitud.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("solicitud_id", solicitudId);
                jsonParam.put("accion", "rechazar");

                OutputStream os = connection.getOutputStream();
                os.write(jsonParam.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    runOnUiThread(() -> {
                        try {
                            if (jsonResponse.getBoolean("success")) {
                                Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                                finish(); // Cerrar la actividad después de rechazar
                            } else {
                                Toast.makeText(this, "Error: " + jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al procesar solicitud", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}