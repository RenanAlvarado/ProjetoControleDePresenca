package com.example.leitor_qr_code.view.organizador;

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
import java.util.stream.Collectors;

public class HistoricoOrganizadorFragment extends Fragment {

    private RecyclerView recyclerHistorico;
    private EventoAdapter adapter;
    private List<Evento> listaEventosHistorico = new ArrayList<>();
    private TextView textEmptyState;
    private EventoDAO eventoDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico_organizador, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerHistorico = view.findViewById(R.id.recyclerHistoricoEventos);
        textEmptyState = view.findViewById(R.id.textEmptyStateHistorico);
        eventoDAO = new EventoDAO();

        setupRecyclerView();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarHistoricoEventos();
    }

    private void setupRecyclerView() {
        recyclerHistorico.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventoAdapter(listaEventosHistorico, evento -> {
            Intent intent = new Intent(getActivity(), DetalhesEventoOrganizadorActivity.class);
            intent.putExtra("eventoSelecionado", evento);
            startActivity(intent);
        });
        recyclerHistorico.setAdapter(adapter);
    }

    private void carregarHistoricoEventos() {
        // Pede ao DAO para trazer apenas os eventos CONCLUÍDOS
        eventoDAO.carregarEventosPorOrganizador(true, eventos -> {
            if (eventos.isEmpty()) {
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerHistorico.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerHistorico.setVisibility(View.VISIBLE);
                listaEventosHistorico.clear();
                listaEventosHistorico.addAll(eventos);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
