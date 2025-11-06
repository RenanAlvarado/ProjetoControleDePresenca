package com.example.leitor_qr_code.view.organizador;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.util.EventoAdapter;
import com.example.leitor_qr_code.view.dicas.DicasActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class HomeOrganizadorFragment extends Fragment {

    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos = new ArrayList<>();
    private EventoDAO eventoDAO;
    private UsuarioDAO usuarioDAO;
    private TextView textSaudacao;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_organizador, container, false);

        recyclerEventos = view.findViewById(R.id.recyclerEventos);
        textSaudacao = view.findViewById(R.id.textSaudacao);
        FloatingActionButton fabDicas = view.findViewById(R.id.fabDicas);

        recyclerEventos.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventoAdapter(listaEventos, evento -> {
            Intent intent = new Intent(getActivity(), DetalhesEventoOrganizadorActivity.class);
            intent.putExtra("eventoSelecionado", evento);
            startActivity(intent);
        });
        recyclerEventos.setAdapter(adapter);

        eventoDAO = new EventoDAO();
        usuarioDAO = new UsuarioDAO();

        fabDicas.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DicasActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void carregarEventos() {
        eventoDAO.carregarEventosPorOrganizador(false, eventos -> {
            if (getContext() == null || !isAdded()) {
                return;
            }

            if (eventos.isEmpty()) {
                Toast.makeText(getContext(), "Nenhum evento ativo encontrado.", Toast.LENGTH_SHORT).show();
            }
            listaEventos.clear();
            listaEventos.addAll(eventos);
            adapter.notifyDataSetChanged();
        });
    }

    private void carregarNomeUsuario() {
        usuarioDAO.buscarNomeUsuarioLogado(nome -> {
            if (getContext() == null || !isAdded()) return;

            if (nome != null && !nome.isEmpty()) {
                textSaudacao.setText("Olá, " + nome);
            } else {
                textSaudacao.setText("Olá!");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        carregarNomeUsuario();

        eventoDAO.verificarEConcluirEventosAutomaticamente(eventosConcluidos -> {
            if (getContext() == null || !isAdded()) {
                return;
            }
            if (eventosConcluidos > 0) {
                String plural = eventosConcluidos > 1 ? "s" : "";
                Toast.makeText(getContext(), eventosConcluidos + " evento" + plural + " foi concluído automaticamente.", Toast.LENGTH_LONG).show();
            }
            carregarEventos();
        });
    }
}
