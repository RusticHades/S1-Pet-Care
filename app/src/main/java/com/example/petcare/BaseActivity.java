package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.net.HttpURLConnection;
import java.net.URL;

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    private ImageButton btnMenuHamburguesa;
    private View headerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        Toolbar toolbar = findViewById(R.id.toolbarMenu);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        // Inicializar vistas
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        actualizarHeaderNavigationView(); //Metodo para colocar el nombre y foto de perfil del usuario xd
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);
        LinearLayout headerLayout = headerView.findViewById(R.id.headerLayout);

        headerLayout.setOnClickListener(v -> {
            abrirActivityInformacionDeUsuario(); //Metodo para abrir la activity de informacion del usuario usando el nav header
        });


        // Configuracion del ImageButton para abrir el menu
        btnMenuHamburguesa = findViewById(R.id.buttonMenuHamburguesa);
        btnMenuHamburguesa.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarHeaderNavigationView();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        int id = item.getItemId();

        if (id == R.id.navMenuInicio) {
            //If para asegurarse que no se abren multiples activitys iguales
            if (!(this instanceof Inicio)) {
                startActivity(new Intent(this, Inicio.class));
                finish();
            }
        } else if (id == R.id.navMenuHistorial) {

        } else if (id == R.id.navMenuAgendar) {

        } else if (id == R.id.navMenuCitas) {

        }
        return true;
    }

    //Metodo para abrir la activyti de informacion del usuario
    protected void abrirActivityInformacionDeUsuario() {
        startActivity(new Intent(this, InformacionDeUsuario.class));
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    public void actualizarHeaderNavigationView() {
        runOnUiThread(() -> {
            if (navigationView != null) {
                View headerView = navigationView.getHeaderView(0);
                TextView textViewUserName = headerView.findViewById(R.id.textViewUserName);
                ImageView imageViewProfile = headerView.findViewById(R.id.imageViewProfile);

                SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);

                // Obtener datos actualizados del usuario
                String nombre = prefs.getString("usuario_nombre", "Nombre de Usuario");
                String fotoUrl = prefs.getString("usuario_foto_url", null); // Cambiado a foto_url

                // Actualizar nombre
                textViewUserName.setText(nombre);

                // Actualizar foto de perfil
                if (fotoUrl != null && !fotoUrl.isEmpty()) {
                    new CargarImagenHeaderTask(imageViewProfile).execute(fotoUrl);
                } else {
                    imageViewProfile.setImageResource(R.drawable.usuario_predeterminado);
                    imageViewProfile.setBackground(null);
                }
            }
        });
    }

    // AsyncTask para cargar la imagen del header
    private class CargarImagenHeaderTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public CargarImagenHeaderTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                return BitmapFactory.decodeStream(connection.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setBackground(null);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(R.drawable.usuario_predeterminado);
                imageView.setBackground(null);
            }
        }
    }
}