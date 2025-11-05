package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.R;

public class ConfirmacaoEntradaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacao_entrada);

        Button btnVoltarScanner = findViewById(R.id.btnVoltarScanner);
        btnVoltarScanner.setOnClickListener(v -> finish());

        // A lógica do botão de imprimir será adicionada depois
    }
}
