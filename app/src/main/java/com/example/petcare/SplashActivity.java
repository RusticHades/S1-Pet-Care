package com.example.petcare;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView imgLogoSplash, dogWalking;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Configuración de la barra de estado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        imgLogoSplash = findViewById(R.id.imageViewLogoSplash);
        dogWalking = findViewById(R.id.dogWalking);
        progressBar = findViewById(R.id.progressBarSplash);

        // Posicionar inicialmente el perro
        dogWalking.post(() -> {
            dogWalking.setX(progressBar.getX() - dogWalking.getWidth()/2);
            dogWalking.setVisibility(View.INVISIBLE);
        });

        // Animación de entrada del logo
        imgLogoSplash.setAlpha(0f);
        imgLogoSplash.setScaleX(0.8f);
        imgLogoSplash.setScaleY(0.8f);

        imgLogoSplash.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {
                    dogWalking.setVisibility(View.VISIBLE);
                    startDogIdleAnimation();
                    startProgressBar();
                })
                .start();
    }

    private void startDogIdleAnimation() {
        // Pequeña animación de respiración cuando está quieto
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(dogWalking, "scaleX", 1f, 1.05f, 1f);
        scaleX.setDuration(1000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(dogWalking, "scaleY", 1f, 1.05f, 1f);
        scaleY.setDuration(1000);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);

        AnimatorSet idleSet = new AnimatorSet();
        idleSet.playTogether(scaleX, scaleY);
        idleSet.start();
    }

    private void startProgressBar() {
        if (isRunning) return;
        isRunning = true;

        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;
                handler.post(() -> {
                    progressBar.setProgress(progressStatus);

                    // Calcular posición X del perro
                    float progressFraction = progressStatus / 100f;
                    float maxMovement = progressBar.getWidth() - dogWalking.getWidth();
                    float newX = progressBar.getX() + (progressFraction * maxMovement);

                    // Mover el perro con animación dinámica
                    dogWalking.animate()
                            .x(newX)
                            .setDuration(50)
                            .start();

                    // Animaciones adicionales cada cierto progreso
                    if (progressStatus % 20 == 0) {
                        makeDogJump();
                    }
                });

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // Transición final
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }).start();
    }

    private void makeDogJump() {
        // Animación de salto corto
        ObjectAnimator jumpAnim = ObjectAnimator.ofFloat(dogWalking, "translationY", 0f, -40f, 0f);
        jumpAnim.setDuration(300);
        jumpAnim.setInterpolator(new BounceInterpolator());

        // Animación de rotación (como si moviera la cola)
        ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(dogWalking, "rotation", 0f, 15f, -15f, 0f);
        rotateAnim.setDuration(400);

        AnimatorSet jumpSet = new AnimatorSet();
        jumpSet.playTogether(jumpAnim, rotateAnim);
        jumpSet.start();
    }
}