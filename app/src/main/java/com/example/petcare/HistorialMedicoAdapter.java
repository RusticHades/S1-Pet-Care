package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// Adaptador para el RecyclerView que muestra el historial médico de mascotas
public class HistorialMedicoAdapter extends RecyclerView.Adapter<HistorialMedicoAdapter.HistorialViewHolder> {

    // Lista que contiene los datos del historial médico
    private List<HistorialMedicoMascota> historialList;
    // Listener para manejar clics en los items
    private OnItemClickListener listener;

    // Interfaz para comunicar los clics a la actividad/fragmento
    public interface OnItemClickListener {
        void onItemClick(HistorialMedicoMascota historial);
    }

    // Constructor del adaptador
    public HistorialMedicoAdapter(List<HistorialMedicoMascota> historialList, OnItemClickListener listener) {
        this.historialList = historialList;
        this.listener = listener;
    }

    // Crea nuevos ViewHolders (invocado por el layout manager)
    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout de cada item del historial
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historial_medico, parent, false);
        return new HistorialViewHolder(view);
    }

    // Reemplaza el contenido de una vista (invocado por el layout manager)
    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        // Obtiene el elemento de la lista en la posición actual
        HistorialMedicoMascota historial = historialList.get(position);

        // Asigna los valores a las vistas:
        // Fecha de la consulta
        holder.textViewFecha.setText("Fecha: " + historial.getFechaConsulta());
        // Motivo de la consulta
        holder.textViewMotivo.setText("Motivo: " + historial.getMotivoConsulta());
        // Diagnóstico realizado
        holder.textViewDiagnostico.setText("Diagnóstico: " + historial.getDiagnostico());

        // Muestra tratamiento solo si existe (con etiqueta)
        if (historial.getTratamiento() != null && !historial.getTratamiento().isEmpty()) {
            holder.textViewTratamiento.setText("Tratamiento: " + historial.getTratamiento());
            holder.textViewTratamiento.setVisibility(View.VISIBLE);
        } else {
            holder.textViewTratamiento.setVisibility(View.GONE);
        }

        // Muestra peso si existe (con etiqueta)
        if (historial.getPesoActual() != null) {
            holder.textViewPeso.setText("Peso: " + String.format("%.1f kg", historial.getPesoActual()));
            holder.textViewPeso.setVisibility(View.VISIBLE);
        } else {
            holder.textViewPeso.setVisibility(View.GONE);
        }

        // Muestra temperatura si existe (con etiqueta)
        if (historial.getTemperatura() != null) {
            holder.textViewTemperatura.setText("Temp: " + String.format("%.1f°C", historial.getTemperatura()));
            holder.textViewTemperatura.setVisibility(View.VISIBLE);
        } else {
            holder.textViewTemperatura.setVisibility(View.GONE);
        }

        // Muestra u oculta el layout de detalles según si hay datos
        if (holder.textViewPeso.getVisibility() == View.VISIBLE ||
                holder.textViewTemperatura.getVisibility() == View.VISIBLE) {
            holder.layoutDetalles.setVisibility(View.VISIBLE);
        } else {
            holder.layoutDetalles.setVisibility(View.GONE);
        }

        // Configura el clic en el botón "Ver Detalles"
        holder.buttonVerDetalles.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(historial);
            }
        });
    }

    // Devuelve el tamaño de la lista de datos
    @Override
    public int getItemCount() {
        return historialList.size();
    }

    // ViewHolder que describe una vista de item y metadatos sobre su lugar dentro del RecyclerView
    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        // Views que componen cada item del historial:
        TextView textViewFecha;       // Muestra "Fecha: [fecha]"
        TextView textViewMotivo;      // Muestra "Motivo: [motivo]"
        TextView textViewDiagnostico;  // Muestra "Diagnóstico: [diagnóstico]"
        TextView textViewTratamiento;  // Muestra "Tratamiento: [tratamiento]" (opcional)
        TextView textViewPeso;        // Muestra "Peso: [valor] kg" (opcional)
        TextView textViewTemperatura;  // Muestra "Temp: [valor]°C" (opcional)
        View layoutDetalles;          // Layout que contiene peso y temperatura
        Button buttonVerDetalles;     // Botón para ver detalles completos

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            // Vincula las vistas con los elementos del layout
            textViewFecha = itemView.findViewById(R.id.textViewFechaConsulta);
            textViewMotivo = itemView.findViewById(R.id.textViewMotivoConsulta);
            textViewDiagnostico = itemView.findViewById(R.id.textViewDiagnostico);
            textViewTratamiento = itemView.findViewById(R.id.textViewTratamiento);
            textViewPeso = itemView.findViewById(R.id.textViewPeso);
            textViewTemperatura = itemView.findViewById(R.id.textViewTemperatura);
            layoutDetalles = itemView.findViewById(R.id.layoutDetalles);
            buttonVerDetalles = itemView.findViewById(R.id.buttonVerDetalles);
        }
    }
}