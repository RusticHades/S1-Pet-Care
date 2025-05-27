package com.example.petcare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MascotasAdapter extends RecyclerView.Adapter<MascotasAdapter.MascotasViewHolder> {
    private List<Mascotas> mascotas;


    public MascotasAdapter(List<Mascotas> mascotas) {
        this.mascotas = mascotas;
    }

    @NonNull
    @Override
    public MascotasAdapter.MascotasViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mascota,parent,false);

        return new MascotasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotasViewHolder holder, int position) {
        Mascotas mascota = mascotas.get(position);
        holder.txtNombreMascota.setText(mascota.getNombreMascota());

        if(mascota.getRazaMascota() == null || (mascota.getRazaMascota()).equals("")) {
            holder.txtEspecieRazaMascota.setText(mascota.getEspecieMascota());
        } else {
            holder.txtEspecieRazaMascota.setText(mascota.getEspecieMascota() + " - " + mascota.getRazaMascota());
        }
        holder.txtEdadSexoMascota.setText(mascota.getEdadMascota() + " años - " + mascota.getSexoMascota() );
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    public static class MascotasViewHolder extends RecyclerView.ViewHolder{
        public TextView txtNombreMascota, txtEspecieRazaMascota, txtEdadSexoMascota;

        public MascotasViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtNombreMascota = itemView.findViewById(R.id.textViewNombreMascota);
            this.txtEspecieRazaMascota = itemView.findViewById(R.id.textViewEspecieRazaMascota);
            this.txtEdadSexoMascota = itemView.findViewById(R.id.textViewEdadSexoMascota);
        }
    }
}
