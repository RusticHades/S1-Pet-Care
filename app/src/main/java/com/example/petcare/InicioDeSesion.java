package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class InicioDeSesion extends AppCompatActivity {
    private EditText txtCorreoElectronico, txtContrasenia;
    private Button btnRegistrar, btnIniciar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio_de_sesion);

        // Inicializar vistas
        txtCorreoElectronico = findViewById(R.id.editTextUsuario);
        txtContrasenia = findViewById(R.id.editTextContrasenia);
        btnRegistrar = findViewById(R.id.buttonRegistrar);
        btnIniciar = findViewById(R.id.buttonIniciar);

        btnRegistrar.setOnClickListener(v ->
                startActivity(new Intent(InicioDeSesion.this, Registro.class)));

        btnIniciar.setOnClickListener(v -> {
            String correo = txtCorreoElectronico.getText().toString();
            String contrasenia = txtContrasenia.getText().toString();

            if(validarDatos(correo, contrasenia)){
                iniciarSesion(correo, contrasenia);
            }
        });

        //verificarSesionActiva();
    }

    private void verificarSesionActiva() {
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
        if (prefs.contains("usuario_id")) {
            startActivity(new Intent(this, Inicio.class));
            finish();
        }
    }

    private boolean validarDatos(String correo, String contrasenia) {
        if (correo.isEmpty()) {
            txtCorreoElectronico.setError("Ingrese su correo electrónico");
            txtCorreoElectronico.requestFocus();
            return false;
        }

        if (contrasenia.isEmpty()) {
            txtContrasenia.setError("Ingrese su contraseña");
            txtContrasenia.requestFocus();
            return false;
        }
        return true;
    }

    private void iniciarSesion(String correoElectronico, String contrasenia) {
        String url = getString(R.string.url_servidor) + "/miapp/login_usuario.php";

        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL urlObj = new URL(url);
                urlConnection = (HttpURLConnection) urlObj.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String postData = "correo=" + URLEncoder.encode(correoElectronico, "UTF-8") +
                        "&contrasenia=" + URLEncoder.encode(contrasenia, "UTF-8");

                try (OutputStream outputStream = urlConnection.getOutputStream()) {
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                }

                int responseCode = urlConnection.getResponseCode();
                InputStream inputStream = responseCode == HttpURLConnection.HTTP_OK ?
                        urlConnection.getInputStream() : urlConnection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                Log.d("LOGIN", "Respuesta del servidor: " + response);

                procesarRespuestaLogin(response.toString(), correoElectronico);

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(InicioDeSesion.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show());
                Log.e("LOGIN", "Error: ", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private void procesarRespuestaLogin(String jsonResponse, String correo) {
        try {
            JSONObject json = new JSONObject(jsonResponse);
            boolean success = json.getBoolean("success");
            String message = json.getString("message");

            runOnUiThread(() -> {
                if (success) {
                    try {
                        JSONObject usuarioJson = json.getJSONObject("usuario");
                        String fotoUrl = json.optString("foto_url", null);

                        // Guardar datos de sesión
                        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("usuario_id", usuarioJson.getString("id"));
                        editor.putString("usuario_nombre", usuarioJson.getString("usuario"));
                        editor.putString("usuario_correo", correo);
                        editor.putString("usuario_tipo", usuarioJson.getString("tipo_usuario"));
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            editor.putString("usuario_foto_url", fotoUrl);
                        }
                        editor.apply();

                        // Redirigir a la pantalla principal
                        startActivity(new Intent(InicioDeSesion.this, Inicio.class));
                        finish();

                    } catch (JSONException e) {
                        Toast.makeText(InicioDeSesion.this, "Error al procesar datos del usuario", Toast.LENGTH_LONG).show();
                        Log.e("LOGIN", "Error parsing user data", e);
                    }
                } else {
                    Toast.makeText(InicioDeSesion.this, message, Toast.LENGTH_LONG).show();
                }
            });

        } catch (JSONException e) {
            runOnUiThread(() ->
                    Toast.makeText(InicioDeSesion.this, "Error en formato de respuesta", Toast.LENGTH_LONG).show());
            Log.e("LOGIN", "Error parsing JSON", e);
        }
    }
}