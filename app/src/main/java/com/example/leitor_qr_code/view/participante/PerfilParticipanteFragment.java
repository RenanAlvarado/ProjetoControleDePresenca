package com.example.leitor_qr_code.view.participante;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.LoginActivity;
import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilParticipanteFragment extends Fragment {

    private ImageView imgPerfil, btnEditarFoto;
    private EditText editNome, editEmail, editTipo;
    private Button btnSalvar, btnSair;
    private ActivityResultLauncher<String> imagePicker;
    private UsuarioDAO usuarioDAO;

    public PerfilParticipanteFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usuarioDAO = new UsuarioDAO();

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(), uri
                            );
                            usuarioDAO.atualizarFotoUsuario(requireActivity(), bitmap, imgPerfil);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referenciando os componentes do layout
        imgPerfil = view.findViewById(R.id.imgPerfil);
        btnEditarFoto = view.findViewById(R.id.btnEditarFoto);
        editNome = view.findViewById(R.id.editNome);
        editEmail = view.findViewById(R.id.editEmail);
        editTipo = view.findViewById(R.id.editTipo);
        btnSalvar = view.findViewById(R.id.btnSalvar);
        btnSair = view.findViewById(R.id.btnSair);

        btnEditarFoto.setOnClickListener(v -> imagePicker.launch("image/*"));

        // Carrega os dados do usuário (foto e campos de texto)
        usuarioDAO.carregarDadosUsuario(
                imgPerfil,
                editNome,
                editEmail,
                editTipo,
                this
        );

        // --- Ação do Botão Sair ---
        btnSair.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        });
    }
}
