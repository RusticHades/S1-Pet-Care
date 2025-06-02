package com.example.petcare;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InformacionDeMascota extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1001;
    private static final int IMAGE_WIDTH = 800;
    private static final int IMAGE_HEIGHT = 1200;

    private ImageView imgMascota, imgQR;
    private TextView txtNombre, txtEspecieRaza, txtEdad, txtSexo, txtPeso, txtEsterilizado, txtFechaNacimiento;
    private Button btnGenerarFicha;

    private String mascotaNombre, mascotaEspecie, mascotaRaza, mascotaEdad, mascotaSexo;
    private String mascotaPeso, mascotaEsterilizado, mascotaNacimiento, mascotaId;
    private Bitmap mascotaBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion_de_mascota);

        initViews();

        btnGenerarFicha = findViewById(R.id.btnGenerarPDF);
        btnGenerarFicha.setOnClickListener(v -> generarFichaMascota());

        mascotaId = getIntent().getStringExtra("id_mascota");
        if (mascotaId != null && !mascotaId.isEmpty()) {
            new ObtenerInfoMascotaTask().execute(mascotaId);
        } else {
            Toast.makeText(this, "Error: ID de mascota no válido", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        imgMascota = findViewById(R.id.imageViewMascota);
        imgQR = findViewById(R.id.imageViewQR);
        txtNombre = findViewById(R.id.textViewNombre);
        txtEspecieRaza = findViewById(R.id.textViewEspecieRaza);
        txtEdad = findViewById(R.id.textViewEdad);
        txtSexo = findViewById(R.id.textViewSexo);
        txtPeso = findViewById(R.id.textViewPeso);
        txtEsterilizado = findViewById(R.id.textViewEsterilizado);
        txtFechaNacimiento = findViewById(R.id.textViewFechaNacimiento);
    }

    private void generarFichaMascota() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Generando ficha...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Crear un bitmap para la ficha
                Bitmap fichaBitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(fichaBitmap);

                // Pintar fondo blanco
                canvas.drawColor(Color.WHITE);

                // Configurar pintura para el texto
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(40);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

                // Título
                canvas.drawText("Ficha Médica de Mascota", 50, 80, paint);

                // Datos de la mascota
                paint.setTextSize(30);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

                int yPos = 150;
                canvas.drawText("Nombre: " + mascotaNombre, 50, yPos, paint); yPos += 50;
                canvas.drawText("Especie: " + mascotaEspecie, 50, yPos, paint); yPos += 50;
                canvas.drawText("Raza: " + mascotaRaza, 50, yPos, paint); yPos += 50;
                canvas.drawText("Edad: " + mascotaEdad + " años", 50, yPos, paint); yPos += 50;
                canvas.drawText("Sexo: " + mascotaSexo, 50, yPos, paint); yPos += 50;
                canvas.drawText("Peso: " + mascotaPeso + " kg", 50, yPos, paint); yPos += 50;
                canvas.drawText("Esterilizado: " + mascotaEsterilizado, 50, yPos, paint); yPos += 50;
                canvas.drawText("Fecha Nacimiento: " + mascotaNacimiento, 50, yPos, paint); yPos += 80;

                // Agregar imagen de la mascota si está disponible
                if (mascotaBitmap != null) {
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(mascotaBitmap, 300, 300, true);
                    canvas.drawBitmap(scaledBitmap, 50, yPos, null);
                    yPos += 350;
                }

                // Agregar código QR
                Bitmap qrBitmap = generarQRCode("petcare://mascota/" + mascotaId, 300);
                if (qrBitmap != null) {
                    canvas.drawBitmap(qrBitmap, IMAGE_WIDTH - 350, yPos - 350, null);
                }

                // Pie de página
                paint.setTextSize(20);
                canvas.drawText("Generado el " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()),
                        50, IMAGE_HEIGHT - 50, paint);

                Bitmap finalFichaBitmap = fichaBitmap;
                handler.post(() -> {
                    progressDialog.dismiss();
                    mostrarFichaEnDialogo(finalFichaBitmap);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(InformacionDeMascota.this,
                            "Error al generar ficha: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("GenerarFicha", "Error", e);
                });
            }
        });
    }

    private void mostrarFichaEnDialogo(Bitmap fichaBitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ficha de " + mascotaNombre);

        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(fichaBitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        builder.setView(imageView);

        builder.setPositiveButton("Compartir", (dialog, which) -> compartirFicha(fichaBitmap));
        builder.setNegativeButton("Cerrar", null);

        if (tienePermisosAlmacenamiento()) {
            builder.setNeutralButton("Guardar", (dialog, which) -> guardarFicha(fichaBitmap));
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        // Ajustar el tamaño del diálogo
        imageView.post(() -> {
            View parent = (View) imageView.getParent();
            int width = parent.getWidth();
            int height = parent.getHeight();
            dialog.getWindow().setLayout(width, height);
        });
    }

    private void compartirFicha(Bitmap fichaBitmap) {
        try {
            File cacheFile = guardarImagenEnCache(fichaBitmap, "ficha_compartir.png");
            Uri contentUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", cacheFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Compartir ficha"));
        } catch (Exception e) {
            Toast.makeText(this, "Error al compartir: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarFicha(Bitmap fichaBitmap) {
        if (!tienePermisosAlmacenamiento()) {
            solicitarPermisosAlmacenamiento();
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                File file = crearArchivoFicha();

                try (FileOutputStream out = new FileOutputStream(file)) {
                    fichaBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }

                // Notificar a la galería
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(file));
                sendBroadcast(mediaScanIntent);

                handler.post(() -> {
                    Toast.makeText(this,
                            "Ficha guardada en: " + file.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                handler.post(() -> {
                    Toast.makeText(this,
                            "Error al guardar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private File guardarImagenEnCache(Bitmap bitmap, String nombreArchivo) throws IOException {
        File cacheDir = getCacheDir();
        File cacheFile = new File(cacheDir, nombreArchivo);

        try (FileOutputStream out = new FileOutputStream(cacheFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        }

        return cacheFile;
    }

    private File crearArchivoFicha() throws IOException {
        File storageDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        }

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new IOException("No se pudo crear el directorio");
        }

        String fileName = "Ficha_" + mascotaNombre.replaceAll("[^a-zA-Z0-9_]", "_") + ".png";
        return new File(storageDir, fileName);
    }

    private boolean tienePermisosAlmacenamiento() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED;
    }

    private void solicitarPermisosAlmacenamiento() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE_STORAGE_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Permisos denegados. No se puede guardar la ficha.",
                        Toast.LENGTH_SHORT).show();
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

    private class ObtenerInfoMascotaTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String idMascota = params[0];
            String urlServer = getString(R.string.url_servidor) + "/miapp/obtener_info_completa_mascota.php?id_mascota=" + idMascota;

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
                Log.e("InformacionMascota", "Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) {
                Toast.makeText(InformacionDeMascota.this, "Error al cargar los datos", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            try {
                JSONObject mascota = new JSONObject(result);

                if (mascota.has("error")) {
                    Toast.makeText(InformacionDeMascota.this, mascota.getString("error"), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                mascotaNombre = mascota.getString("nombre");
                mascotaEspecie = mascota.getString("especie");
                mascotaRaza = mascota.getString("raza");
                mascotaEdad = mascota.getString("edad");
                mascotaSexo = mascota.getString("sexo");
                mascotaPeso = mascota.getString("peso");
                mascotaEsterilizado = mascota.getString("esterilizado").equals("1") ? "Sí" : "No";
                mascotaNacimiento = mascota.getString("fecha_nacimiento");

                txtNombre.setText(mascotaNombre);
                txtEspecieRaza.setText(mascotaEspecie + " - " + mascotaRaza);
                txtEdad.setText(mascotaEdad + " años");
                txtSexo.setText(mascotaSexo);
                txtPeso.setText(mascotaPeso + " kg");
                txtEsterilizado.setText(mascotaEsterilizado);
                txtFechaNacimiento.setText(mascotaNacimiento);

                cargarImagenMascota(mascota.optString("foto_mascota", ""));

                // Generar QR para redirección
                String qrContent = "petcare://mascota/" + mascotaId;
                Bitmap qrBitmap = generarQRCode(qrContent, 400);
                if (qrBitmap != null) {
                    imgQR.setImageBitmap(qrBitmap);
                }
            } catch (JSONException e) {
                Log.e("InformacionMascota", "Error parsing JSON: " + e.getMessage());
                Toast.makeText(InformacionDeMascota.this, "Error al procesar los datos", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void cargarImagenMascota(String fotoMascota) {
        if (fotoMascota.isEmpty()) {
            imgMascota.setImageResource(R.drawable.foto_perfil_mascota);
            return;
        }

        if (fotoMascota.startsWith("images/")) {
            new CargarImagenTask(imgMascota).execute(getString(R.string.url_servidor) + "/miapp/" + fotoMascota);
        } else {
            try {
                byte[] decodedString = Base64.decode(fotoMascota, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgMascota.setImageBitmap(decodedByte);
            } catch (Exception e) {
                imgMascota.setImageResource(R.drawable.foto_perfil_mascota);
            }
        }
    }

    private static class CargarImagenTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public CargarImagenTask(ImageView imageView) {
            this.imageView = imageView;
        }

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
                Log.e("CargarImagen", "Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(R.drawable.foto_perfil_mascota);
            }
        }
    }
}