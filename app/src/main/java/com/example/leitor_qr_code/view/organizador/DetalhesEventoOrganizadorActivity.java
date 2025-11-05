package com.example.leitor_qr_code.view.organizador;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_evento_organizador);

        eventoDAO = new EventoDAO();
        evento = (Evento) getIntent().getSerializableExtra("eventoSelecionado");

        // --- Configuração dos Componentes ---
        TextView txtTitulo = findViewById(R.id.txtTituloEvento);
        TextView txtDescricao = findViewById(R.id.txtDescricaoEvento);
        TextView txtData = findViewById(R.id.txtDataEvento);
        TextView txtLocal = findViewById(R.id.txtLocalEvento);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);

        // --- Preenchimento dos Dados do Evento ---
        if(evento != null){
            txtTitulo.setText(evento.getNome());
            txtDescricao.setText(evento.getDescricao());
            txtData.setText(evento.getData());
            txtLocal.setText(evento.getLocal());

            // --- Configuração da Lista de Inscritos ---
            recyclerInscritos = findViewById(R.id.recyclerInscritos);
            recyclerInscritos.setLayoutManager(new LinearLayoutManager(this));
            adapter = new InscritoAdapter(listaInscritos);
            recyclerInscritos.setAdapter(adapter);

            // --- Carregar os Inscritos ---
            carregarInscritos(evento.getIdEvento());
        }

        // --- Ação do Botão Voltar ---
        btnVoltar.setOnClickListener(v -> finish());
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
