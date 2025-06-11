package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {
    private List<Citas> citasList;
    private OnCitaClickListener listener;
    private String inicioVeterinario;

    public interface OnCitaClickListener {
        void onCitaClick(int position);
        void onDeleteClick(int position);
    }

    public CitasAdapter(List<Citas> citasList, OnCitaClickListener listener, String inicioVeterinario) {
        this.citasList = citasList;
        this.listener = listener;
        this.inicioVeterinario = inicioVeterinario;
    }

    @NonNull
    @Override
    public CitaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cita, parent, false);
        return new CitaViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CitaViewHolder holder, int position) {
        Citas cita = citasList.get(position);
        holder.tvFechaHora.setText(cita.getFechaHora());
        holder.tvMotivo.setText(cita.getMotivo());

        // Mostrar notas si existen, sino mostrar "Sin notas adicionales"
        String notas = cita.getNotas();
        if(notas == null || notas.isEmpty()) {
            holder.tvNotas.setText("Sin notas adicionales");
        } else {
            holder.tvNotas.setText(notas);
        }

        if ("inicio".equals(inicioVeterinario)) {
            holder.btnEliminar.setVisibility(View.GONE);
        } else {
            holder.btnEliminar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return citasList.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFechaHora, tvMotivo, tvNotas;
        ImageButton btnEliminar;

        public CitaViewHolder(@NonNull View itemView, OnCitaClickListener listener) {
            super(itemView);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvMotivo = itemView.findViewById(R.id.tvMotivo);
            tvNotas = itemView.findViewById(R.id.tvNotas);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCitaClick(position);
                    }
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}