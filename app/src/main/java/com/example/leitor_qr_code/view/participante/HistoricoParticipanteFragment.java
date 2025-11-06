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
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.util.EventoAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoricoParticipanteFragment extends Fragment {

    private RecyclerView recyclerHistorico;
    private EventoAdapter adapter;
    private List<Evento> listaEventosHistorico = new ArrayList<>();
    private TextView textEmptyState;
    private InscricaoDAO inscricaoDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_historico_participante, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerHistorico = view.findViewById(R.id.recyclerHistoricoParticipante);
        textEmptyState = view.findViewById(R.id.textEmptyStateHistoricoParticipante);
        inscricaoDAO = new InscricaoDAO();

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
            Intent intent = new Intent(getActivity(), DetalhesEventoParticipanteActivity.class);
            intent.putExtra("eventoSelecionado", evento);
            intent.putExtra("isHistorico", true); // Sinalizador adicionado
            startActivity(intent);
        });
        recyclerHistorico.setAdapter(adapter);
    }

    private void carregarHistoricoEventos() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null) return;

        inscricaoDAO.carregarEventosInscritos(uid, eventos -> {
            if (getContext() == null || !isAdded()) return;

            List<Evento> eventosConcluidos = eventos.stream()
                    .filter(Evento::isConcluido)
                    .collect(Collectors.toList());

            if (eventosConcluidos.isEmpty()) {
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerHistorico.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerHistorico.setVisibility(View.VISIBLE);
                listaEventosHistorico.clear();
                listaEventosHistorico.addAll(eventosConcluidos);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
