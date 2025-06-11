package com.example.petcare;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MascotasInicioAdapter extends RecyclerView.Adapter<MascotasInicioAdapter.MascotaInicioViewHolder> {
    private List<Mascotas> mascotas;
    private Context context;

    public MascotasInicioAdapter(List<Mascotas> mascotas, Context context) {
        this.mascotas = mascotas;
        this.context = context;
    }

    @NonNull
    @Override
    public MascotaInicioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota_inicio, parent, false);
        return new MascotaInicioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaInicioViewHolder holder, int position) {
        Mascotas mascota = mascotas.get(position);

        // Configurar nombre de la mascota
        holder.textViewNombreMascota.setText(mascota.getNombreMascota());

        // Cargar imagen de la mascota
        String fotoMascota = mascota.getFotoMascota();
        if (fotoMascota == null || fotoMascota.isEmpty() || fotoMascota.equals("null")) {
            holder.imageViewMascota.setImageResource(R.drawable.foto_perfil_mascota);
        } else if (fotoMascota.startsWith("http")) {
            // Cargar desde URL
            new CargarImagenMascotaTask(holder.imageViewMascota).execute(fotoMascota);
        } else if (fotoMascota.startsWith("images/")) {
            // Cargar desde ruta relativa del servidor
            String urlCompleta = context.getString(R.string.url_servidor) + "/miapp/" + fotoMascota;
            new CargarImagenMascotaTask(holder.imageViewMascota).execute(urlCompleta);
        } else {
            // Intentar cargar como base64
            try {
                byte[] decodedString = Base64.decode(fotoMascota, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.imageViewMascota.setImageBitmap(decodedByte);
            } catch (Exception e) {
                e.printStackTrace();
                holder.imageViewMascota.setImageResource(R.drawable.foto_perfil_mascota);
            }
        }

        Bitmap qrCode = generarQRCodeVerde("petcare://veterinario/mascota/" + String.valueOf(mascota.getIdMascota()), 250);

        ImageView qrImageView = holder.itemView.findViewById(R.id.imageViewQrMascotaInicio);
        if (qrCode != null) {
            qrImageView.setImageBitmap(qrCode);
        } else {
            qrImageView.setImageResource(R.drawable.imagen_qr_generico);
        }

        // Configurar click listeners para los botones
        holder.cardCitas.setOnClickListener(v -> {
            Intent intent = new Intent(context, AgendarCita.class);
            intent.putExtra("id_mascota", mascota.getIdMascota());
            intent.putExtra("id_veterinario", mascota.getIdUsuario());
            intent.putExtra("inicio_o_veterinario", "inicio");
            context.startActivity(intent);
        });

        holder.cardHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(context, MostrarHistorialMedico.class);
            intent.putExtra("id_mascota", mascota.getIdMascota());
            context.startActivity(intent);
        });

        holder.cardInformacion.setOnClickListener(v -> {
            Intent intent = new Intent(context, InformacionDeMascota.class);
            intent.putExtra("id_mascota", mascota.getIdMascota());
            context.startActivity(intent);
        });

        holder.cardQR.setOnClickListener(v -> {
            // Generar el código QR con el ID de la mascota

            // Crear un diálogo personalizado
            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            // Inflar el layout personalizado
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_qr_mascota, null);

            // Usar una variable diferente para el ImageView del diálogo
            ImageView dialogQrImageView = dialogView.findViewById(R.id.qrImageView);
            TextView titleTextView = dialogView.findViewById(R.id.titleTextView);

            // Configurar la imagen QR en el ImageView del diálogo
            Bitmap qrCodeAux = generarQRCode("petcare://veterinario/mascota/" + String.valueOf(mascota.getIdMascota()), 250);
            if (qrCodeAux != null) {
                dialogQrImageView.setImageBitmap(qrCodeAux);
            } else {
                dialogQrImageView.setImageResource(R.drawable.imagen_qr_generico);
            }

            titleTextView.setText("Código QR de " + mascota.getNombreMascota());


            builder.setView(dialogView);
            builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    private Bitmap generarQRCode(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2); // Aumentamos el margen

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();
            Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            // Opcional: Añadir un logo en el centro
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_escanear_codigo);
            if (logo != null) {
                Bitmap combined = Bitmap.createBitmap(width, width, bitmap.getConfig());
                Canvas canvas = new Canvas(combined);
                canvas.drawBitmap(bitmap, 0, 0, null);

                // Escalar el logo
                int logoSize = width / 5;
                logo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, false);

                // Posicionar el logo en el centro
                int left = (width - logo.getWidth()) / 2;
                int top = (width - logo.getHeight()) / 2;
                canvas.drawBitmap(logo, left, top, null);

                return combined;
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap generarQRCodeVerde(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2); // Aumentamos el margen

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            int width = bitMatrix.getWidth();
            Bitmap bitmap = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);

            // Define el color verde (tu color principal)
            int greenColor = Color.parseColor("#76C46F");

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? greenColor : Color.WHITE);
                }
            }

            // Opcional: Añadir un logo en el centro
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_escanear_codigo);
            if (logo != null) {
                Bitmap combined = Bitmap.createBitmap(width, width, bitmap.getConfig());
                Canvas canvas = new Canvas(combined);
                canvas.drawBitmap(bitmap, 0, 0, null);

                // Escalar el logo
                int logoSize = width / 5;
                logo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, false);

                // Posicionar el logo en el centro
                int left = (width - logo.getWidth()) / 2;
                int top = (width - logo.getHeight()) / 2;
                canvas.drawBitmap(logo, left, top, null);

                return combined;
            }

            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    public static class MascotaInicioViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombreMascota;
        ImageView imageViewMascota;
        CardView cardCitas, cardHistorial, cardInformacion, cardQR;

        public MascotaInicioViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombreMascota = itemView.findViewById(R.id.textViewNombreMascotaInicio);
            imageViewMascota = itemView.findViewById(R.id.imageViewMascotaInicio);
            cardCitas = itemView.findViewById(R.id.cardCita);
            cardHistorial = itemView.findViewById(R.id.cardHistorial);
            cardInformacion = itemView.findViewById(R.id.cardInformacion);
            cardQR = itemView.findViewById(R.id.cardQR);
        }
    }

    private static class CargarImagenMascotaTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public CargarImagenMascotaTask(ImageView imageView) {
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
                e.printStackTrace();
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