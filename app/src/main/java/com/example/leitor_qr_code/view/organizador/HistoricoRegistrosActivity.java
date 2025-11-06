package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.adapter.RegistroAdapter;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.model.Registro;
import com.example.leitor_qr_code.util.BluetoothPrinterHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoricoRegistrosActivity extends AppCompatActivity {

    private RecyclerView recyclerHistorico;
    private RegistroAdapter adapter;
    private List<Registro> listaHistorico = new ArrayList<>();
    private TextView textEmptyState;
    private TextView textDataInscricao;
    private Button btnImprimirCertificado;

    private InscricaoDAO inscricaoDAO;
    private EventoDAO eventoDAO;
    private UsuarioDAO usuarioDAO;

    private Evento evento;
    private String nomeUsuario;
    private Date dataInscricao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_registros);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        String eventoId = getIntent().getStringExtra("eventoId");
        String usuarioId = getIntent().getStringExtra("usuarioId");

        recyclerHistorico = findViewById(R.id.recyclerHistorico);
        textEmptyState = findViewById(R.id.textEmptyHistorico);
        textDataInscricao = findViewById(R.id.textDataInscricao);
        btnImprimirCertificado = findViewById(R.id.btnImprimirCertificado);

        inscricaoDAO = new InscricaoDAO();
        eventoDAO = new EventoDAO();
        usuarioDAO = new UsuarioDAO();

        setupRecyclerView();
        carregarTodosOsDados(eventoId, usuarioId);

        btnImprimirCertificado.setOnClickListener(v -> imprimirCertificado());
    }

    private void setupRecyclerView() {
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegistroAdapter(listaHistorico);
        recyclerHistorico.setAdapter(adapter);
    }

    private void carregarTodosOsDados(String eventoId, String usuarioId) {
        if (eventoId == null || usuarioId == null) return;

        eventoDAO.carregarEventoPorId(eventoId, eventoCarregado -> this.evento = eventoCarregado);
        usuarioDAO.buscarNomePorId(usuarioId, nome -> this.nomeUsuario = nome);
        inscricaoDAO.buscarDataInscricao(eventoId, usuarioId, data -> {
            this.dataInscricao = data;
            if (data != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                textDataInscricao.setText("Inscrito em: " + sdf.format(data));
            } else {
                textDataInscricao.setText("Data da inscrição não encontrada.");
            }
        });
        inscricaoDAO.carregarRegistros(eventoId, usuarioId, registros -> {
            if (registros.isEmpty()) {
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerHistorico.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerHistorico.setVisibility(View.VISIBLE);
                listaHistorico.clear();
                listaHistorico.addAll(registros);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void imprimirCertificado() {
        if (evento == null || nomeUsuario == null) {
            Toast.makeText(this, "Aguardando dados para gerar certificado...", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String horaEntrada = "Não registrou entrada";
        String horaSaida = "Não registrou saída";
        boolean primeiraEntradaRegistrada = false;

        for (Registro registro : listaHistorico) {
            if (registro.getTipo() != null) {
                // CORREÇÃO: Registra apenas a PRIMEIRA entrada
                if (registro.getTipo().equalsIgnoreCase("entrada") && !primeiraEntradaRegistrada) {
                    horaEntrada = sdf.format(registro.getTimestamp());
                    primeiraEntradaRegistrada = true;
                }
                // CORREÇÃO: Sempre atualiza a saída, para pegar a ÚLTIMA
                if (registro.getTipo().equalsIgnoreCase("saida")) {
                    horaSaida = sdf.format(registro.getTimestamp());
                }
            }
        }

        String dataInscricaoStr = "Data indisponível";
        if (dataInscricao != null) {
            dataInscricaoStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dataInscricao);
        }

        String certificado = 
            "CERTIFICADO DE PARTICIPACAO\n" +
            "--------------------------------\n" +
            "Evento: " + evento.getNome() + "\n" +
            "Participante: " + nomeUsuario + "\n\n" +
            "Inscrito em: " + dataInscricaoStr + "\n" +
            "Entrada: " + horaEntrada + "\n" +
            "Saida: " + horaSaida + "\n" +
            "--------------------------------\n";

        BluetoothPrinterHelper printerHelper = new BluetoothPrinterHelper(this);
        printerHelper.printText(certificado);
    }
}
