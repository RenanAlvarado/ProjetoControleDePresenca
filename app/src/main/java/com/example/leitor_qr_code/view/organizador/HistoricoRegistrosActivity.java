package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.adapter.RegistroAdapter;
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.example.leitor_qr_code.model.Registro;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoRegistrosActivity extends AppCompatActivity {

    private RecyclerView recyclerHistorico;
    private RegistroAdapter adapter;
    private List<Registro> listaHistorico = new ArrayList<>();
    private TextView textEmptyState;
    private TextView textDataInscricao; // Novo
    private InscricaoDAO inscricaoDAO;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico_registros);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        String eventoId = getIntent().getStringExtra("eventoId");
        String usuarioId = getIntent().getStringExtra("usuarioId");

        recyclerHistorico = findViewById(R.id.recyclerHistorico);
        textEmptyState = findViewById(R.id.textEmptyHistorico);
        textDataInscricao = findViewById(R.id.textDataInscricao); // Referência
        inscricaoDAO = new InscricaoDAO();

        setupRecyclerView();
        carregarHistorico(eventoId, usuarioId);
        carregarDataInscricao(eventoId, usuarioId); // Chamada do novo método
    }

    private void setupRecyclerView() {
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RegistroAdapter(listaHistorico);
        recyclerHistorico.setAdapter(adapter);
    }

    private void carregarDataInscricao(String eventoId, String usuarioId) {
        if (eventoId == null || usuarioId == null) return;
        inscricaoDAO.buscarDataInscricao(eventoId, usuarioId, data -> {
            if (data != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                textDataInscricao.setText("Inscrito em: " + sdf.format(data));
            } else {
                textDataInscricao.setText("Data da inscrição não encontrada.");
            }
        });
    }

    private void carregarHistorico(String eventoId, String usuarioId) {
        if (eventoId == null || usuarioId == null) return;

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
}
