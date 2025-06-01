    package com.example.petcare;
    
    import android.content.Intent;
    import android.graphics.Insets;
    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.widget.FrameLayout;
    
    import androidx.activity.EdgeToEdge;
    import androidx.core.view.ViewCompat;
    import androidx.core.view.WindowInsetsCompat;
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
    
    public class MainActivity extends BaseActivity {
    
        private RecyclerView recyclerView;
        private MascotasAdapter adapter;
        private List<Mascotas> mascotasList = new ArrayList<>();

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
    
            setContentView(R.layout.activity_main);
    
            recyclerView = findViewById(R.id.recyclerViewMascotas);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new MascotasAdapter(mascotasList, this);
            recyclerView.setAdapter(adapter);
    
            new ObtenerAnimalesTask().execute();
            startActivity(new Intent(MainActivity.this, Registro.class));
            finish();
        }
    
        class ObtenerAnimalesTask extends AsyncTask<Void, Void, String>{
            @Override
            protected String doInBackground(Void... voids) {
    
                String urlServer = "http://192.168.0.192:8080/miapp/obtener_mascotas.php?id_usuario=1";
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String respuestaJson = null;
    
                try {
                    URL url = new URL(urlServer);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
    
                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuilder buffer = new StringBuilder();
                    if (inputStream == null) {
                        return null;
                    }
    
                    reader = new BufferedReader(new InputStreamReader(inputStream));
    
                    String line;
                     while ((line = reader.readLine()) != null){
                         buffer.append(line).append("\n");
                     }
    
                     if (buffer.length() == 0){
                         return null;
                     }
    
                     respuestaJson = buffer.toString();
                } catch (IOException e){
                    e.printStackTrace();
                    return null;
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        }catch (final IOException e){
                            e.printStackTrace();
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

                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            mascotasList.add(new Mascotas(
                                    jsonObject.getString("id_mascota"),
                                    jsonObject.getString("nombre"),
                                    jsonObject.getString("especie"),
                                    jsonObject.getString("raza"),
                                    jsonObject.getString("edad"),
                                    jsonObject.getString("sexo"),
                                    jsonObject.optString("foto_mascota", "") // Nuevo campo para la foto
                            ));
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("JSONError", "Error parsing JSON: " + e.getMessage());
                        Log.d("JSONResponse", "Server response: " + respuestaJson);
                    }
                } else {
                    Log.e("NetworkError", "Response is null");
                }
            }
        }
    }