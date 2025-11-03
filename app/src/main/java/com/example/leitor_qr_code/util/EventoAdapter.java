package com.example.leitor_qr_code.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.model.Evento;

import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.EventoViewHolder> {

    private final List<Evento> listaEventos;
    private final OnEventoClickListener listener;

    public interface OnEventoClickListener {
        void onClick(Evento evento);
    }

    public EventoAdapter(List<Evento> listaEventos, OnEventoClickListener listener) {
        this.listaEventos = listaEventos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_evento, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = listaEventos.get(position);

        // CORREÇÃO: Alterado de getTitulo() para getNome()
        holder.txtNome.setText(evento.getNome());
        holder.txtLocal.setText(evento.getLocal());
        holder.txtData.setText(evento.getData());

        holder.itemView.setOnClickListener(v -> listener.onClick(evento));
    }

    @Override
    public int getItemCount() {
        return listaEventos.size();
    }

    static class EventoViewHolder extends RecyclerView.ViewHolder {

        TextView txtNome, txtData, txtLocal;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);

            txtNome = itemView.findViewById(R.id.textNomeEvento);
            txtLocal = itemView.findViewById(R.id.textLocalEvento);
            txtData = itemView.findViewById(R.id.textDataEvento);
        }
    }
}
