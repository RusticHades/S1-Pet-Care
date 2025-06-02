package com.example.petcare;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VistaVeterinarioEnMascotas extends AppCompatActivity {

    private String mascotaId;
    private TextView txtNombre, txtEspecie, txtRaza, txtEdad, txtHistorial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vista_veterinario_en_mascotas);

        mascotaId = getIntent().getStringExtra("id_mascota");
        //  initViews();

        if (mascotaId != null) {
            new ObtenerInfoMascotaVeterinarioTask().execute(mascotaId);
        } else {
            Toast.makeText(this, "ID de mascota no válido", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //private void initViews() {
    //    txtNombre = findViewById(R.id.txtNombre);
    //    txtEspecie = findViewById(R.id.txtEspecie);
    //    txtRaza = findViewById(R.id.txtRaza);
    //    txtEdad = findViewById(R.id.txtEdad);
    //    txtHistorial = findViewById(R.id.txtHistorial);
    //}

    private class ObtenerInfoMascotaVeterinarioTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String idMascota = params[0];
            String urlServer = getString(R.string.url_servidor) + "/miapp/obtener_info_veterinario_mascota.php?id_mascota=" + idMascota;

            try {
                URL url = new URL(urlServer);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return "Error: " + responseCode;
                }

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                inputStream.close();
                return response.toString();
            } catch (Exception e) {
                Log.e("VeterinarioMascota", "Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(VistaVeterinarioEnMascotas.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            try {
                JSONObject mascota = new JSONObject(result);

                if (mascota.has("error")) {
                    Toast.makeText(VistaVeterinarioEnMascotas.this, mascota.getString("error"), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                txtNombre.setText(mascota.getString("nombre"));
                txtEspecie.setText("Especie: " + mascota.getString("especie"));
                txtRaza.setText("Raza: " + mascota.getString("raza"));
                txtEdad.setText("Edad: " + mascota.getString("edad") + " años");
                txtHistorial.setText("Historial Médico:\n" + mascota.optString("historial", "No hay historial registrado"));

            } catch (JSONException e) {
                Log.e("VeterinarioMascota", "Error parsing JSON: " + e.getMessage());
                Toast.makeText(VistaVeterinarioEnMascotas.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}