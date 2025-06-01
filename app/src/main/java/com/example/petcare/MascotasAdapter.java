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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MascotasAdapter extends RecyclerView.Adapter<MascotasAdapter.MascotasViewHolder> {
    private List<Mascotas> mascotas;
    private Context context;

    public MascotasAdapter(List<Mascotas> mascotas, Context context) {
        this.mascotas = mascotas;
        this.context = context;
    }

    @NonNull
    @Override
    public MascotasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota, parent, false);
        return new MascotasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotasViewHolder holder, int position) {
        Mascotas mascota = mascotas.get(position);
        holder.txtNombreMascota.setText(mascota.getNombreMascota());

        if(mascota.getRazaMascota() == null || mascota.getRazaMascota().isEmpty()) {
            holder.txtEspecieRazaMascota.setText(mascota.getEspecieMascota());
        } else {
            holder.txtEspecieRazaMascota.setText(mascota.getEspecieMascota() + " - " + mascota.getRazaMascota());
        }
        holder.txtEdadSexoMascota.setText(mascota.getEdadMascota() + " años - " + mascota.getSexoMascota());

        // Cargar la imagen de la mascota
        if (mascota.getFotoMascota() != null && !mascota.getFotoMascota().isEmpty()) {
            if (mascota.getFotoMascota().startsWith("images/")) {
                // Es una URL, cargamos desde el servidor
                String urlCompleta = context.getString(R.string.url_servidor) + "/miapp/" + mascota.getFotoMascota();
                Toast.makeText(context, urlCompleta, Toast.LENGTH_LONG).show();
                new CargarImagenMascotaTask(holder.imgMascota).execute(urlCompleta);
            } else {
                // Es base64 (para compatibilidad con versiones anteriores)
                try {
                    byte[] decodedString = Base64.decode(mascota.getFotoMascota(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    holder.imgMascota.setImageBitmap(decodedByte);
                } catch (Exception e) {
                    e.printStackTrace();
                    mostrarImagenPredeterminada(holder.imgMascota);
                }
            }
        } else {
            mostrarImagenPredeterminada(holder.imgMascota);
        }

        holder.btnEditarMascota.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditarMascota.class);
            intent.putExtra("id_mascota", mascota.getIdMascota());
            context.startActivity(intent);
        });
    }

    private void mostrarImagenPredeterminada(ImageView imageView) {
        imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.foto_perfil_mascota));
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    public static class MascotasViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNombreMascota, txtEspecieRazaMascota, txtEdadSexoMascota;
        public Button btnEditarMascota;
        public ImageView imgMascota;

        public MascotasViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtNombreMascota = itemView.findViewById(R.id.textViewNombreMascota);
            this.txtEspecieRazaMascota = itemView.findViewById(R.id.textViewEspecieRazaMascota);
            this.txtEdadSexoMascota = itemView.findViewById(R.id.textViewEdadSexoMascota);
            this.btnEditarMascota = itemView.findViewById(R.id.buttonEditarMascota);
            this.imgMascota = itemView.findViewById(R.id.imageViewMascotaInicio);
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