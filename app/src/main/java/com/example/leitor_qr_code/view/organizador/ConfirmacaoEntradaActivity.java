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
        TextView textStatus = findViewById(R.id.textBoasVindas); // Referência para o título

        // --- Ações ---
        btnVoltarScanner.setOnClickListener(v -> finish());

        // --- Preenchimento dos Dados ---
        String nome = getIntent().getStringExtra("nomeParticipante");
        String status = getIntent().getStringExtra("statusRegistro"); // Recebe o status

        if (nome != null) {
            textNomeParticipante.setText(nome);
        }
        if (status != null) {
            textStatus.setText(status); // Exibe o status ("Entrada Registrada" ou "Saída Registrada")
        }

        // Define o horário atual
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String horarioAtual = sdf.format(new Date());
        textHorarioEntrada.setText("Horário do registro: " + horarioAtual);
    }
}
