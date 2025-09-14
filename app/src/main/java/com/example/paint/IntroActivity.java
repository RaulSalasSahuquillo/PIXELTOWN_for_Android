package com.example.paint;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    private VideoView videoView;
    private boolean finishedOrSkipped = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Oculta barras para modo inmersivo (fullscreen)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        setContentView(R.layout.activity_intro);

        videoView = findViewById(R.id.videoView);

        // URI al recurso en /res/raw/intro.mp4
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.intro);
        videoView.setVideoURI(videoUri);

        // Al terminar el vídeo -> lanzar juego
        videoView.setOnCompletionListener(mp -> goToGame());

        // Si hay error, saltamos a juego para no bloquear
        videoView.setOnErrorListener((mp, what, extra) -> {
            goToGame();
            return true;
        });

        // Permitir saltar la intro tocando la pantalla
        videoView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                goToGame();
                return true;
            }
            return false;
        });

        videoView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reforzar modo inmersivo al volver
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        if (videoView != null && !videoView.isPlaying() && !finishedOrSkipped) {
            videoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    private void goToGame() {
        if (finishedOrSkipped) return;
        finishedOrSkipped = true;
        // Lanza tu actividad del juego (cámbiala por la tuya)
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish(); // no volver a la intro al pulsar atrás
    }
}
