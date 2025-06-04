package com.example.petcare;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetalleHistorialCompleto extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_historial_completo);

        TextView tvNombreMascota = findViewById(R.id.tvNombreMascota);
        TextView tvFechaConsulta = findViewById(R.id.tvFechaConsulta);
        TextView tvMotivoConsulta = findViewById(R.id.tvMotivoConsulta);
        TextView tvDiagnostico = findViewById(R.id.tvDiagnostico);
        TextView tvTratamiento = findViewById(R.id.tvTratamiento);
        TextView tvObservaciones = findViewById(R.id.tvObservaciones);
        TextView tvPesoActual = findViewById(R.id.tvPesoActual);
        TextView tvTemperatura = findViewById(R.id.tvTemperatura);
        TextView tvVacunas = findViewById(R.id.tvVacunas);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvNombreMascota.setText(extras.getString("nombre_mascota", ""));
            tvFechaConsulta.setText(formatFecha(extras.getString("fecha_consulta", "")));
            tvMotivoConsulta.setText(extras.getString("motivo_consulta", "No especificado"));
            tvDiagnostico.setText(extras.getString("diagnostico", "No especificado"));

            String tratamiento = extras.getString("tratamiento", "");
            tvTratamiento.setText(tratamiento.isEmpty() ? "No se especificó tratamiento" : tratamiento);

            String observaciones = extras.getString("observaciones", "");
            tvObservaciones.setText(observaciones.isEmpty() ? "No hay observaciones" : observaciones);

            Double peso = extras.getDouble("peso_actual", 0);
            tvPesoActual.setText(peso > 0 ? String.format(Locale.getDefault(), "%.2f kg", peso) : "No registrado");

            Double temp = extras.getDouble("temperatura", 0);
            tvTemperatura.setText(temp > 0 ? String.format(Locale.getDefault(), "%.2f °C", temp) : "No registrada");

            String vacunas = extras.getString("vacunas_aplicadas", "");
            tvVacunas.setText(vacunas.isEmpty() ? "No se aplicaron vacunas" : vacunas);
        }
    }

    private String formatFecha(String fechaOriginal) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(fechaOriginal);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return fechaOriginal;
        }
    }
}