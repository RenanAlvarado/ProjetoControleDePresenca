package com.example.leitor_qr_code.view.organizador;

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

//Vai ser apagada ou substituida depois
public class SelecionarEventoFragment extends Fragment {

    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos = new ArrayList<>();
    private TextView textEmptyState;
    private EventoDAO eventoDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selecionar_evento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerEventos = view.findViewById(R.id.recyclerEventosParaScanner);
        textEmptyState = view.findViewById(R.id.textEmptyStateScanner);
        eventoDAO = new EventoDAO();

        setupRecyclerView();
        carregarEventos();
    }

    private void setupRecyclerView() {
        recyclerEventos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventoAdapter(listaEventos, evento -> {
            // CORREÇÃO: Usa o nome correto do Fragment (QrCodeFragment)
            Fragment qrCodeFragment = new Fragment();
            Bundle args = new Bundle();
            args.putString("eventoId", evento.getIdEvento());
            qrCodeFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame_container_organizador, qrCodeFragment)
                    .addToBackStack(null) // Permite voltar para a seleção
                    .commit();
        });
        recyclerEventos.setAdapter(adapter);
    }

    private void carregarEventos() {
        // CORREÇÃO: Passa 'false' para carregar eventos não concluídos (ativos)
        eventoDAO.carregarEventosPorOrganizador(false, eventos -> {
            if (eventos.isEmpty()) { // CORREÇÃO: erro de digitação
                textEmptyState.setVisibility(View.VISIBLE);
                recyclerEventos.setVisibility(View.GONE);
            } else {
                textEmptyState.setVisibility(View.GONE);
                recyclerEventos.setVisibility(View.VISIBLE);
                listaEventos.clear();
                listaEventos.addAll(eventos);
                adapter.notifyDataSetChanged();
            }
        });
    }
}
