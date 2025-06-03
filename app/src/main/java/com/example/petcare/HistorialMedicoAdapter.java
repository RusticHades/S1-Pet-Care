package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistorialMedicoAdapter extends RecyclerView.Adapter<HistorialMedicoAdapter.HistorialViewHolder> {

    private List<HistorialMedico> historiales;

    public HistorialMedicoAdapter(List<HistorialMedico> historiales) {
        this.historiales = historiales;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial_medico, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        HistorialMedico historial = historiales.get(position);
        holder.fechaConsulta.setText(historial.getFechaConsulta());
        holder.diagnostico.setText(historial.getDiagnostico());
        holder.pesoActual.setText(String.format("%.2f kg", historial.getPesoActual()));
    }

    @Override
    public int getItemCount() {
        return historiales.size();
    }

    public void actualizarDatos(List<HistorialMedico> nuevosHistoriales) {
        historiales = nuevosHistoriales;
        notifyDataSetChanged();
    }

    static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView fechaConsulta, diagnostico, pesoActual;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            fechaConsulta = itemView.findViewById(R.id.textViewFechaConsulta);
            diagnostico = itemView.findViewById(R.id.textViewDiagnostico);
            pesoActual = itemView.findViewById(R.id.textViewPesoActual);
        }
    }
}