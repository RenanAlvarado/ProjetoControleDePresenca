package com.example.leitor_qr_code.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothPrinterHelper {

    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Context context;

    public BluetoothPrinterHelper(Context context) {
        this.context = context;
    }

    public void printText(String textToPrint) {
        new Thread(() -> {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                showToast("Dispositivo não suporta Bluetooth");
                return;
            }
            if (!bluetoothAdapter.isEnabled()) {
                showToast("Por favor, ative o Bluetooth");
                return;
            }

            BluetoothDevice printerDevice = findPrinter(bluetoothAdapter);
            if (printerDevice == null) {
                showToast("Nenhuma impressora Bluetooth encontrada");
                return;
            }

            try (BluetoothSocket socket = printerDevice.createRfcommSocketToServiceRecord(PRINTER_UUID)) {
                socket.connect();
                if (socket.isConnected()) {
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(new byte[]{0x1B, 0x40}); // Reset
                    outputStream.write(new byte[]{0x1B, 0x61, 1}); // Alinhar ao centro

                    // CORREÇÃO: Converte o texto para o formato que a impressora entende
                    outputStream.write(textToPrint.getBytes("CP860"));

                    outputStream.write("\n\n\n".getBytes()); // Espaço extra no final
                    outputStream.flush();
                    showToast("Comprovante enviado para impressão");
                }
            } catch (Exception e) { // Captura exceções de IO e de codificação
                e.printStackTrace();
                showToast("Erro ao conectar ou imprimir: " + e.getMessage());
            }
        }).start();
    }

    private BluetoothDevice findPrinter(BluetoothAdapter adapter) {
        try {
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                // Tenta encontrar uma impressora pelo nome. Ajuste se o nome da sua for diferente.
                if (device.getName().toLowerCase().contains("printer") || device.getName().toLowerCase().contains("mp")) {
                    return device;
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            showToast("Permissão de Bluetooth não concedida");
        }
        return null;
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        });
    }
}
