package com.example.leitor_qr_code.ui.home.shows.qrcode.perfil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
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

import com.bumptech.glide.Glide;
import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilOrganizadorFragment extends Fragment {

    private ImageView imgPerfil, btnEditarFoto;
    private EditText editNome, editEmail, editTipo;
    private Button btnSalvar, btnSair;
    private ActivityResultLauncher<String> imagePicker;
    private UsuarioDAO usuarioDAO;

    public PerfilOrganizadorFragment() {}

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
        return inflater.inflate(R.layout.fragment_perfil_organizador, container, false);
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
        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Carrega a foto
                        String base64 = doc.getString("photoBase64");
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            Glide.with(this).load(bytes).circleCrop().into(imgPerfil);
                        } else {
                            imgPerfil.setImageResource(R.drawable.icn_perfil_2);
                        }

                        // Carrega e preenche os campos de texto
                        editNome.setText(doc.getString("nome"));
                        editEmail.setText(doc.getString("email"));
                        editTipo.setText(doc.getString("tipo"));
                    }
                });
    }
}
