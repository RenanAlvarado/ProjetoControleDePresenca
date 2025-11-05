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
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.model.Usuario;
import com.example.leitor_qr_code.util.InscritoAdapter;

import java.util.ArrayList;
import java.util.List;

public class DetalhesEventoOrganizadorActivity extends AppCompatActivity {

    private Evento evento;
    private EventoDAO eventoDAO;
    private RecyclerView recyclerInscritos;
    private InscritoAdapter adapter;
    private List<Usuario> listaInscritos = new ArrayList<>();
    private Button btnExcluir, btnEscanear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_organizador);

        eventoDAO = new EventoDAO();
        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");

        // --- Referências ---
        TextView txtTitulo = findViewById(R.id.txtTituloEvento);
        TextView txtDescricao = findViewById(R.id.txtDescricaoEvento);
        TextView txtLocal = findViewById(R.id.txtLocalEvento);
        TextView txtDataHoraInicio = findViewById(R.id.txtDataHoraInicio);
        TextView txtDataHoraFim = findViewById(R.id.txtDataHoraFim);
        TextView txtLiberarScanner = findViewById(R.id.txtLiberarScanner);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnExcluir = findViewById(R.id.btnExcluirEvento);
        btnEscanear = findViewById(R.id.btnEscanearQrCodes);

        // --- Ações dos Botões ---
        btnVoltar.setOnClickListener(v -> finish());
        
        btnEscanear.setOnClickListener(v -> {
            Intent intent = new Intent(this, ScannerOrganizadorActivity.class);
            intent.putExtra("eventoId", evento.getIdEvento());
            startActivity(intent);
        });

        btnExcluir.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Excluir Evento")
                    .setMessage("Tem certeza que deseja excluir este evento?")
                    .setPositiveButton("Excluir", (dialog, which) -> {
                        eventoDAO.excluirEvento(evento.getIdEvento(), this, this::finish);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
        
        // --- Preenchimento dos Dados ---
        if(evento != null){
            txtTitulo.setText(evento.getNome());
            txtDescricao.setText(evento.getDescricao());
            txtLocal.setText(evento.getLocal());
            txtDataHoraInicio.setText("Início: " + evento.getDataInicio() + " às " + evento.getHoraInicio());
            txtDataHoraFim.setText("Fim: " + evento.getDataFim() + " às " + evento.getHoraFim());
            txtLiberarScanner.setText("Scanner liberado: " + evento.getLiberarScannerAntes());

            recyclerInscritos = findViewById(R.id.recyclerInscritos);
            recyclerInscritos.setLayoutManager(new LinearLayoutManager(this));
            adapter = new InscritoAdapter(listaInscritos);
            recyclerInscritos.setAdapter(adapter);

            carregarInscritos(evento.getIdEvento());
        }
    }

    private void carregarInscritos(String eventoId) {
        eventoDAO.carregarInscritos(eventoId, usuarios -> {
            listaInscritos.clear();
            listaInscritos.addAll(usuarios);
            adapter.notifyDataSetChanged();

            if (usuarios.isEmpty()) {
                Toast.makeText(this, "Nenhum participante inscrito ainda.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
