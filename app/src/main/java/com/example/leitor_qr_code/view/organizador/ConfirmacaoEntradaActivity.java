package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.util.BluetoothPrinterHelper;

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
        // ID CORRIGIDO
        Button btnImprimir = findViewById(R.id.btnImprimir);
        TextView textNomeParticipante = findViewById(R.id.textNomeParticipanteConfirmacao);
        TextView textHorarioEntrada = findViewById(R.id.textHorarioEntrada);
        TextView textStatus = findViewById(R.id.textBoasVindas);

        // --- Ações ---
        btnVoltarScanner.setOnClickListener(v -> finish());

        // --- Preenchimento dos Dados ---
        String nomeEvento = getIntent().getStringExtra("nomeEvento");
        String nomeParticipante = getIntent().getStringExtra("nomeParticipante");
        String emailParticipante = getIntent().getStringExtra("emailParticipante");
        String status = getIntent().getStringExtra("statusRegistro");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String horarioAtual = sdf.format(new Date());

        if (nomeParticipante != null) {
            textNomeParticipante.setText(nomeParticipante);
        }
        if (status != null) {
            textStatus.setText(status);
        }
        textHorarioEntrada.setText("Horário do registro: " + horarioAtual);

        // --- Ação do botão Imprimir ---
        btnImprimir.setOnClickListener(v -> {
            if (nomeEvento == null || nomeParticipante == null || emailParticipante == null) {
                Toast.makeText(this, "Dados insuficientes para imprimir.", Toast.LENGTH_SHORT).show();
                return;
            }

            BluetoothPrinterHelper printerHelper = new BluetoothPrinterHelper(this);

            // LÓGICA DO TÍTULO CORRIGIDA
            String tituloComprovante = "COMPROVANTE DE ENTRADA";
            // Verifica por "saida" (sem acento), pois é como vem do DAO
            if (status != null && status.toLowerCase().contains("saida")) {
                tituloComprovante = "COMPROVANTE DE SAÍDA";
            }

            // Formata o texto do comprovante
            String comprovante = 
                tituloComprovante + "\n" +
                "--------------------------------\n" +
                "Evento: " + nomeEvento + "\n" +
                "Participante: " + nomeParticipante + "\n" +
                "Email: " + emailParticipante + "\n" +
                "Horario: " + horarioAtual + "\n" +
                "--------------------------------\n";

            printerHelper.printText(comprovante);
        });
    }
}
