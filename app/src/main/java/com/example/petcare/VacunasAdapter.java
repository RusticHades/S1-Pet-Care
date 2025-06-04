package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VacunasAdapter extends RecyclerView.Adapter<VacunasAdapter.VacunaViewHolder> {
    private final List<Vacuna> vacunasList;
    private final OnVacunaClickListener listener;

    public interface OnVacunaClickListener {
        void onEliminarClick(int position);
    }

    public VacunasAdapter(List<Vacuna> vacunasList, OnVacunaClickListener listener) {
        this.vacunasList = vacunasList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VacunaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vacuna, parent, false);
        return new VacunaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacunaViewHolder holder, int position) {
        Vacuna vacuna = vacunasList.get(position);
        holder.textNombre.setText(vacuna.getNombre());
        holder.textFechaAplicacion.setText("Aplicada: " + vacuna.getFechaAplicacion());
        holder.textFechaRefuerzo.setText("Próximo refuerzo: " +
                (vacuna.getFechaRefuerzo().isEmpty() ? "No especificado" : vacuna.getFechaRefuerzo()));
        holder.textVeterinario.setText("Veterinario: " +
                (vacuna.getVeterinario().isEmpty() ? "No especificado" : vacuna.getVeterinario()));
        holder.textNotas.setText(vacuna.getNotas());

        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(position));
    }

    @Override
    public int getItemCount() {
        return vacunasList.size();
    }

    static class VacunaViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textFechaAplicacion, textFechaRefuerzo, textVeterinario, textNotas;
        ImageButton btnEliminar;

        public VacunaViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            textFechaAplicacion = itemView.findViewById(R.id.textFechaAplicacion);
            textFechaRefuerzo = itemView.findViewById(R.id.textFechaRefuerzo);
            textVeterinario = itemView.findViewById(R.id.textVeterinario);
            textNotas = itemView.findViewById(R.id.textNotas);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}