package com.example.leitor_qr_code.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.EventoDAO;
import com.example.leitor_qr_code.model.Evento;
import com.example.leitor_qr_code.util.EventoAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeOrganizadorFragment extends Fragment {

    private RecyclerView recyclerEventos;
    private EventoAdapter adapter;
    private List<Evento> listaEventos = new ArrayList<>();
    private EventoDAO eventoDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_organizador, container, false);

        recyclerEventos = view.findViewById(R.id.recyclerEventos);
        recyclerEventos.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventoAdapter(listaEventos, evento -> {
            Toast.makeText(getContext(), "Selecionado: " + evento.getNome(), Toast.LENGTH_SHORT).show();
            // Aqui depois vamos abrir tela de detalhes
        });
        recyclerEventos.setAdapter(adapter);

        eventoDAO = new EventoDAO();

        carregarEventos();

        return view;
    }

    private void carregarEventos() {
        eventoDAO.carregarEventosPorOrganizador(eventos -> {
            listaEventos.clear();
            listaEventos.addAll(eventos);
            adapter.notifyDataSetChanged();

            if (eventos.isEmpty()) {
                Toast.makeText(getContext(), "Nenhum evento cadastrado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
