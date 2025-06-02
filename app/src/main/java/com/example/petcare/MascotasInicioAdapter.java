package com.example.petcare;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

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

        // Configurar click listeners para los botones
        holder.cardAgendarCita.setOnClickListener(v -> {
            //Intent intent = new Intent(context, AgendarCitaActivity.class);
            //intent.putExtra("id_mascota", mascota.getIdMascota());
            //context.startActivity(intent);
        });

        holder.cardHistorial.setOnClickListener(v -> {
            //Intent intent = new Intent(context, HistorialMedicoActivity.class);
            //intent.putExtra("id_mascota", mascota.getIdMascota());
            //context.startActivity(intent);
        });

        holder.cardInformacion.setOnClickListener(v -> {
            Intent intent = new Intent(context, InformacionDeMascota.class);
            intent.putExtra("id_mascota", mascota.getIdMascota());
            context.startActivity(intent);
        });

        holder.cardQR.setOnClickListener(v -> {
            //Intent intent = new Intent(context, QRCodeActivity.class);
            //intent.putExtra("id_mascota", mascota.getIdMascota());
            //context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    public static class MascotaInicioViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombreMascota;
        ImageView imageViewMascota;
        CardView cardAgendarCita, cardHistorial, cardInformacion, cardQR;

        public MascotaInicioViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombreMascota = itemView.findViewById(R.id.textViewNombreMascotaInicio);
            imageViewMascota = itemView.findViewById(R.id.imageViewMascotaInicio);
            cardAgendarCita = itemView.findViewById(R.id.cardAgendarCita);
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