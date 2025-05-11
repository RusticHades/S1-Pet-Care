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
    
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(InicioDeSesion.this, Registro.class));
            }
        });
    
        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String correo = txtCorreoElectronico.getText().toString();
                String contrasenia = txtContrasenia.getText().toString();
    
                if(validarDatos(correo, contrasenia)){
                    iniciarSesion(correo, contrasenia);
                }
            }
        });
    }
    
    private boolean validarDatos(String correo, String contrasenia){
    
        if (correo.isEmpty()) {
            txtCorreoElectronico.setError("Ingrese un nombre de usuario");
            txtCorreoElectronico.requestFocus();
            return false;
        }
    
        if (contrasenia.isEmpty()) {
            txtContrasenia.setError("Ingrese una contraseña");
            txtContrasenia.requestFocus();
            return false;
        } else if (contrasenia.length() < 8) {
            txtContrasenia.setError("La contraseña debe tener al menos 8 caracteres");
            txtContrasenia.requestFocus();
            return false;
        }
        return true;
    }
    
    private void iniciarSesion(String correoElectronico, String contrasenia) {
        String url = "http://192.168.0.192:8080/miapp/login_usuario.php";
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
                InputStream inputStream = responseCode == HttpURLConnection.HTTP_OK ? urlConnection.getInputStream() : urlConnection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                Log.d("LOGIN", "Respuesta del servidor: " + response.toString());
    
                try {
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");
    
                    runOnUiThread(() -> {
                        if (success) {
                            try {
                                // Obtener datos del usuario
                                JSONObject usuarioJson = jsonResponse.getJSONObject("usuario");
                                String nombreUsuario = usuarioJson.getString("usuario");
                                String tipoUsuario = usuarioJson.getString("tipo_usuario");
                                String fotoUsuarioBase64 = usuarioJson.optString("foto_usuario", null);
    
                                // Guardar datos de sesion
                                SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("usuario_id", usuarioJson.getString("id"));
                                editor.putString("usuario_nombre", nombreUsuario);
                                editor.putString("usuario_correo", correoElectronico);
                                editor.putString("usuario_tipo", tipoUsuario);
                                if (fotoUsuarioBase64 != null && !fotoUsuarioBase64.isEmpty()) {
                                    editor.putString("usuario_foto", fotoUsuarioBase64);
                                }
                                editor.apply();
    
                                Toast.makeText(InicioDeSesion.this, message, Toast.LENGTH_SHORT).show();
    
                                // Redirigir al Inicio
                                startActivity(new Intent(InicioDeSesion.this, Inicio.class));
                                finish();

                            } catch (JSONException e) {
                                Toast.makeText(InicioDeSesion.this, "Error al procesar datos del usuario", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(InicioDeSesion.this, "Error: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException e) {
                    runOnUiThread(() -> {
                        Toast.makeText(InicioDeSesion.this, "Error en formato de respuesta: " + response.toString(), Toast.LENGTH_LONG).show();
                    });
                    e.printStackTrace();
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(InicioDeSesion.this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }
}