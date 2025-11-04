package com.example.leitor_qr_code.view.participante;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.util.EventoAdapter;
import com.example.leitor_qr_code.view.organizador.DetalhesEventoOrganizadorActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeParticipanteFragment extends Fragment {

    private RecyclerView recyclerView;
    private final List<Evento> listaEventos = new ArrayList<>();
    private EventoAdapter eventoAdapter;
    private EventoDAO eventoDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_participante, container, false);

        recyclerView = view.findViewById(R.id.recyclerEventosDisponiveis);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventoAdapter = new EventoAdapter(listaEventos, evento -> {
            Intent intent = new Intent(getContext(), DetalhesEventoParticipanteActivity.class);
            intent.putExtra("eventoSelecionado", evento);
            startActivity(intent);
        });

        recyclerView.setAdapter(eventoAdapter);

        eventoDAO = new EventoDAO();
        carregarEventosDisponiveis();

        return view;
    }

    private void carregarEventosDisponiveis() {
        eventoDAO.carregarEventosDisponiveis(eventos -> {
            listaEventos.clear();
            listaEventos.addAll(eventos);
            eventoAdapter.notifyDataSetChanged();
        });
    }
}