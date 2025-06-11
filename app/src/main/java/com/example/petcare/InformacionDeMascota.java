package com.example.petcare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private String mascotaPeso, mascotaEsterilizado, mascotaNacimiento, mascotaId, tipoUsuario;
    private Bitmap mascotaBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informacion_de_mascota);

        initViews();

        btnGenerarFicha = findViewById(R.id.btnGenerarFicha);
        btnGenerarFicha.setOnClickListener(v -> generarFichaMascota());

        mascotaId = getIntent().getStringExtra("id_mascota");
        tipoUsuario = getIntent().getStringExtra("tipo_usuario");

        if(tipoUsuario.equals("visualizar")){
            LinearLayout linearLayout = findViewById(R.id.linearLayoutCodigoId);
            linearLayout.setVisibility(View.GONE);
        }

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
                // Tamaño A4 en pixels (842x1190 a 72dpi)
                int width = 842;
                int height = 1190;
                Bitmap fichaBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(fichaBitmap);

                // Fondo con tu color
                Paint bgPaint = new Paint();
                bgPaint.setColor(ContextCompat.getColor(this, R.color.colorFondo));
                canvas.drawRect(0, 0, width, height, bgPaint);

                // Encabezado con tu color principal
                Paint headerPaint = new Paint();
                headerPaint.setColor(ContextCompat.getColor(this, R.color.colorPrincipal));
                canvas.drawRect(0, 0, width, 150, headerPaint);

                // Logo o título
                Paint titlePaint = new Paint();
                titlePaint.setColor(Color.WHITE);
                titlePaint.setTextSize(48);
                titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                titlePaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("PETCARE - CARNET DE MASCOTA", width/2, 100, titlePaint);

                // Configurar pintura para el texto
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(32);
                textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

                // Marco para la foto con tu color secundario
                Paint framePaint = new Paint();
                framePaint.setColor(ContextCompat.getColor(this, R.color.colorSecundario));
                framePaint.setStyle(Paint.Style.STROKE);
                framePaint.setStrokeWidth(5);

                // Variables para posicionamiento dinámico
                int yPos = 200; // Posición Y inicial después del encabezado
                int leftMargin = 50;
                int rightColumn = width/2 + 50;
                int lineHeight = 40; // Altura base por línea
                int sectionSpacing = 20; // Espacio entre secciones

                // Datos de la mascota en dos columnas
                // Columna izquierda
                canvas.drawText("Nombre:", leftMargin, yPos, textPaint);
                canvas.drawText(mascotaNombre, leftMargin + 150, yPos, textPaint);

                canvas.drawText("Raza:", leftMargin, yPos + lineHeight, textPaint);
                canvas.drawText(mascotaRaza, leftMargin + 150, yPos + lineHeight, textPaint);

                canvas.drawText("Sexo:", leftMargin, yPos + (lineHeight * 2), textPaint);
                canvas.drawText(mascotaSexo, leftMargin + 150, yPos + (lineHeight * 2), textPaint);

                String[] esterilizadoLines = dividirTexto("Esterilizado: " + mascotaEsterilizado, 20);
                for (int i = 0; i < esterilizadoLines.length; i++) {
                    canvas.drawText(esterilizadoLines[i], leftMargin, yPos + (lineHeight * 3) + (i * lineHeight), textPaint);
                }

                // Columna derecha
                canvas.drawText("Especie:", rightColumn, yPos, textPaint);
                canvas.drawText(mascotaEspecie, rightColumn + 150, yPos, textPaint);

                canvas.drawText("Edad:", rightColumn, yPos + lineHeight, textPaint);
                canvas.drawText(mascotaEdad, rightColumn + 150, yPos + lineHeight, textPaint);

                canvas.drawText("Peso:", rightColumn, yPos + (lineHeight * 2), textPaint);
                canvas.drawText(mascotaPeso, rightColumn + 150, yPos + (lineHeight * 2), textPaint);

                String[] nacimientoLines = dividirTexto("Nacimiento: " + mascotaNacimiento, 20);
                for (int i = 0; i < nacimientoLines.length; i++) {
                    canvas.drawText(nacimientoLines[i], rightColumn, yPos + (lineHeight * 3) + (i * lineHeight), textPaint);
                }

                // Ajustar posición Y para la siguiente sección (foto + QR)
                yPos += (lineHeight * 4) + sectionSpacing;

                // Agregar imagen de la mascota
                if (mascotaBitmap != null) {
                    int photoSize = 300;
                    Bitmap circularBitmap = getCircularBitmap(
                            Bitmap.createScaledBitmap(mascotaBitmap, photoSize, photoSize, true)
                    );
                    canvas.drawBitmap(circularBitmap, leftMargin, yPos, null);
                    canvas.drawCircle(leftMargin + photoSize/2, yPos + photoSize/2, photoSize/2 + 5, framePaint);
                }

                // Agregar código QR con marco
                Bitmap qrBitmap = generarQRCode("petcare://veterinario/mascota/" + mascotaId, 250);
                if (qrBitmap != null) {
                    canvas.drawBitmap(qrBitmap, rightColumn, yPos, null);
                    canvas.drawRect(rightColumn - 5, yPos - 5, rightColumn + qrBitmap.getWidth() + 5,
                            yPos + qrBitmap.getHeight() + 5, framePaint);
                }

                // Pie de página
                Paint footerPaint = new Paint();
                footerPaint.setColor(Color.DKGRAY);
                footerPaint.setTextSize(20);
                footerPaint.setTextAlign(Paint.Align.CENTER);
                String footerText = "Generado el " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()) +
                        " - PetCare © 2023";
                canvas.drawText(footerText, width/2, height - 30, footerPaint);

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
    private String[] dividirTexto(String texto, int maxChars) {
        if (texto.length() <= maxChars) {
            return new String[]{texto};
        }

        ArrayList<String> lines = new ArrayList<>();
        int start = 0;
        while (start < texto.length()) {
            int end = Math.min(start + maxChars, texto.length());
            lines.add(texto.substring(start, end));
            start = end;
        }

        return lines.toArray(new String[0]);
    }

    private void mostrarFichaEnDialogo(Bitmap fichaBitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Carnet de " + mascotaNombre);

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
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2f, bitmap.getHeight() / 2f, bitmap.getWidth() / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private void compartirFicha(Bitmap fichaBitmap) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Preparando para compartir...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Crear un archivo temporal en el directorio de caché externo
                File cachePath = new File(getExternalCacheDir(), "shared_images");
                if (!cachePath.exists()) {
                    cachePath.mkdirs();
                }

                String fileName = "ficha_" + System.currentTimeMillis() + ".png";
                File file = new File(cachePath, fileName);

                try (FileOutputStream out = new FileOutputStream(file)) {
                    fichaBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                }

                Uri contentUri = FileProvider.getUriForFile(
                        InformacionDeMascota.this,
                        getPackageName() + ".provider",
                        file);

                handler.post(() -> {
                    progressDialog.dismiss();

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/png");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        startActivity(Intent.createChooser(shareIntent, "Compartir ficha de mascota"));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(InformacionDeMascota.this,
                                "No hay aplicaciones disponibles para compartir",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(InformacionDeMascota.this,
                            "Error al compartir: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("CompartirFicha", "Error", e);
                });
            }
        });
    }

    private void guardarFicha(Bitmap fichaBitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || tienePermisosAlmacenamiento()) {
            guardarFichaEnDescargas(fichaBitmap);
        } else {
            solicitarPermisosAlmacenamiento();
        }
    }

    private void guardarFichaEnDescargas(Bitmap fichaBitmap) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Guardando ficha...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                // Nombre del archivo
                String fileName = "Carnet_" + mascotaNombre.replaceAll("[^a-zA-Z0-9]", "_") +
                        "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".png";

                File file;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ContentResolver resolver = getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/PetCare");

                    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

                    if (imageUri == null) {
                        throw new IOException("No se pudo crear el archivo en Documents/PetCare");
                    }

                    try (OutputStream out = resolver.openOutputStream(imageUri)) {
                        if (!fichaBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                            throw new IOException("No se pudo guardar el bitmap");
                        }
                    }

                    file = new File(getPathFromUri(imageUri));
                } else {
                    File documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    File petcareDir = new File(documentsDir, "PetCare");

                    if (!petcareDir.exists()) {
                        if (!petcareDir.mkdirs()) {
                            throw new IOException("No se pudo crear el directorio PetCare en Documents");
                        }
                    }

                    file = new File(petcareDir, fileName);

                    try (FileOutputStream out = new FileOutputStream(file)) {
                        if (!fichaBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                            throw new IOException("No se pudo guardar el bitmap");
                        }
                        out.flush();
                    }

                    // Notificar al sistema del nuevo archivo
                    MediaScannerConnection.scanFile(this,
                            new String[]{file.getAbsolutePath()},
                            new String[]{"image/png"},
                            null);
                }

                handler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Ficha guardada", Toast.LENGTH_LONG).show();
                    abrirArchivoGuardado(file);
                });
            } catch (Exception e) {
                handler.post(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Error al guardar en descargas, se intentara en cache ", Toast.LENGTH_LONG).show();
                    Log.e("GuardarFicha", "Error", e);

                    // Intentar guardar en caché como último recurso
                    guardarEnCacheComoUltimoRecurso(fichaBitmap);
                });
            }
        });
    }

    @SuppressLint("Range")
    private String getPathFromUri(Uri uri) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path != null ? path : uri.getPath();
    }

    private void guardarEnCacheComoUltimoRecurso(Bitmap fichaBitmap) {
        try {
            File cacheDir = getExternalCacheDir();
            String fileName = "Carnet_" + mascotaNombre.replaceAll("[^a-zA-Z0-9]", "_") +
                    "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".png";

            File file = new File(cacheDir, fileName);

            try (FileOutputStream out = new FileOutputStream(file)) {
                fichaBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
            }

            Toast.makeText(this, "Ficha guardada en caché", Toast.LENGTH_LONG).show();
            abrirArchivoGuardado(file);
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar incluso en caché: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("GuardarCache", "Error", e);
        }
    }

    private void abrirArchivoGuardado(File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            intent.setDataAndType(uri, "image/png");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No hay aplicación para ver la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean tienePermisosAlmacenamiento() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
                txtEdad.setText("Edad: "+mascotaEdad);
                txtSexo.setText("Sexo: "+mascotaSexo);
                txtPeso.setText("Peso: "+mascotaPeso);
                txtEsterilizado.setText("Esterilizado: "+mascotaEsterilizado);
                txtFechaNacimiento.setText(mascotaNacimiento);

                cargarImagenMascota(mascota.optString("foto_mascota", ""));

                // Generar QR para redirección
                String qrContent = "petcare://veterinario/mascota/" + mascotaId;
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
            mascotaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.foto_perfil_mascota);
            return;
        }

        if (fotoMascota.startsWith("images/")) {
            new CargarImagenTask(imgMascota).execute(getString(R.string.url_servidor) + "/miapp/" + fotoMascota);
        } else {
            try {
                byte[] decodedString = Base64.decode(fotoMascota, Base64.DEFAULT);
                mascotaBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                imgMascota.setImageBitmap(mascotaBitmap);
            } catch (Exception e) {
                imgMascota.setImageResource(R.drawable.foto_perfil_mascota);
                mascotaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.foto_perfil_mascota);
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