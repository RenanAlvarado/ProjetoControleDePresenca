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

public class HomeParticipanteFragment extends Fragment {

    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos = new ArrayList<>();
    private TextView textEmptyState;
    private EventoDAO eventoDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_participante, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerEventos = view.findViewById(R.id.recyclerEventos);
        textEmptyState = view.findViewById(R.id.textEmptyStateHome);
        eventoDAO = new EventoDAO();

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarEventosDisponiveis();
    }

    private void setupRecyclerView() {
        recyclerEventos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventoAdapter(listaEventos, evento -> {
            Intent intent = new Intent(getActivity(), DetalhesEventoParticipanteActivity.class);
            intent.putExtra("eventoSelecionado", evento);
            startActivity(intent);
        });
        recyclerEventos.setAdapter(adapter);
    }

    private void carregarEventosDisponiveis() {
        eventoDAO.carregarEventosDisponiveis(eventosDisponiveis -> {
            if (eventosDisponiveis.isEmpty()) {
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerEventos.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerEventos.setVisibility(View.VISIBLE);
                listaEventos.clear();
                listaEventos.addAll(eventosDisponiveis);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
