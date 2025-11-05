package com.example.leitor_qr_code.view.participante;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.util.EventoAdapter;

import java.util.ArrayList;
import java.util.List;

public class InscricoesParticipanteFragment extends Fragment {

    private RecyclerView recyclerMeusEventos;
    private EventoAdapter adapter;
    private List<Evento> listaMeusEventos = new ArrayList<>();
    private TextView textEmptyState;
    private EventoDAO eventoDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inscricoes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerMeusEventos = view.findViewById(R.id.recyclerMeusEventos);
        textEmptyState = view.findViewById(R.id.textEmptyState);
        eventoDAO = new EventoDAO();

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        // CORREÇÃO: Carrega os eventos toda vez que a tela se torna visível
        carregarEventosInscritos();
    }

    private void setupRecyclerView() {
        recyclerMeusEventos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventoAdapter(listaMeusEventos, evento -> {
            Intent intent = new Intent(getActivity(), DetalhesEventoParticipanteActivity.class);
            intent.putExtra("eventoSelecionado", evento);
            startActivity(intent);
        });
        recyclerMeusEventos.setAdapter(adapter);
    }

    private void carregarEventosInscritos() {
        eventoDAO.carregarEventosInscritos(eventos -> {
            if (eventos.isEmpty()) {
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerMeusEventos.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerMeusEventos.setVisibility(View.VISIBLE);
                listaMeusEventos.clear();
                listaMeusEventos.addAll(eventos);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
