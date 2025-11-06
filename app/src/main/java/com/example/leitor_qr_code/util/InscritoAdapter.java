package com.example.leitor_qr_code.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.model.Usuario;

import java.util.List;

public class InscritoAdapter extends RecyclerView.Adapter<InscritoAdapter.InscritoViewHolder> {

    private final List<Usuario> listaInscritos;
    private final OnInscritoClickListener listener; // Adicionado

    // Interface para o clique
    public interface OnInscritoClickListener {
        void onInscritoClick(Usuario usuario);
    }

    // Construtor atualizado
    public InscritoAdapter(List<Usuario> listaInscritos, OnInscritoClickListener listener) {
        this.listaInscritos = listaInscritos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InscritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inscrito, parent, false);
        return new InscritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InscritoViewHolder holder, int position) {
        Usuario usuario = listaInscritos.get(position);
        holder.txtNome.setText(usuario.getNome());
        holder.txtEmail.setText(usuario.getEmail());

        Context context = holder.itemView.getContext();

        switch (usuario.getStatusPresenca()) {
            case "Entrou":
                holder.textStatus.setText("Entrou");
                holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.status_entrou_text));
                holder.textStatus.setVisibility(View.VISIBLE);
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_entrou_bg));
                break;
            case "Saiu":
                holder.textStatus.setText("Saiu");
                holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.status_saiu_text));
                holder.textStatus.setVisibility(View.VISIBLE);
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.status_saiu_bg));
                break;
            default: // "Não Entrou"
                holder.textStatus.setText("Não Entrou");
                holder.textStatus.setVisibility(View.VISIBLE);
                holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.black));
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                break;
        }
        
        // Define a ação de clique
        holder.itemView.setOnClickListener(v -> listener.onInscritoClick(usuario));
    }

    @Override
    public int getItemCount() {
        return listaInscritos.size();
    }

    static class InscritoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtEmail, textStatus;
        CardView cardView;

        public InscritoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.textNomeInscrito);
            txtEmail = itemView.findViewById(R.id.textEmailInscrito);
            textStatus = itemView.findViewById(R.id.textStatusPresenca);
            cardView = itemView.findViewById(R.id.card_inscrito);
        }
    }
}
