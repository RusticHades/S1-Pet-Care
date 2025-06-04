package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicamentosAdapter extends RecyclerView.Adapter<MedicamentosAdapter.MedicamentoViewHolder> {
    private final List<Medicamento> medicamentosList;
    private final OnMedicamentoClickListener listener;

    public interface OnMedicamentoClickListener {
        void onEliminarClick(int position);
    }

    public MedicamentosAdapter(List<Medicamento> medicamentosList, OnMedicamentoClickListener listener) {
        this.medicamentosList = medicamentosList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento medicamento = medicamentosList.get(position);
        holder.textNombre.setText(medicamento.getNombre());
        holder.textDosis.setText("Dosis: " + medicamento.getDosis());
        holder.textFrecuencia.setText("Frecuencia: " + medicamento.getFrecuencia());
        holder.textMotivo.setText("Motivo: " +
                (medicamento.getMotivo().isEmpty() ? "No especificado" : medicamento.getMotivo()));
        holder.textFechaInicio.setText("Inicio: " + medicamento.getFechaInicio());
        holder.textFechaFin.setText("Fin: " +
                (medicamento.getFechaFin().isEmpty() ? "No especificado" : medicamento.getFechaFin()));
        holder.textNotas.setText(medicamento.getNotas());

        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarClick(position));
    }

    @Override
    public int getItemCount() {
        return medicamentosList.size();
    }

    static class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView textNombre, textDosis, textFrecuencia, textMotivo, textFechaInicio, textFechaFin, textNotas;
        ImageButton btnEliminar;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            textNombre = itemView.findViewById(R.id.textNombre);
            textDosis = itemView.findViewById(R.id.textDosis);
            textFrecuencia = itemView.findViewById(R.id.textFrecuencia);
            textMotivo = itemView.findViewById(R.id.textMotivo);
            textFechaInicio = itemView.findViewById(R.id.textFechaInicio);
            textFechaFin = itemView.findViewById(R.id.textFechaFin);
            textNotas = itemView.findViewById(R.id.textNotas);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }
    }
}