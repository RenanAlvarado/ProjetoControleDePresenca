package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConfirmacaoEntradaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmacao_entrada);

        // --- Referências ---
        Button btnVoltarScanner = findViewById(R.id.btnVoltarScanner);
        TextView textNomeParticipante = findViewById(R.id.textNomeParticipanteConfirmacao);
        TextView textHorarioEntrada = findViewById(R.id.textHorarioEntrada);

        // --- Ações ---
        btnVoltarScanner.setOnClickListener(v -> finish());

        // --- Preenchimento dos Dados ---
        String nome = getIntent().getStringExtra("nomeParticipante");
        if (nome != null) {
            textNomeParticipante.setText(nome);
        }

        // Define o horário atual
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String horarioAtual = sdf.format(new Date());
        textHorarioEntrada.setText("Entrada registrada às " + horarioAtual);

        // A lógica do botão de imprimir será adicionada depois
    }
}
