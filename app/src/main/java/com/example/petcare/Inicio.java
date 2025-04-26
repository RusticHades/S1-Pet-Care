package com.example.petcare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Inicio extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Esto establece el layout base

        // Inflar el contenido especifico
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_inicio, contentFrame, true);

        navigationView.setCheckedItem(R.id.navMenuInicio);
        View menuItemView = navigationView.findViewById(R.id.navMenuInicio);
        if (menuItemView != null) {
            menuItemView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSecundario));
        }
        EdgeToEdge.enable(this);
    }
}