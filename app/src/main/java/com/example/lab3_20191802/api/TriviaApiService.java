package com.example.lab3_20191802.api;

import com.example.lab3_20191802.model.PreguntaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TriviaApiService {

    @GET("api.php")
    Call<PreguntaResponse> obtenerPreguntas(
            @Query("amount") int amount,
            @Query("category") int category,
            @Query("difficulty") String difficulty,
            @Query("type") String type // siempre "multiple"
    );
}