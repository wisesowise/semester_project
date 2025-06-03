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
import com.wise.semester_project.dao.SensorDao;
import com.wise.semester_project.database.AppDatabase;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.model.Sensor;
import com.wise.semester_project.viewmodel.InventoryViewModel;
import java.util.List;
import java.util.Random;

public class ScanFragment extends Fragment {
    private static final String TAG = "ScanFragment";
    private InventoryViewModel viewModel;
    private Button scanButton;
    private SwitchMaterial nfcModeSwitch;
    private SwitchMaterial checkoutModeSwitch;
    private Random random;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;
    private String[][] techListsArray;
    private List<InventoryItem> inventoryItems; // 保存库存物品列表
    private SensorDao sensorDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        random = new Random();
        viewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        
        // 获取SensorDao实例
        AppDatabase db = AppDatabase.getInstance(requireContext());
        sensorDao = db.sensorDao();
        
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
        checkoutModeSwitch = view.findViewById(R.id.switch_checkout_mode);
        
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
        
        // 设置出库模式开关监听器
        checkoutModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String message = isChecked ? "已切换到出库模式" : "已切换到入库模式";
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
        
        scanButton.setOnClickListener(v -> simulateNfcScan());
        
        // 获取库存物品列表
        viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
            this.inventoryItems = items;
        });
        
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
        boolean isCheckoutMode = checkoutModeSwitch.isChecked();
        
        // 生成温度数据 (20-30°C)
        double temperature = 20 + random.nextDouble() * 10;
        // 生成湿度数据 (40-70%)
        double humidity = 40 + random.nextDouble() * 30;
        
        if (isCheckoutMode) {
            // 出库模式：只能扫描库中已有的商品
            if (inventoryItems == null || inventoryItems.isEmpty()) {
                Toast.makeText(getContext(), "库存为空，无法出库", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 随机选择库存中的一个物品进行出库
            int randomIndex = random.nextInt(inventoryItems.size());
            InventoryItem randomItem = inventoryItems.get(randomIndex);
            String rfidTag = randomItem.getRfidTag();
            
            Toast.makeText(getContext(), "模拟扫描物品: " + randomItem.getName(), Toast.LENGTH_SHORT).show();
            processInventoryData(rfidTag, temperature, humidity);
        } else {
            // 入库模式：可以是新物品或已有物品
            // 随机决定是扫描库中已有的物品还是新物品
            if (inventoryItems != null && !inventoryItems.isEmpty() && random.nextBoolean()) {
                // 50%几率扫描已有物品
                int randomIndex = random.nextInt(inventoryItems.size());
                InventoryItem randomItem = inventoryItems.get(randomIndex);
                String rfidTag = randomItem.getRfidTag();
                
                Toast.makeText(getContext(), "模拟扫描已有物品: " + randomItem.getName(), Toast.LENGTH_SHORT).show();
                processInventoryData(rfidTag, temperature, humidity);
            } else {
                // 50%几率生成新物品
                String rfidTag = String.format("TAG%06d", random.nextInt(1000000));
                Toast.makeText(getContext(), "模拟扫描新物品: " + rfidTag, Toast.LENGTH_SHORT).show();
                processInventoryData(rfidTag, temperature, humidity);
            }
        }
    }

    private void processInventoryData(String rfidTag, double temperature, double humidity) {
        boolean isCheckoutMode = checkoutModeSwitch.isChecked();
        
        // 使用单次观察避免多次更新和LiveData泄漏
        viewModel.getItemByRfid(rfidTag).observe(getViewLifecycleOwner(), existingItem -> {
            // 先移除观察者避免重复处理
            viewModel.getItemByRfid(rfidTag).removeObservers(getViewLifecycleOwner());
            
            if (existingItem != null) {
                if (isCheckoutMode) {
                    // 出库模式：减少库存
                    int currentQuantity = existingItem.getQuantity();
                    // 默认出库1个单位，也可以通过输入框让用户指定数量
                    int checkoutQuantity = 1;
                    
                    if (currentQuantity >= checkoutQuantity) {
                        existingItem.setQuantity(currentQuantity - checkoutQuantity);
                        existingItem.setTemperature(temperature);
                        existingItem.setHumidity(humidity);
                        existingItem.setLastUpdated(System.currentTimeMillis());
                        viewModel.update(existingItem);
                        
                        String message = String.format("出库成功: %s，数量: %d，剩余: %d", 
                            existingItem.getName(), checkoutQuantity, existingItem.getQuantity());
                        showMessage(message);
                        
                        // 如果库存为0，可以选择从数据库中删除该物品
                        if (existingItem.getQuantity() == 0) {
                            viewModel.delete(existingItem);
                            showMessage("物品库存为0，已从仓库移除: " + existingItem.getName());
                        }
                    } else {
                        showMessage("库存不足，无法出库: " + existingItem.getName());
                    }
                } else {
                    // 入库模式：更新现有物品
                    // 使用ViewModel的合并逻辑而不是直接更新
                    InventoryItem newItem = new InventoryItem(
                        existingItem.getName(),
                        existingItem.getRfidTag(),
                        1, // 每次入库增加1
                        temperature,
                        humidity,
                        existingItem.getLocation()
                    );
                    viewModel.incrementQuantity(existingItem, 1);
                    showMessage("入库成功: " + existingItem.getName() + "，当前库存: " + (existingItem.getQuantity() + 1));
                }
            } else {
                if (isCheckoutMode) {
                    // 出库模式下扫描不存在的物品
                    showMessage("物品不存在，无法出库: " + rfidTag);
                } else {
                    // 入库模式：创建新物品
                    // 生成随机存储位置：从A区、B区、C区中随机选择，格式为"X区-xx"
                    String[] areas = {"A区", "B区", "C区"};
                    String randomArea = areas[random.nextInt(areas.length)];
                    String location = String.format("%s-%02d", randomArea, random.nextInt(100));
                    
                    // 从对应区域的传感器获取实时温湿度数据
                    double areaTemp = temperature;  // 默认值，如果无法获取传感器数据则使用传入的温度
                    double areaHumidity = humidity; // 默认值，如果无法获取传感器数据则使用传入的湿度
                    
                    // 使用同步方法获取传感器数据，避免LiveData异步问题
                    try {
                        Sensor areaSensor = sensorDao.getSensorByLocationSync(randomArea);
                        if (areaSensor != null) {
                            areaTemp = areaSensor.getTemperature();
                            areaHumidity = areaSensor.getHumidity();
                            Log.d(TAG, String.format("使用%s传感器数据: %.1f°C, %.1f%%", 
                                randomArea, areaTemp, areaHumidity));
                        } else {
                            Log.w(TAG, "未找到" + randomArea + "的传感器数据，使用默认值");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "获取传感器数据失败", e);
                    }
                    
                    InventoryItem newItem = new InventoryItem(
                        "物品-" + rfidTag.substring(0, Math.min(6, rfidTag.length())),
                        rfidTag,
                        1, // 初始数量为1
                        areaTemp,
                        areaHumidity,
                        location
                    );
                    viewModel.insert(newItem);
                    showMessage("新物品入库成功: " + newItem.getName() + 
                        "，位置: " + location + 
                        String.format("，环境: %.1f°C, %.1f%%", areaTemp, areaHumidity));
                }
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