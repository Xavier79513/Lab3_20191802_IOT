package com.example.lab3_20191802;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class EstadisticasActivity extends AppCompatActivity {

    private TextView textCorrectas, textIncorrectas, textNoRespondidas;
    private Button btnVolverAJugar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        textCorrectas = findViewById(R.id.textCorrectas);
        textIncorrectas = findViewById(R.id.textIncorrectas);
        textNoRespondidas = findViewById(R.id.textNoRespondidas);
        btnVolverAJugar = findViewById(R.id.btnVolverAJugar);

        int correctas = getIntent().getIntExtra("correctas", 0);
        int incorrectas = getIntent().getIntExtra("incorrectas", 0);
        int noRespondidas = getIntent().getIntExtra("no_respondidas", 0);

        textCorrectas.setText("Correctas: " + correctas);
        textIncorrectas.setText("Incorrectas: " + incorrectas);
        textNoRespondidas.setText("No Respondidas: " + noRespondidas);

        btnVolverAJugar.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class); // O donde esté tu menú principal
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}