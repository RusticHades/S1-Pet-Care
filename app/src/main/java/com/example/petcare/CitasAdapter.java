package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CitasAdapter extends RecyclerView.Adapter<CitasAdapter.CitaViewHolder> {
    private List<Citas> citasList;
    private OnCitaClickListener listener;

    public interface OnCitaClickListener {
        void onCitaClick(int position);
    }

    public CitasAdapter(List<Citas> citasList, OnCitaClickListener listener) {
        this.citasList = citasList;
        this.listener = listener;
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
        Citas citas = citasList.get(position);
        holder.tvFechaHora.setText(citas.getFechaHora());
        holder.tvMotivo.setText(citas.getMotivo());
        holder.tvEstado.setText(citas.getEstado());
    }

    @Override
    public int getItemCount() {
        return citasList.size();
    }

    static class CitaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFechaHora, tvMotivo, tvEstado;

        public CitaViewHolder(@NonNull View itemView, OnCitaClickListener listener) {
            super(itemView);
            tvFechaHora = itemView.findViewById(R.id.tvFechaHora);
            tvMotivo = itemView.findViewById(R.id.tvMotivo);
            tvEstado = itemView.findViewById(R.id.tvEstado);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onCitaClick(position);
                    }
                }
            });
        }
    }
}