package com.example.petcare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

// Actividad que muestra el detalle completo de un registro del historial médico
public class DetalleHistorial extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_historial);

        // 1. OBTENCIÓN DE DATOS DEL INTENT -------------------------------
        Intent intent = getIntent();

        // Recuperar todos los datos del intent con sus valores por defecto
        int idHistorial = intent.getIntExtra("id_historial", 0); // ID interno del historial
        String fechaConsulta = intent.getStringExtra("fecha_consulta"); // Fecha de la consulta
        String motivoConsulta = intent.getStringExtra("motivo_consulta"); // Razón de la consulta
        String diagnostico = intent.getStringExtra("diagnostico"); // Diagnóstico médico
        String tratamiento = intent.getStringExtra("tratamiento"); // Tratamiento indicado
        String observaciones = intent.getStringExtra("observaciones"); // Notas adicionales
        double pesoActual = intent.getDoubleExtra("peso_actual", 0.0); // Peso en kg
        double temperatura = intent.getDoubleExtra("temperatura", 0.0); // Temperatura en °C
        String vacunasAplicadas = intent.getStringExtra("vacunas_aplicadas"); // Vacunas aplicadas

        // Verificar si se recibieron valores específicos
        boolean hasPeso = intent.hasExtra("peso_actual");
        boolean hasTemperatura = intent.hasExtra("temperatura");

        // 2. CONFIGURACIÓN DE VISTAS -------------------------------------
        // Obtener referencias a todos los TextView del layout
        TextView textViewFecha = findViewById(R.id.textViewFechaDetalle);
        TextView textViewMotivo = findViewById(R.id.textViewMotivoDetalle);
        TextView textViewDiagnostico = findViewById(R.id.textViewDiagnosticoDetalle);
        TextView textViewTratamiento = findViewById(R.id.textViewTratamientoDetalle);
        TextView textViewObservaciones = findViewById(R.id.textViewObservacionesDetalle);
        TextView textViewPeso = findViewById(R.id.textViewPesoDetalle);
        TextView textViewTemperatura = findViewById(R.id.textViewTemperaturaDetalle);
        TextView textViewVacunas = findViewById(R.id.textViewVacunasDetalle);
        MaterialButton buttonVolver = findViewById(R.id.buttonVolverDetalle);

        // 3. ASIGNACIÓN DE VALORES A LAS VISTAS --------------------------
        // Fecha de consulta (siempre debería tener valor)
        textViewFecha.setText("Fecha: " + fechaConsulta);

        // Motivo de consulta (campo obligatorio)
        textViewMotivo.setText("Motivo: " + motivoConsulta);

        // Diagnóstico (campo obligatorio)
        textViewDiagnostico.setText("Diagnóstico: " + diagnostico);

        // Tratamiento (campo opcional)
        if (tratamiento != null && !tratamiento.isEmpty()) {
            textViewTratamiento.setText("Tratamiento: " + tratamiento);
        } else {
            textViewTratamiento.setText("Tratamiento: No se registró tratamiento");
        }

        // Observaciones (campo opcional)
        if (observaciones != null && !observaciones.isEmpty()) {
            textViewObservaciones.setText("Observaciones: " + observaciones);
        } else {
            textViewObservaciones.setText("Observaciones: No hay observaciones registradas");
        }

        // Peso (campo opcional)
        if (hasPeso) {
            textViewPeso.setText("Peso: " + String.format("%.1f kg", pesoActual));
        } else {
            textViewPeso.setText("Peso: No registrado");
        }

        // Temperatura (campo opcional)
        if (hasTemperatura) {
            textViewTemperatura.setText("Temperatura: " + String.format("%.1f°C", temperatura));
        } else {
            textViewTemperatura.setText("Temperatura: No registrada");
        }

        // Vacunas (campo opcional)
        if (vacunasAplicadas != null && !vacunasAplicadas.isEmpty()) {
            textViewVacunas.setText("Vacunas aplicadas: " + vacunasAplicadas);
        } else {
            textViewVacunas.setText("Vacunas aplicadas: No se aplicaron vacunas");
        }

        // 4. CONFIGURACIÓN DE BOTONES ------------------------------------
        // Botón para volver a la actividad anterior
        buttonVolver.setOnClickListener(v -> finish());
    }
}