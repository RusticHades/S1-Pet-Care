package com.example.petcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        actualizarHeaderNavigationView();
        navigationView.setNavigationItemSelectedListener(this);

        headerView = navigationView.getHeaderView(0);
        LinearLayout headerLayout = headerView.findViewById(R.id.headerLayout);

        headerLayout.setOnClickListener(v -> {
            abrirActivityInformacionDeUsuario();
        });

        // Configurar visibilidad de items del menú según tipo de usuario
        configurarMenuSegunTipoUsuario();

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

    private void configurarMenuSegunTipoUsuario() {
        Menu menu = navigationView.getMenu();
        String userType = getTipoUsuario(); // Obtener tipo de usuario de SharedPreferences

        MenuItem adminItem = menu.findItem(R.id.navMenuAdminPanel);
        MenuItem vetRequestItem = menu.findItem(R.id.navMenuSolicitarVeterinario);

        adminItem.setVisible(false);

        if (userType != null) {
            switch (userType) {
                case "admin":
                    adminItem.setVisible(true);
                    vetRequestItem.setVisible(false);
                    break;
                case "veterinario":
                    vetRequestItem.setVisible(false);
                    break;
            }
        }
    }

    private String getTipoUsuario() {
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
        return prefs.getString("usuario_tipo", "usuario");
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarHeaderNavigationView();
        configurarMenuSegunTipoUsuario(); // Actualizar menú por si cambió el tipo de usuario
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        int id = item.getItemId();

        if (id == R.id.navMenuInicio) {
            if (!(this instanceof Inicio)) {
                startActivity(new Intent(this, Inicio.class));
                finish();
            }
        } else if (id == R.id.navMenuEscanearCodigo) {
            if (!getClass().equals(EscanearCodigo.class)) {
                startActivity(new Intent(this, EscanearCodigo.class));
                finish();
            }
        } else if (id == R.id.navMenuCerrarSesion) {
            cerrarSesion();
        } else if (id == R.id.navMenuSolicitarVeterinario) {
            mostrarDialogoSolicitudVeterinario();
            return true;
        } else if (id == R.id.navMenuAdminPanel) {
            if (!getClass().equals(AdminVeterinarios.class)) {
                startActivity(new Intent(this, AdminVeterinarios.class));
                finish();
            }
            return true;
        }
        return true;
    }

    private void cerrarSesion() {
        SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, InicioDeSesion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarDialogoSolicitudVeterinario() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Solicitar ser Veterinario");
        builder.setMessage("¿Deseas enviar una solicitud para convertirte en veterinario?");

        builder.setPositiveButton("Sí", (dialog, which) -> {
            // Llevar a la activity de solicitud de veterinario
            startActivity(new Intent(this, SolicitudVeterinario.class));
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

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

                String nombre = prefs.getString("usuario_nombre", "Nombre de Usuario");
                String fotoUrl = prefs.getString("usuario_foto_url", null);

                textViewUserName.setText(nombre);

                if (fotoUrl != null && !fotoUrl.isEmpty()) {
                    new CargarImagenHeaderTask(imageViewProfile).execute(fotoUrl);
                } else {
                    imageViewProfile.setImageResource(R.drawable.usuario_predeterminado);
                    imageViewProfile.setBackground(null);
                }
            }
        });
    }

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