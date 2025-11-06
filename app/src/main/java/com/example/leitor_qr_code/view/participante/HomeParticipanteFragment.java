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
import com.example.leitor_qr_code.dao.InscricaoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.util.EventoAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HomeParticipanteFragment extends Fragment {

    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos = new ArrayList<>();
    private TextView textEmptyState;
    private EventoDAO eventoDAO;
    private InscricaoDAO inscricaoDAO;

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
        inscricaoDAO = new InscricaoDAO();

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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (uid == null) return;
        
        // 1. Pega a lista de eventos em que o usuário está inscrito
        inscricaoDAO.carregarEventosInscritos(uid, eventosInscritos -> {
            List<String> idsInscritos = eventosInscritos.stream()
                                                    .map(Evento::getIdEvento)
                                                    .collect(Collectors.toList());
            
            // 2. Passa essa lista para o método que busca os eventos disponíveis
            eventoDAO.carregarEventosDisponiveis(idsInscritos, eventosDisponiveis -> {
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
        });
    }
}
