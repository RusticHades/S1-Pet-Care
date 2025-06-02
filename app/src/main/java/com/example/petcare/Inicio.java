package com.example.petcare;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Inicio extends BaseActivity {
    private RecyclerView recyclerViewMascotas;
    private LinearLayout emptyStateView;
    private MascotasInicioAdapter mascotasAdapter;
    private List<Mascotas> mascotasList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el contenido específico
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_inicio, contentFrame, true);

        // Configurar vistas
        recyclerViewMascotas = contentFrame.findViewById(R.id.recyclerViewMascotas);
        emptyStateView = contentFrame.findViewById(R.id.emptyStateView);

        // Configurar RecyclerView
        recyclerViewMascotas.setLayoutManager(new LinearLayoutManager(this));
        mascotasAdapter = new MascotasInicioAdapter(mascotasList, this);
        recyclerViewMascotas.setAdapter(mascotasAdapter);

        // Cargar datos
        new ObtenerAnimalesTask().execute();

        // Configurar navegación
        navigationView.setCheckedItem(R.id.navMenuInicio);
        View menuItemView = navigationView.findViewById(R.id.navMenuInicio);
        if (menuItemView != null) {
            menuItemView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSecundario));
        }
        EdgeToEdge.enable(this);
    }

    private void updateUI() {
        if (mascotasList.isEmpty()) {
            recyclerViewMascotas.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewMascotas.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    private class ObtenerAnimalesTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
            String idUsuario = prefs.getString("usuario_id", "");

            if (idUsuario.isEmpty()) {
                return null;
            }

            String urlServer = getString(R.string.url_servidor) + "/miapp/obtener_mascotas.php?id_usuario=" + idUsuario;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String respuestaJson = null;

            try {
                URL url = new URL(urlServer);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000); // 10 segundos de timeout
                urlConnection.setReadTimeout(10000);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("NetworkError", "HTTP error code: " + responseCode);
                    return null;
                }

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                respuestaJson = buffer.toString();
            } catch (IOException e) {
                Log.e("NetworkError", "Error fetching data", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("NetworkError", "Error closing stream", e);
                    }
                }
            }

            return respuestaJson;
        }

        @Override
        protected void onPostExecute(String respuestaJson) {
            if (respuestaJson != null) {
                try {
                    JSONArray jsonArray = new JSONArray(respuestaJson);
                    mascotasList.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        String fotoMascota = jsonObject.optString("foto_mascota", "");
                        // Validar si la foto está vacía o es null
                        if (fotoMascota == null || fotoMascota.isEmpty() || fotoMascota.equals("null")) {
                            fotoMascota = ""; // Usará la imagen por defecto en el adapter
                        }

                        mascotasList.add(new Mascotas(
                                jsonObject.getString("id_mascota"),
                                jsonObject.getString("nombre"),
                                jsonObject.getString("especie"),
                                jsonObject.getString("raza"),
                                jsonObject.getString("edad"),
                                jsonObject.getString("sexo"),
                                fotoMascota
                        ));
                    }

                    mascotasAdapter.notifyDataSetChanged();
                    updateUI();
                } catch (JSONException e) {
                    Log.e("JSONError", "Error parsing JSON", e);
                    Log.d("JSONResponse", "Server response: " + respuestaJson);
                    updateUI();
                }
            } else {
                Log.e("NetworkError", "Response is null");
                updateUI();
            }
        }
    }
}