package com.wise.semester_project.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.wise.semester_project.R;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.viewmodel.InventoryViewModel;
import java.util.Random;

public class ScanFragment extends Fragment {
    private static final String TAG = "ScanFragment";
    private InventoryViewModel viewModel;
    private Button scanButton;
    private SwitchMaterial nfcModeSwitch;
    private Random random;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        random = new Random();
        viewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        
        // 初始化NFC
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext());
        if (nfcAdapter != null) {
            pendingIntent = PendingIntent.getActivity(requireActivity(), 0,
                new Intent(requireContext(), requireActivity().getClass())
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
            
            IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
            intentFiltersArray = new IntentFilter[] { ndef };
            
            techListsArray = new String[][] { new String[] { MifareClassic.class.getName() } };
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        
        scanButton = view.findViewById(R.id.button_scan);
        nfcModeSwitch = view.findViewById(R.id.switch_nfc_mode);
        
        // 设置NFC模式开关监听器
        nfcModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // 切换到真实NFC模式
                if (nfcAdapter != null && nfcAdapter.isEnabled()) {
                    scanButton.setEnabled(false);
                    Toast.makeText(getContext(), "已切换到NFC扫描模式", Toast.LENGTH_SHORT).show();
                } else {
                    nfcModeSwitch.setChecked(false);
                    Toast.makeText(getContext(), "设备不支持NFC或NFC未启用", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 切换到模拟模式
                scanButton.setEnabled(true);
                Toast.makeText(getContext(), "已切换到模拟模式", Toast.LENGTH_SHORT).show();
            }
        });
        
        scanButton.setOnClickListener(v -> simulateNfcScan());
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcModeSwitch.isChecked()) {
            nfcAdapter.enableForegroundDispatch(requireActivity(), pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(requireActivity());
        }
    }

    public void handleNfcIntent(Intent intent) {
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            if (tag != null) {
                processNfcTag(tag);
            }
        }
    }

    private void processNfcTag(Tag tag) {
        try {
            MifareClassic mifareClassic = MifareClassic.get(tag);
            if (mifareClassic != null) {
                mifareClassic.connect();
                byte[] tagId = tag.getId();
                String rfidTag = bytesToHex(tagId);
                
                // 读取温度和湿度数据(这里假设数据存储在特定扇区)
                byte[] data = mifareClassic.readBlock(4);
                double temperature = (data[0] & 0xFF) / 10.0;
                double humidity = (data[1] & 0xFF) / 10.0;
                
                mifareClassic.close();
                
                // 处理读取到的数据
                processInventoryData(rfidTag, temperature, humidity);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading NFC tag", e);
            Toast.makeText(getContext(), "读取NFC标签失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void simulateNfcScan() {
        // 生成随机的RFID标签号
        String rfidTag = String.format("TAG%06d", random.nextInt(1000000));
        // 生成随机的温度数据 (20-30°C)
        double temperature = 20 + random.nextDouble() * 10;
        // 生成随机的湿度数据 (40-70%)
        double humidity = 40 + random.nextDouble() * 30;
        
        processInventoryData(rfidTag, temperature, humidity);
    }

    private void processInventoryData(String rfidTag, double temperature, double humidity) {
        // 检查是否已存在该标签
        viewModel.getItemByRfid(rfidTag).observe(getViewLifecycleOwner(), existingItem -> {
            if (existingItem != null) {
                // 更新现有物品
                existingItem.setTemperature(temperature);
                existingItem.setHumidity(humidity);
                existingItem.setLastUpdated(System.currentTimeMillis());
                viewModel.update(existingItem);
                showMessage("更新物品: " + existingItem.getName());
            } else {
                // 创建新物品
                InventoryItem newItem = new InventoryItem(
                    "物品-" + rfidTag.substring(0, Math.min(6, rfidTag.length())),
                    rfidTag,
                    random.nextInt(100),
                    temperature,
                    humidity,
                    "位置-" + random.nextInt(10)
                );
                viewModel.insert(newItem);
                showMessage("添加新物品: " + newItem.getName());
            }
        });
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
} 