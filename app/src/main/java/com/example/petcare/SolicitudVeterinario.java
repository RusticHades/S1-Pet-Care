package com.example.petcare;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SolicitudVeterinario extends AppCompatActivity {

    private EditText etEspecialidad, etUbicacion, etTelefono;
    private Button btnSeleccionarCertificado, btnEnviarSolicitud;
    private byte[] certificadoBytes;
    private String nombreCertificado;

    private static final int CODIGO_SELECCIONAR_DOCUMENTO = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitud_veterinario);

        etEspecialidad = findViewById(R.id.etEspecialidad);
        etUbicacion = findViewById(R.id.etUbicacion);
        etTelefono = findViewById(R.id.etTelefono);
        btnSeleccionarCertificado = findViewById(R.id.btnSeleccionarCertificado);
        btnEnviarSolicitud = findViewById(R.id.btnEnviarSolicitud);

        btnSeleccionarCertificado.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, CODIGO_SELECCIONAR_DOCUMENTO);
        });

        btnEnviarSolicitud.setOnClickListener(v -> {
            String especialidad = etEspecialidad.getText().toString().trim();
            String ubicacion = etUbicacion.getText().toString().trim();
            String telefono = etTelefono.getText().toString().trim();

            if (validarDatos(especialidad, ubicacion, telefono)) {
                enviarSolicitudVeterinario(especialidad, ubicacion, telefono);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CODIGO_SELECCIONAR_DOCUMENTO && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            nombreCertificado = obtenerNombreArchivo(uri);

            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                certificadoBytes = getBytes(inputStream);
                btnSeleccionarCertificado.setText("Certificado seleccionado: " + nombreCertificado);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al leer el archivo", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validarDatos(String especialidad, String ubicacion, String telefono) {
        if (especialidad.isEmpty()) {
            etEspecialidad.setError("Ingrese su especialidad");
            return false;
        }

        if (ubicacion.isEmpty()) {
            etUbicacion.setError("Ingrese su ubicación");
            return false;
        }

        if (telefono.isEmpty()) {
            etTelefono.setError("Ingrese su teléfono");
            return false;
        }

        if (certificadoBytes == null) {
            Toast.makeText(this, "Seleccione un certificado", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void enviarSolicitudVeterinario(String especialidad, String ubicacion, String telefono) {
        // Obtener el ID del usuario actual (debes implementar esto según tu sistema de autenticación)
        int usuarioId = 2;

        new Thread(() -> {
            try {
                URL url = new URL(getString(R.string.url_servidor) + "/miapp/enviar_solicitud_veterinario.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                String boundary = "Boundary-" + System.currentTimeMillis();
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                OutputStream outputStream = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

                // Añadir parámetros
                addFormField(writer, "usuario_id", String.valueOf(usuarioId), boundary);
                addFormField(writer, "especialidad", especialidad, boundary);
                addFormField(writer, "ubicacion", ubicacion, boundary);
                addFormField(writer, "telefono", telefono, boundary);

                // Añadir archivo del certificado
                addFilePart(writer, outputStream, "certificado", certificadoBytes, nombreCertificado, boundary);

                writer.append("--").append(boundary).append("--").append("\r\n");
                writer.flush();
                writer.close();

                // Dentro del método enviarSolicitudVeterinario(), después de obtener la respuesta:
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    try {
                        JSONObject jsonResponse = new JSONObject(response.toString());
                        boolean success = jsonResponse.getBoolean("success");
                        String message = jsonResponse.getString("message");

                        // Dentro del método enviarSolicitudVeterinario(), reemplaza este bloque:
                        runOnUiThread(() -> {
                            if (success) {
                                new AlertDialog.Builder(SolicitudVeterinario.this)  // Usa SolicitudVeterinario.this en lugar de solo this
                                        .setTitle("Solicitud Enviada")
                                        .setMessage(message)
                                        .setPositiveButton("Aceptar", (dialog, which) -> {
                                            // Esto se ejecutará solo cuando el usuario presione Aceptar
                                            startActivity(new Intent(SolicitudVeterinario.this, InicioDeSesion.class));
                                            finish(); // Cierra la actividad actual
                                        })
                                        .setIcon(R.drawable.icon_sin_mascotas)
                                        .setCancelable(false) // Evita que el diálogo se cierre tocando fuera de él
                                        .show();
                                certificadoBytes = null;
                            } else {
                                Toast.makeText(SolicitudVeterinario.this, "Error: " + message, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Error al procesar la respuesta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                } else {
                    // Manejo de errores HTTP (mantén el código existente)
                    String errorResponse = readErrorStream(connection);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Error en el servidor (" + responseCode + "): " + errorResponse, Toast.LENGTH_LONG).show();
                    });
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String readErrorStream(HttpURLConnection connection) {
        try {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                return response.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "No se pudo obtener el mensaje de error";
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String obtenerNombreArchivo(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void addFormField(PrintWriter writer, String name, String value, String boundary) {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n");
        writer.append("\r\n");
        writer.append(value).append("\r\n");
        writer.flush();
    }

    private void addFilePart(PrintWriter writer, OutputStream outputStream, String fieldName, byte[] fileData, String fileName, String boundary) throws IOException {
        writer.append("--").append(boundary).append("\r\n");
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(fileName).append("\"\r\n");
        writer.append("Content-Type: image/jpeg\r\n");
        writer.append("Content-Transfer-Encoding: binary\r\n");
        writer.append("\r\n");
        writer.flush();

        outputStream.write(fileData);
        outputStream.flush();

        writer.append("\r\n");
        writer.flush();
    }


}