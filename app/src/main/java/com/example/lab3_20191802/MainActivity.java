package com.example.lab3_20191802;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerCategoria, spinnerDificultad;
    private EditText editCantidad;
    private Button btnComprobarConexion, btnComenzar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular vistas
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        editCantidad = findViewById(R.id.editCantidad);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        btnComprobarConexion = findViewById(R.id.btnComprobarConexion);
        btnComenzar = findViewById(R.id.btnComenzar);

        cargarSpinners();

        btnComprobarConexion.setOnClickListener(v -> {
            if (validarCampos()) {
                if (hayConexionInternet()) {
                    Toast.makeText(MainActivity.this, "¡Conexión exitosa!", Toast.LENGTH_SHORT).show();
                    btnComenzar.setEnabled(true);
                    btnComenzar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
                } else {
                    Toast.makeText(MainActivity.this, "Error: No hay conexión", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnComenzar.setOnClickListener(v -> {
            if (validarCampos()) {
                iniciarJuego();
            }
        });
    }

    private void cargarSpinners() {
        String[] categorias = {"Cultura General", "Libros", "Películas", "Música", "Computación", "Matemática", "Deportes", "Historia"};
        String[] niveles = {"Fácil", "Medio", "Difícil"};

        ArrayAdapter<String> adapterCategorias = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategorias);

        ArrayAdapter<String> adapterDificultad = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, niveles);
        adapterDificultad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDificultad.setAdapter(adapterDificultad);
    }

    private boolean validarCampos() {
        String cantidadStr = editCantidad.getText().toString().trim();

        if (spinnerCategoria.getSelectedItem() == null || spinnerDificultad.getSelectedItem() == null) {
            Toast.makeText(this, "Seleccione categoría y dificultad", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Ingrese la cantidad de preguntas", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0 || cantidad > 50) {
                Toast.makeText(this, "La cantidad debe ser entre 1 y 50", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean hayConexionInternet() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private void iniciarJuego() {
        try {
            String categoriaSeleccionada = spinnerCategoria.getSelectedItem().toString();
            String dificultadSeleccionada = spinnerDificultad.getSelectedItem().toString();
            String cantidadTexto = editCantidad.getText().toString().trim();

            if (cantidadTexto.isEmpty()) {
                Toast.makeText(this, "Ingrese la cantidad de preguntas", Toast.LENGTH_SHORT).show();
                return;
            }

            int cantidadPreguntas = Integer.parseInt(cantidadTexto);

            // Validaciones adicionales
            if (cantidadPreguntas <= 0 || cantidadPreguntas > 50) {
                Toast.makeText(this, "La cantidad debe ser entre 1 y 50", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mapear a formatos que espera la API
            int categoriaId = mapearCategoriaAId(categoriaSeleccionada);
            String dificultadApi = mapearDificultadApi(dificultadSeleccionada);

            // Crear Intent para PreguntasActivity
            Intent intent = new Intent(MainActivity.this, PreguntasActivity.class);
            intent.putExtra("cantidad", cantidadPreguntas);
            intent.putExtra("dificultad", dificultadApi);
            intent.putExtra("categoriaId", categoriaId);

            // Iniciar la actividad
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "Error al iniciar el juego: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private int mapearCategoriaAId(String categoria) {
        switch (categoria) {
            case "Cultura General": return 9;
            case "Libros": return 10;
            case "Películas": return 11;
            case "Música": return 12;
            case "Computación": return 18;
            case "Matemática": return 19;
            case "Deportes": return 21;
            case "Historia": return 23;
            default: return 9; // Cultura General por defecto
        }
    }

    private String mapearDificultadApi(String dificultad) {
        switch (dificultad.toLowerCase()) {
            case "fácil": return "easy";
            case "medio": return "medium";
            case "difícil": return "hard";
            default: return "easy";
        }
    }
}