package com.example.leitor_qr_code.view.organizador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.model.Usuario;
import com.example.leitor_qr_code.util.InscritoAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DetalhesEventoOrganizadorActivity extends AppCompatActivity {

    private Evento evento;
    private EventoDAO eventoDAO;
    private InscricaoDAO inscricaoDAO;
    private RecyclerView recyclerInscritos;
    private InscritoAdapter adapter;
    private List<Usuario> listaInscritos = new ArrayList<>();
    private Button btnExcluir, btnEscanear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_organizador);

        eventoDAO = new EventoDAO();
        inscricaoDAO = new InscricaoDAO();
        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");

        setupViewsAndListeners();

        if(evento != null){
            fillEventData();
            setupRecyclerView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (evento != null) {
            // CORREÇÃO: Passa o ID do evento para o método
            carregarDadosInscritos(evento.getIdEvento());
        }
    }

    private void carregarDadosInscritos(String eventoId) {
        inscricaoDAO.carregarInscritos(eventoId, usuarios -> {
            if (usuarios == null || usuarios.isEmpty()) {
                Toast.makeText(this, "Nenhum participante inscrito ainda.", Toast.LENGTH_SHORT).show();
                listaInscritos.clear();
                adapter.notifyDataSetChanged();
                return;
            }

            AtomicInteger counter = new AtomicInteger(usuarios.size());
            for (Usuario usuario : usuarios) {
                inscricaoDAO.verificarStatusPresenca(eventoId, usuario.getId(), status -> {
                    usuario.setStatusPresenca(status);
                    if (counter.decrementAndGet() == 0) {
                        listaInscritos.clear();
                        listaInscritos.addAll(usuarios);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
    
    private void setupViewsAndListeners(){
         btnExcluir = findViewById(R.id.btnExcluirEvento);
        btnEscanear = findViewById(R.id.btnEscanearQrCodes);
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        
        btnEscanear.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerOrganizadorActivity.class);
            intent.putExtra("eventoId", evento.getIdEvento());
            startActivity(intent);
        });

        btnExcluir.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Excluir Evento")
                .setMessage("Tem certeza?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    inscricaoDAO.excluirInscricoesPorEvento(evento.getIdEvento(), 
                        () -> eventoDAO.excluirEvento(evento.getIdEvento(), this, this::finish, () -> {}),
                        () -> {}
                    );
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });
    }
    
    private void fillEventData(){
        ((TextView) findViewById(R.id.txtTituloEvento)).setText(evento.getNome());
        ((TextView) findViewById(R.id.txtDescricaoEvento)).setText(evento.getDescricao());
        ((TextView) findViewById(R.id.txtLocalEvento)).setText(evento.getLocal());
        ((TextView) findViewById(R.id.txtDataHoraInicio)).setText("Início: " + evento.getDataInicio() + " às " + evento.getHoraInicio());
        ((TextView) findViewById(R.id.txtDataHoraFim)).setText("Fim: " + evento.getDataFim() + " às " + evento.getHoraFim());
        ((TextView) findViewById(R.id.txtLiberarScanner)).setText("Scanner liberado: " + evento.getLiberarScannerAntes());
    }
    
    private void setupRecyclerView(){
        recyclerInscritos = findViewById(R.id.recyclerInscritos);
        recyclerInscritos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InscritoAdapter(listaInscritos);
        recyclerInscritos.setAdapter(adapter);
    }
}
