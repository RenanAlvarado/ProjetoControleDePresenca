package com.example.leitor_qr_code.view.organizador;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.LoginActivity;
import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.example.leitor_qr_code.view.participante.MainParticipanteActivity;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilOrganizadorFragment extends Fragment {

    private ImageView imgPerfil, btnEditarFoto;
    private EditText editNome, editEmail, editNovaSenha, editConfirmarSenha;
    private Button btnSalvar, btnExcluirConta, btnSair, btnMudarParaParticipante, btnMudarParaOrganizador;
    private ActivityResultLauncher<String> imagePicker;
    private UsuarioDAO usuarioDAO;

    public PerfilOrganizadorFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usuarioDAO = new UsuarioDAO();
        imagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), uri);
                    usuarioDAO.atualizarFotoUsuario(requireActivity(), bitmap, imgPerfil);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgPerfil = view.findViewById(R.id.imgPerfil);
        btnEditarFoto = view.findViewById(R.id.btnEditarFoto);
        editNome = view.findViewById(R.id.editNome);
        editEmail = view.findViewById(R.id.editEmail);
        editNovaSenha = view.findViewById(R.id.editNovaSenha);
        editConfirmarSenha = view.findViewById(R.id.editConfirmarSenha);
        btnSalvar = view.findViewById(R.id.btnSalvar);
        btnExcluirConta = view.findViewById(R.id.btnExcluirConta);
        btnMudarParaParticipante = view.findViewById(R.id.btnMudarParaParticipante);
        btnMudarParaOrganizador = view.findViewById(R.id.btnMudarParaOrganizador);
        btnSair = view.findViewById(R.id.btnSair);

        btnMudarParaParticipante.setVisibility(View.VISIBLE);
        btnMudarParaOrganizador.setVisibility(View.GONE);

        btnEditarFoto.setOnClickListener(v -> imagePicker.launch("image/*"));

        usuarioDAO.carregarDadosUsuario(imgPerfil, editNome, editEmail, this);

        btnSalvar.setOnClickListener(v -> salvarAlteracoes());

        btnExcluirConta.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Excluir Conta")
                    .setMessage("Tem certeza que deseja excluir sua conta? Todos os seus dados, eventos e inscrições serão apagados permanentemente. Esta ação é irreversível.")
                    .setPositiveButton("Excluir", (dialog, which) -> excluirConta())
                    .setNegativeButton("Cancelar", null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        });

        btnMudarParaParticipante.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainParticipanteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if(getActivity() != null) {
                getActivity().finish();
            }
        });

        btnSair.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if(getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void salvarAlteracoes() {
        String novoNome = editNome.getText().toString().trim();
        String novoEmail = editEmail.getText().toString().trim();
        String novaSenha = editNovaSenha.getText().toString().trim();
        String confirmarSenha = editConfirmarSenha.getText().toString().trim();

        if (novoNome.isEmpty() || novoEmail.isEmpty()) {
            Toast.makeText(getContext(), "Nome e email não podem estar vazios.", Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioDAO.atualizarDadosUsuario(novoNome, novoEmail, success -> {
            if (getContext() == null || !isAdded()) return;
            if (success) {
                Toast.makeText(getContext(), "Nome e email salvos com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Falha ao salvar nome e email.", Toast.LENGTH_SHORT).show();
            }
        });

        if (!TextUtils.isEmpty(novaSenha)) {
            if (!novaSenha.equals(confirmarSenha)) {
                Toast.makeText(getContext(), "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (novaSenha.length() < 6) {
                Toast.makeText(getContext(), "A nova senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show();
                return;
            }

            usuarioDAO.atualizarSenha(novaSenha, success -> {
                if (getContext() == null || !isAdded()) return;
                if (success) {
                    Toast.makeText(getContext(), "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show();
                    editNovaSenha.setText("");
                    editConfirmarSenha.setText("");
                } else {
                    Toast.makeText(getContext(), "Falha ao alterar a senha. Tente fazer login novamente.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void excluirConta() {
        Toast.makeText(getContext(), "Excluindo conta... Por favor, aguarde.", Toast.LENGTH_LONG).show();
        usuarioDAO.excluirContaCompleta(success -> {
            if (getActivity() == null || !isAdded()) return;

            if (success) {
                Toast.makeText(getContext(), "Conta excluída com sucesso.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            } else {
                Toast.makeText(getContext(), "Falha ao excluir a conta. Tente fazer login novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }
}