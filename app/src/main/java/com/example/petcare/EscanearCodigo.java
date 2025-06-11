package com.example.petcare;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.HashMap;
import java.util.Map;

public class EscanearCodigo extends BaseActivity {

    private Button btnEscanear, btnNuevoEscaner, btnRedirigir;
    private TextView textViewResultado, textViewResultadoTitulo, textViewEscaneadoPor;
    private androidx.cardview.widget.CardView cardResultado;
    private ImageView imageViewQR;
    private String mascotaIdEscaneada = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_escanear_codigo, contentFrame, true);

        // Inicializar vistas
        btnEscanear = findViewById(R.id.btnEscanear);
        btnNuevoEscaner = findViewById(R.id.btnNuevoEscaner);
        btnRedirigir = findViewById(R.id.btnRedirigir);
        textViewResultado = findViewById(R.id.textViewResultado);
        textViewResultadoTitulo = findViewById(R.id.textViewResultadoTitulo);
        textViewEscaneadoPor = findViewById(R.id.textViewEscaneadoPor);
        cardResultado = findViewById(R.id.cardResultado);
        imageViewQR = findViewById(R.id.imageViewQR);

        // Configurar botón de escaneo
        btnEscanear.setOnClickListener(v -> {
            // Verificar permisos de cámara
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        101);
            } else {
                iniciarEscaneo();
            }
        });

        // Configurar botón de nuevo escaneo
        btnNuevoEscaner.setOnClickListener(v -> {
            cardResultado.setVisibility(View.GONE);
            btnNuevoEscaner.setVisibility(View.GONE);
            btnEscanear.setVisibility(View.VISIBLE);
            btnRedirigir.setVisibility(View.GONE);
            imageViewQR.setImageResource(R.drawable.icon_escanear_codigo);
            mascotaIdEscaneada = null;
        });

        // Configurar botón de redirección
        btnRedirigir.setOnClickListener(v -> {
            if (mascotaIdEscaneada != null) {
                redirectToVeterinarioMascota();
            }
        });

        // Configurar navegación
        navigationView.setCheckedItem(R.id.navMenuEscanearCodigo);
        View menuItemView = navigationView.findViewById(R.id.navMenuEscanearCodigo);
        if (menuItemView != null) {
            menuItemView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSecundario));
        }

        EdgeToEdge.enable(this);
    }

    private void iniciarEscaneo() {
        // Configurar el escáner de ZXing
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Escanea un código de barras o QR");
        integrator.setCameraId(0);  // Usar cámara trasera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show();
            } else {
                // Mostrar resultados y generar QR
                mostrarResultado(result.getContents(), result.getFormatName());
                Bitmap qrCode = generarQRCode(result.getContents(), 500);
                if (qrCode != null) {
                    imageViewQR.setImageBitmap(qrCode);
                }

                // Verificar si es un código de mascota
                verificarCodigoMascota(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void verificarCodigoMascota(String contenido) {
        Log.d("QR_DEBUG", "Contenido escaneado: " + contenido); // Verifica qué se está leyendo


        // Verifica si el contenido coincide con el formato esperado
        if (contenido.startsWith("petcare://veterinario/mascota/")) {
            try {
                // Extrae el ID de la mascota (elimina el prefijo)
                mascotaIdEscaneada = contenido.replace("petcare://veterinario/mascota/", "").trim();

                // Verifica que el ID sea numérico
                Long.parseLong(mascotaIdEscaneada); // Si no es número, lanzará excepción

                // Si llegó aquí, el formato es correcto y el ID es válido
                Log.d("QR_DEBUG", "ID de mascota válido: " + mascotaIdEscaneada);

                // Muestra el botón de redirección y actualiza la UI
                runOnUiThread(() -> {
                    btnRedirigir.setVisibility(View.VISIBLE);
                    textViewResultadoTitulo.setText("Mascota encontrada");
                    textViewResultado.setText("ID: " + mascotaIdEscaneada);
                });

            } catch (NumberFormatException e) {
                Log.e("QR_DEBUG", "El ID no es numérico: " + mascotaIdEscaneada);
                mascotaIdEscaneada = null;
                runOnUiThread(() -> {
                    btnRedirigir.setVisibility(View.GONE);
                    textViewResultadoTitulo.setText("Código escaneado");
                    textViewResultado.setText(contenido);
                });
            }
        } else {
            Log.d("QR_DEBUG", "Formato incorrecto. Se esperaba: petcare://veterinario/mascota/ID");
            mascotaIdEscaneada = null;
            runOnUiThread(() -> {
                btnRedirigir.setVisibility(View.GONE);
                textViewResultadoTitulo.setText("Código escaneado");
                textViewResultado.setText(contenido);
            });
        }
    }

    private void redirectToVeterinarioMascota() {
        if (mascotaIdEscaneada != null) {
            // Obtener el ID del usuario actual desde SharedPreferences
            SharedPreferences prefs = getSharedPreferences("miapp_prefs", MODE_PRIVATE);
            String idUsuario = prefs.getString("usuario_id", "0"); // "0" si no existe
            String tipoUsuario = prefs.getString("usuario_tipo", "usuario");

            if (tipoUsuario != null) {
                switch (tipoUsuario) {
                    case "admin":
                            // Verificar que ambos IDs sean válidos
                            if (!mascotaIdEscaneada.isEmpty() && !idUsuario.equals("0")) {
                                Intent intent = new Intent(this, VistaVeterinarioEnMascotas.class);
                                intent.putExtra("id_mascota", mascotaIdEscaneada);
                                intent.putExtra("id_usuario", idUsuario);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Error: No se pudo obtener la información necesaria", Toast.LENGTH_SHORT).show();
                            }
                        break;
                    case "veterinario":
                            if (!mascotaIdEscaneada.isEmpty() && !idUsuario.equals("0")) {
                                Intent intent = new Intent(this, VistaVeterinarioEnMascotas.class);
                                intent.putExtra("id_mascota", mascotaIdEscaneada);
                                intent.putExtra("id_usuario", idUsuario);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Error: No se pudo obtener la información necesaria", Toast.LENGTH_SHORT).show();
                            }
                        break;
                    case "usuario":
                            if (!mascotaIdEscaneada.isEmpty() && !idUsuario.equals("0")) {
                                Intent intent = new Intent(this, InformacionDeMascota.class);
                                intent.putExtra("id_mascota", mascotaIdEscaneada);
                                intent.putExtra("tipo_usuario", "visualizar");
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Error: No se pudo obtener la información necesaria", Toast.LENGTH_SHORT).show();
                            }
                        break;
                }
            }
        }
    }

    private Bitmap generarQRCode(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();
            Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void mostrarResultado(String contenido, String formato) {
        cardResultado.setVisibility(View.VISIBLE);
        btnNuevoEscaner.setVisibility(View.VISIBLE);
        btnEscanear.setVisibility(View.GONE);

        textViewResultado.setText(contenido);
        textViewEscaneadoPor.setText("Formato: " + formato);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarEscaneo();
            } else {
                Toast.makeText(this, "Se necesitan permisos de cámara para escanear", Toast.LENGTH_SHORT).show();
            }
        }
    }
}