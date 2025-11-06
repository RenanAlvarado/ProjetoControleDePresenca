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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
            fillEventData(); 
        }
    }

    private void carregarDadosInscritos(String eventoId) {
        textResumoPresentes = findViewById(R.id.textResumoPresentes);

        inscricaoDAO.carregarInscritos(eventoId, usuarios -> {
            if (isFinishing() || isDestroyed()) return;
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
                        if (isFinishing() || isDestroyed()) return;
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
        TextView textStatusEvento = findViewById(R.id.textStatusEvento);
        Button btnConcluir = findViewById(R.id.btnConcluirEvento);
        Button btnEscanear = findViewById(R.id.btnEscanearQrCodes);

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

        if (evento.isConcluido()) {
            textStatusEvento.setVisibility(View.VISIBLE);
            btnConcluir.setVisibility(View.GONE);
            btnEscanear.setVisibility(View.GONE);
        } else {
            textStatusEvento.setVisibility(View.GONE);
            
            boolean scannerLiberado = isScannerLiberado();
            btnEscanear.setEnabled(scannerLiberado);
            btnEscanear.setAlpha(scannerLiberado ? 1.0f : 0.5f);

            boolean eventoIniciado = isEventoIniciado();
            btnConcluir.setEnabled(eventoIniciado);
            btnConcluir.setAlpha(eventoIniciado ? 1.0f : 0.5f);
        }
    }

    private boolean isEventoIniciado() {
        try {
            String dataHoraInicioStr = evento.getDataInicio() + " " + evento.getHoraInicio();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date dataHoraInicio = sdf.parse(dataHoraInicioStr);
            return new Date().after(dataHoraInicio);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; 
        }
    }

    private boolean isScannerLiberado() {
        try {
            String dataHoraInicioStr = evento.getDataInicio() + " " + evento.getHoraInicio();
            String dataHoraFimStr = evento.getDataFim() + " " + evento.getHoraFim();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date dataHoraInicio = sdf.parse(dataHoraInicioStr);
            Date dataHoraFim = sdf.parse(dataHoraFimStr);

            Calendar cal = Calendar.getInstance();
            cal.setTime(dataHoraInicio);
            String liberarAntes = evento.getLiberarScannerAntes();

            if (liberarAntes.contains("1 hora antes")) {
                cal.add(Calendar.HOUR_OF_DAY, -1);
            } else if (liberarAntes.contains("2 horas antes")) {
                cal.add(Calendar.HOUR_OF_DAY, -2);
            } else if (liberarAntes.contains("3 horas antes")) {
                cal.add(Calendar.HOUR_OF_DAY, -3);
            }

            Date inicioScanner = cal.getTime();
            Date agora = new Date();

            return agora.after(inicioScanner) && agora.before(dataHoraFim);

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
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
