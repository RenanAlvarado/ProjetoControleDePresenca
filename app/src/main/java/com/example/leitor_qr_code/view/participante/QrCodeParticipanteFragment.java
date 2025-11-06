package com.example.leitor_qr_code.view.participante;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.leitor_qr_code.R;
import com.example.leitor_qr_code.dao.UsuarioDAO;
import com.example.leitor_qr_code.util.PrinterUtils;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class QrCodeParticipanteFragment extends Fragment {

    private ImageView imgQrCode;
    private TextView textNomeUsuario;
    private TextView textEmailUsuario;
    private Button btnImprimirQr;
    private UsuarioDAO usuarioDAO;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice printerDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allPermissionsGranted = true;
                    for (Boolean granted : permissions.values()) {
                        if (!granted) {
                            allPermissionsGranted = false;
                            break;
                        }
                    }
                    if (allPermissionsGranted) {
                        procederComImpressao();
                    } else {
                        Toast.makeText(getContext(), "Permissão de Bluetooth é necessária para imprimir.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qr_code_participante, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgQrCode = view.findViewById(R.id.imgQrParticipante);
        textNomeUsuario = view.findViewById(R.id.txtNomeParticipante);
        textEmailUsuario = view.findViewById(R.id.txtEmailParticipante);
        btnImprimirQr = view.findViewById(R.id.btnImprimirQr);
        Button btnAtualizarQr = view.findViewById(R.id.btnAtualizarQr);
        usuarioDAO = new UsuarioDAO();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        gerarQrCodeEPreencherDados();
        btnImprimirQr.setOnClickListener(v -> iniciarProcessoDeImpressao());
        btnAtualizarQr.setOnClickListener(v -> {
            gerarQrCodeEPreencherDados();
            Toast.makeText(getContext(), "QR Code atualizado.", Toast.LENGTH_SHORT).show();
        });
    }

    private void iniciarProcessoDeImpressao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN});
                return;
            }
        }
        procederComImpressao();
    }

    private void procederComImpressao() {
        if (bluetoothAdapter == null) {
            Toast.makeText(getContext(), "Este dispositivo não suporta Bluetooth.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Por favor, ative o Bluetooth.", Toast.LENGTH_LONG).show();
            return;
        }

        printerDevice = null;
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            iniciarProcessoDeImpressao();
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.IMAGING) {
                    printerDevice = device;
                    break;
                }
            }
        }

        if (printerDevice != null) {
            conectarEImprimir(printerDevice);
        } else {
            Toast.makeText(getContext(), "Nenhuma impressora pareada foi encontrada. Por favor, pareie sua impressora térmica nas configurações de Bluetooth do celular.", Toast.LENGTH_LONG).show();
        }
    }

    private void conectarEImprimir(final BluetoothDevice device) {
        Toast.makeText(getContext(), "Conectando à impressora...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();

                String nome = textNomeUsuario.getText().toString();
                String email = textEmailUsuario.getText().toString();
                Bitmap qrCodeBitmap = ((BitmapDrawable) imgQrCode.getDrawable()).getBitmap();

                byte[] init = {0x1B, 0x40};
                byte[] alignCenter = {0x1B, 0x61, 1};
                byte[] lineFeed = {0x0A};

                outputStream.write(init);
                outputStream.write(alignCenter);

                try {
                    outputStream.write(nome.getBytes("GBK"));
                    outputStream.write(lineFeed);
                    outputStream.write(email.getBytes("GBK"));
                } catch (UnsupportedEncodingException e) {
                    outputStream.write(nome.getBytes());
                    outputStream.write(lineFeed);
                    outputStream.write(email.getBytes());
                }

                outputStream.write(lineFeed);
                outputStream.write(lineFeed);

                // **LÓGICA DE FATIAMENTO APLICADA AQUI**
                List<byte[]> slices = PrinterUtils.getSlicedBitmap(qrCodeBitmap, 24);
                if (slices != null) {
                    for (byte[] slice : slices) {
                        outputStream.write(slice);
                        // Pausa crucial para o buffer da impressora
                        Thread.sleep(80);
                    }
                }

                outputStream.write(lineFeed);
                outputStream.write(lineFeed);
                outputStream.write(lineFeed);

                outputStream.flush();

                if(getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Impressão enviada com sucesso!", Toast.LENGTH_LONG).show());
                }

            } catch (IOException | InterruptedException e) { // Adiciona InterruptedException
                e.printStackTrace();
                if(getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Falha na conexão com a impressora.", Toast.LENGTH_LONG).show());
                }
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                    if (bluetoothSocket != null) bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void gerarQrCodeEPreencherDados() {
        usuarioDAO.gerarDadosQrCode(new UsuarioDAO.QrCodeDataCallback() {
            @Override
            public void onDataReady(String nome, String email, String jsonData) {
                if (getContext() == null || !isAdded()) return;
                textNomeUsuario.setText(nome);
                textEmailUsuario.setText(email);
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    // O tamanho de 300x300 é um bom equilíbrio
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(jsonData, BarcodeFormat.QR_CODE, 300, 300);
                    imgQrCode.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Erro ao gerar imagem do QR Code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure() {
                if (getContext() == null || !isAdded()) return;
                Toast.makeText(getContext(), "Falha ao buscar dados para o QR Code.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}