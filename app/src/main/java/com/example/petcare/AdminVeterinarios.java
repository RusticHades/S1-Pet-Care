package com.example.petcare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminVeterinarios extends AppCompatActivity {

    private ListView lvSolicitudes;
    private List<Map<String, String>> solicitudesList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_veterinarios);

        lvSolicitudes = findViewById(R.id.lvSolicitudes);
        solicitudesList = new ArrayList<>();

        cargarSolicitudes();

        lvSolicitudes.setOnItemClickListener((parent, view, position, id) -> {
            Map<String, String> solicitud = solicitudesList.get(position);
            Intent intent = new Intent(this, DetalleSolicitud.class);
            intent.putExtra("solicitud_id", solicitud.get("id"));
            intent.putExtra("nombre", solicitud.get("nombre"));
            intent.putExtra("especialidad", solicitud.get("especialidad"));
            intent.putExtra("certificado", solicitud.get("certificado"));
            intent.putExtra("ubicacion", solicitud.get("ubicacion"));
            intent.putExtra("telefono", solicitud.get("telefono"));
            intent.putExtra("correo", solicitud.get("correo"));
            startActivity(intent);
        });
    }

    private void cargarSolicitudes() {
        new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/obtener_solicitudes_veterinarios.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

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
                    if (jsonResponse.getBoolean("success")) {
                        JSONArray data = jsonResponse.getJSONArray("data");

                        solicitudesList.clear();
                        List<String> nombres = new ArrayList<>();

                        for (int i = 0; i < data.length(); i++) {
                            JSONObject solicitud = data.getJSONObject(i);
                            Map<String, String> map = new HashMap<>();
                            map.put("id", solicitud.getString("id"));
                            map.put("nombre", solicitud.getString("usuario"));
                            map.put("especialidad", solicitud.getString("especialidad"));
                            map.put("certificado", solicitud.getString("certificado"));
                            map.put("ubicacion", solicitud.getString("ubicacion"));
                            map.put("telefono", solicitud.getString("telefono"));
                            map.put("correo", solicitud.getString("correo"));

                            solicitudesList.add(map);
                            nombres.add(solicitud.getString("usuario") + " - " + solicitud.getString("especialidad"));
                        }

                        runOnUiThread(() -> {
                            adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, nombres);
                            lvSolicitudes.setAdapter(adapter);
                        });
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}