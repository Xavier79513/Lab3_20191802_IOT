package com.example.lab3_20191802;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab3_20191802.api.TriviaApiService;
import com.example.lab3_20191802.model.Pregunta;
import com.example.lab3_20191802.model.PreguntaResponse;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreguntasActivity extends AppCompatActivity {

    private TextView textTimer, textPreguntaActual, textPregunta;
    private RadioGroup radioGroupOpciones;
    private Button btnSiguiente;

    private List<Pregunta> listaPreguntas = new ArrayList<>();

    private int tiempoRestanteSegundos;
    private Thread contadorThread;
    private boolean isGameRunning = true;

    private Handler handler = new Handler(Looper.getMainLooper());

    private int preguntaActual = 1;
    private int totalPreguntas = 3;

    private int categoriaId = 9;

    private int respuestasCorrectas = 0;
    private int respuestasIncorrectas = 0;
    private int respuestasNoRespondidas = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preguntas);

        textTimer = findViewById(R.id.textTimer);
        textPreguntaActual = findViewById(R.id.textPreguntaActual);
        textPregunta = findViewById(R.id.textPregunta);
        radioGroupOpciones = findViewById(R.id.radioGroupOpciones);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        int cantidadPreguntas = getIntent().getIntExtra("cantidad", 3);
        String dificultad = getIntent().getStringExtra("dificultad");
        categoriaId = getIntent().getIntExtra("categoriaId", 9); // Recibir categoría si la mandas

        totalPreguntas = cantidadPreguntas;
        tiempoRestanteSegundos = calcularTiempoTotal(cantidadPreguntas, dificultad);

        iniciarContador();
        cargarPreguntasDesdeApi(cantidadPreguntas, categoriaId, dificultad);

        btnSiguiente.setOnClickListener(v -> siguientePregunta());
    }

    private int calcularTiempoTotal(int cantidad, String dificultad) {
        switch (dificultad) {
            case "easy":
                return cantidad * 5;
            case "medium":
                return cantidad * 7;
            case "hard":
                return cantidad * 10;
            default:
                return cantidad * 5;
        }
    }

    private void iniciarContador() {
        contadorThread = new Thread(() -> {
            while (isGameRunning && tiempoRestanteSegundos > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                tiempoRestanteSegundos--;

                handler.post(this::actualizarTimerUI);

                if (tiempoRestanteSegundos <= 0) {
                    isGameRunning = false;
                    handler.post(this::irAEstadisticas);
                }
            }
        });
        contadorThread.start();
    }

    private void actualizarTimerUI() {
        int minutos = tiempoRestanteSegundos / 60;
        int segundos = tiempoRestanteSegundos % 60;
        textTimer.setText(String.format("%02d:%02d", minutos, segundos));
    }

    // En PreguntasActivity.java
    private void mostrarPreguntaActual() {
        runOnUiThread(() -> {
            if (listaPreguntas == null || preguntaActual > listaPreguntas.size()) {
                Toast.makeText(this, "Error al cargar preguntas", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Pregunta pregunta = listaPreguntas.get(preguntaActual - 1);

            // Actualizar UI con la categoría real
            TextView subTitleGame = findViewById(R.id.subTitleGame);
            subTitleGame.setText(pregunta.getCategory());

            textPreguntaActual.setText("Pregunta " + preguntaActual + "/" + totalPreguntas);
            textPregunta.setText(htmlDecode(pregunta.getQuestion()));

            radioGroupOpciones.removeAllViews();
            List<String> opciones = new ArrayList<>(pregunta.getIncorrectAnswers());
            opciones.add(pregunta.getCorrectAnswer());
            Collections.shuffle(opciones);

            for (String opcion : opciones) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setText(htmlDecode(opcion));
                radioButton.setTextSize(16);
                radioButton.setPadding(8, 16, 8, 16);
                radioGroupOpciones.addView(radioButton);
            }
        });
    }

    private String htmlDecode(String text) {
        return android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY).toString();
    }

    private void siguientePregunta() {
        if (radioGroupOpciones.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Selecciona una respuesta antes de continuar.", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton respuestaSeleccionada = findViewById(radioGroupOpciones.getCheckedRadioButtonId());
        String respuestaTexto = respuestaSeleccionada.getText().toString();
        Pregunta pregunta = listaPreguntas.get(preguntaActual - 1);

        if (respuestaTexto.equals(htmlDecode(pregunta.getCorrectAnswer()))) {
            respuestasCorrectas++;
        } else {
            respuestasIncorrectas++;
        }

        if (preguntaActual < totalPreguntas) {
            preguntaActual++;
            mostrarPreguntaActual();
        } else {
            isGameRunning = false;
            irAEstadisticas();
        }
    }

    private void irAEstadisticas() {
        respuestasNoRespondidas = totalPreguntas - (respuestasCorrectas + respuestasIncorrectas);

        Intent intent = new Intent(this, EstadisticasActivity.class);
        intent.putExtra("correctas", respuestasCorrectas);
        intent.putExtra("incorrectas", respuestasIncorrectas);
        intent.putExtra("no_respondidas", respuestasNoRespondidas);
        startActivity(intent);
        finish();
    }
    private void cargarPreguntasDesdeApi(int cantidad, int categoria, String dificultad) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TriviaApiService apiService = retrofit.create(TriviaApiService.class);

        // Agregar logging para debug
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://opentdb.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(TriviaApiService.class);

        Call<PreguntaResponse> call = apiService.obtenerPreguntas(
                cantidad,
                categoria,
                dificultad.toLowerCase(), // Asegurar minúsculas
                "multiple"
        );

        call.enqueue(new Callback<PreguntaResponse>() {
            @Override
            public void onResponse(Call<PreguntaResponse> call, Response<PreguntaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaPreguntas = response.body().getResults();
                    if (listaPreguntas != null && !listaPreguntas.isEmpty()) {
                        mostrarPreguntaActual();
                    } else {
                        Toast.makeText(PreguntasActivity.this,
                                "No se encontraron preguntas para esta configuración",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(PreguntasActivity.this,
                            "Error en la respuesta de la API: " + response.code(),
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<PreguntaResponse> call, Throwable t) {
                Toast.makeText(PreguntasActivity.this,
                        "Error de conexión: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameRunning = false;
    }
}