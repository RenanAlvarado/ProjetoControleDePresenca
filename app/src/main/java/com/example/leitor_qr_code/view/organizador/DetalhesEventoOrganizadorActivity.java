package com.example.leitor_qr_code.view.organizador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private TextView textResumoPresentes;

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
            carregarDadosInscritos(evento.getIdEvento());
        }
    }

    private void carregarDadosInscritos(String eventoId) {
        textResumoPresentes = findViewById(R.id.textResumoPresentes);

        inscricaoDAO.carregarInscritos(eventoId, usuarios -> {
            if (usuarios.isEmpty()) {
                listaInscritos.clear();
                adapter.notifyDataSetChanged();
                textResumoPresentes.setText("Presentes: 0 / 0");
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

                        long presentes = usuarios.stream().filter(u -> "Entrou".equals(u.getStatusPresenca())).count();
                        textResumoPresentes.setText("Presentes: " + presentes + " / " + usuarios.size());
                    }
                });
            }
        });
    }
    
    private void setupViewsAndListeners(){
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        
        findViewById(R.id.btnEscanearQrCodes).setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerOrganizadorActivity.class);
            intent.putExtra("eventoId", evento.getIdEvento());
            startActivity(intent);
        });

        findViewById(R.id.btnConcluirEvento).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Concluir Evento")
                .setMessage("Deseja marcar este evento como concluído?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    eventoDAO.concluirEvento(evento.getIdEvento(), success -> {
                        if(success) {
                            Toast.makeText(this, "Evento concluído com sucesso!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Falha ao concluir evento.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Não", null)
                .show();
        });

        findViewById(R.id.btnExcluirEvento).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Excluir Evento")
                .setMessage("Tem certeza? Isso apagará todos os dados permanentemente.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    inscricaoDAO.excluirInscricoesPorEvento(evento.getIdEvento(), 
                        () -> eventoDAO.excluirEvento(evento.getIdEvento(), this, this::finish, () -> {}),
                        () -> Toast.makeText(this, "Falha ao excluir inscrições.", Toast.LENGTH_SHORT).show()
                    );
                })
                .setNegativeButton("Cancelar", null)
                .show();
        });
    }
    
    private void fillEventData(){
        // Referências dos botões e do status
        TextView textStatusEvento = findViewById(R.id.textStatusEvento);
        Button btnConcluir = findViewById(R.id.btnConcluirEvento);
        Button btnEscanear = findViewById(R.id.btnEscanearQrCodes);

        // Preenchimento dos textos
        ((TextView) findViewById(R.id.txtTituloEvento)).setText(evento.getNome());
        ((TextView) findViewById(R.id.txtDescricaoEvento)).setText(evento.getDescricao());
        ((TextView) findViewById(R.id.txtLocalEvento)).setText(evento.getLocal());
        ((TextView) findViewById(R.id.txtDataHoraInicio)).setText("Início: " + evento.getDataInicio() + " às " + evento.getHoraInicio());
        ((TextView) findViewById(R.id.txtDataHoraFim)).setText("Fim: " + evento.getDataFim() + " às " + evento.getHoraFim());
        ((TextView) findViewById(R.id.txtLiberarScanner)).setText("Scanner liberado: " + evento.getLiberarScannerAntes());
        
        TextView txtPermiteReentrada = findViewById(R.id.txtPermiteReentrada);
        if (evento.isPermiteMultiplasEntradas()) {
            txtPermiteReentrada.setText("Reentrada: Permitida");
        } else {
            txtPermiteReentrada.setText("Reentrada: Não Permitida");
        }

        // Lógica de visibilidade
        if (evento.isConcluido()) {
            textStatusEvento.setVisibility(View.VISIBLE);
            btnConcluir.setVisibility(View.GONE);
            btnEscanear.setVisibility(View.GONE);
        } else {
            textStatusEvento.setVisibility(View.GONE);
            btnConcluir.setVisibility(View.VISIBLE);
            btnEscanear.setVisibility(View.VISIBLE);
        }
    }
    
    private void setupRecyclerView(){
        recyclerInscritos = findViewById(R.id.recyclerInscritos);
        recyclerInscritos.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new InscritoAdapter(listaInscritos, usuario -> {
            Intent intent = new Intent(this, HistoricoRegistrosActivity.class);
            intent.putExtra("eventoId", evento.getIdEvento());
            intent.putExtra("usuarioId", usuario.getId());
            startActivity(intent);
        });
        recyclerInscritos.setAdapter(adapter);
    }
}
