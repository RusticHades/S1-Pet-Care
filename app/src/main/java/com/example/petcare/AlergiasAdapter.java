package com.example.petcare;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlergiasAdapter extends RecyclerView.Adapter<AlergiasAdapter.AlergiaViewHolder> {
    private final List<Alergia> alergiasList;
    private final OnAlergiaClickListener listener;

    public interface OnAlergiaClickListener {
        void onEliminarClick(int position);
    }

    public AlergiasAdapter(List<Alergia> alergiasList, OnAlergiaClickListener listener) {
        this.alergiasList = alergiasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlergiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alergia, parent, false);
        return new AlergiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlergiaViewHolder holder, int position) {
        Alergia alergia = alergiasList.get(position);
        holder.textAlergia.setText(alergia.getNombre());
        holder.textSeveridad.setText("Severidad: " + alergia.getSeveridad());
        holder.textNotas.setText(alergia.getNotas());

        // Cambiar color según severidad
        switch (alergia.getSeveridad().toLowerCase()) {
            case "leve":
                holder.severidadIndicator.setBackgroundColor(Color.parseColor("#4CAF50")); // Verde
                break;
            case "moderada":
                holder.severidadIndicator.setBackgroundColor(Color.parseColor("#FFC107")); // Amarillo
                break;
            case "grave":
                holder.severidadIndicator.setBackgroundColor(Color.parseColor("#F44336")); // Rojo
                break;
        }

        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(position));
    }

    @Override
    public int getItemCount() {
        return alergiasList.size();
    }

    static class AlergiaViewHolder extends RecyclerView.ViewHolder {
        TextView textAlergia, textSeveridad, textNotas;
        View severidadIndicator;
        ImageButton btnEliminar;

        public AlergiaViewHolder(@NonNull View itemView) {
            super(itemView);
            textAlergia = itemView.findViewById(R.id.textAlergia);
            textSeveridad = itemView.findViewById(R.id.textSeveridad);
            textNotas = itemView.findViewById(R.id.textNotas);
            severidadIndicator = itemView.findViewById(R.id.severidadIndicator);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}