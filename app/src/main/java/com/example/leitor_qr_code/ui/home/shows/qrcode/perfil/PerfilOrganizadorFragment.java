package com.example.leitor_qr_code.ui.home.shows.qrcode.perfil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                            // Passa o bitmap para o DAO
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

        imgPerfil = view.findViewById(R.id.imgPerfil);
        btnEditarFoto = view.findViewById(R.id.btnEditarFoto);

        btnEditarFoto.setOnClickListener(v -> imagePicker.launch("image/*"));

        // Carrega a foto inicial do perfil
        carregarFotoPerfil();
    }

    private void carregarFotoPerfil() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String base64 = doc.getString("photoBase64");
                        if (base64 != null && !base64.isEmpty()) {
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            // Aplica a transformação para deixar a imagem redonda
                            Glide.with(this).load(bytes).circleCrop().into(imgPerfil);
                        } else {
                            // Imagem padrão se não houver foto
                            imgPerfil.setImageResource(R.drawable.icn_perfil_2);
                        }
                    }
                });
    }
}
