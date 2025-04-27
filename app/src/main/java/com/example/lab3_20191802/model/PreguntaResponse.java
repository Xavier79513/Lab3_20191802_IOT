package com.example.lab3_20191802.model;


import java.util.List;

public class PreguntaResponse {
    private int response_code;
    private List<Pregunta> results;

    public List<Pregunta> getResults() {
        return results;
    }
}
