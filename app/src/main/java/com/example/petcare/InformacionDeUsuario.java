package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InformacionDeUsuario extends BaseActivity {

    private TextView txtUsuario, txtTipoUsuario, txtCorreo, txtTelefono, txtDireccion, txtMensaje;
    private ImageView imgFotoPerfil;
    private Button btnEditarPerfil, btnAgregrarMascota;

    private RecyclerView rycViewMascotas;
    private MascotasAdapter adapter;
    private List<Mascotas> mascotasList = new ArrayList<>();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_informacion_de_usuario, contentFrame, true);

        // Configuración del NavigationView
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerLayout = headerView.findViewById(R.id.headerLayout);
        if (headerLayout != null) {
            headerLayout.setSelected(true);
        }

        // Inicializar vistas
        txtUsuario = findViewById(R.id.textViewUsuario);
        txtTipoUsuario = findViewById(R.id.textViewTipoUsuario);
        txtCorreo = findViewById(R.id.textViewcorreo);
        txtTelefono = findViewById(R.id.textViewTelefono);
        txtDireccion = findViewById(R.id.textViewdireccion);
        txtMensaje = findViewById(R.id.textViewMensaje);

        imgFotoPerfil = findViewById(R.id.imageViewFotoPerfil);

        btnEditarPerfil = findViewById(R.id.buttonEditarPerfil);
        btnAgregrarMascota = findViewById(R.id.buttonAgregarMascota);
        rycViewMascotas = findViewById(R.id.recyclerViewMascotas);

        rycViewMascotas.setLayoutManager(new LinearLayoutManager(this));
        txtCorreo.setSelected(true);

        // Cargar datos del usuario y sus mascotas
        cargarDatosUsuario();

        rycViewMascotas = findViewById(R.id.recyclerViewMascotas);
        rycViewMascotas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MascotasAdapter(mascotasList);
        rycViewMascotas.setAdapter(adapter);

        new InformacionDeUsuario.ObtenerAnimalesTask().execute();


        EdgeToEdge.enable(this);

        btnEditarPerfil.setOnClickListener(v ->
                startActivityForResult(new Intent(InformacionDeUsuario.this, EditarPerfil.class),1001));

        btnAgregrarMascota.setOnClickListener(v ->
                startActivity(new Intent(InformacionDeUsuario.this, AgregarMascota.class)));
    }

    private void cargarDatosUsuario() {
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);

        // Obtener datos del usuario
        String nombre = prefs.getString("usuario_nombre", "No disponible");
        String correo = prefs.getString("usuario_correo", "No disponible");
        String tipoUsuario = "Tipo de usuario: " + prefs.getString("usuario_tipo", "No disponible");
        String telefono = prefs.getString("usuario_telefono", "");
        String direccion = prefs.getString("usuario_direccion", "");
        String fotoUrl = prefs.getString("usuario_foto_url", null);

        // Procesar datos opcionales
        telefono = (telefono == null || telefono.isEmpty()) ? "No disponible" : telefono;
        direccion = (direccion == null || direccion.isEmpty()) ? "No disponible" : direccion;

        // Mostrar datos en los TextView
        txtUsuario.setText(nombre);
        txtCorreo.setText(correo);
        txtTipoUsuario.setText(tipoUsuario);
        txtTelefono.setText(telefono);
        txtDireccion.setText(direccion);

        if(!telefono.equals("No disponible") || !direccion.equals("No disponible")) {
            txtMensaje.setVisibility(View.GONE);
        }

        // Cargar foto de perfil
        if (fotoUrl != null && !fotoUrl.isEmpty()) {
            new CargarImagenTask().execute(fotoUrl);
        } else {
            imgFotoPerfil.setImageResource(R.drawable.usuario_predeterminado);
        }
    }

    // AsyncTask para cargar la imagen
    private class ObtenerAnimalesTask extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {

            String urlServer = getString(R.string.url_servidor) + "/miapp/obtener_mascotas.php?id_usuario=2";
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
                                jsonObject.getString("sexo")
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

    private class CargarImagenTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imgFotoPerfil.setImageBitmap(result);
            } else {
                imgFotoPerfil.setImageResource(R.drawable.usuario_predeterminado);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Verificar si se actualizó el perfil
            if (data != null && data.getBooleanExtra("usuario_actualizado", false)) {
                // Actualizar los TextView con los nuevos datos
                txtUsuario.setText(data.getStringExtra("nuevo_nombre"));
                txtCorreo.setText(data.getStringExtra("nuevo_correo"));
                txtTelefono.setText(data.getStringExtra("nuevo_telefono"));
                txtDireccion.setText(data.getStringExtra("nuevo_direccion"));

                // Actualizar la foto si hay nueva URL
                String nuevaFotoUrl = data.getStringExtra("nueva_foto_url");
                if (nuevaFotoUrl != null && !nuevaFotoUrl.isEmpty()) {
                    new CargarImagenTask().execute(nuevaFotoUrl);
                }

                // Ocultar mensaje si hay datos
                if (!data.getStringExtra("nuevo_telefono").isEmpty() ||
                        !data.getStringExtra("nuevo_direccion").isEmpty()) {
                    txtMensaje.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Restaurar estado del header al salir
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerLayout = headerView.findViewById(R.id.headerLayout);
        if (headerLayout != null) {
            headerLayout.setSelected(false);
        }
    }
}