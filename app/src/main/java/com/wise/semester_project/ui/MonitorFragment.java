package com.wise.semester_project.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.wise.semester_project.R;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.model.Sensor;
import com.wise.semester_project.model.StorageArea;
import com.wise.semester_project.service.SensorService;
import com.wise.semester_project.viewmodel.InventoryViewModel;
import com.wise.semester_project.viewmodel.MonitorViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MonitorFragment extends Fragment {
    private static final String TAG = "MonitorFragment";
    
    private InventoryViewModel inventoryViewModel;
    private MonitorViewModel monitorViewModel;
    private TextView textAbnormalItems;
    private TextView textLowStockItems;
    private RecyclerView recyclerViewSensors;
    private SensorListAdapter sensorAdapter;
    
    // 传感器服务
    private SensorService sensorService;
    private boolean isBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SensorService.LocalBinder binder = (SensorService.LocalBinder) service;
            sensorService = binder.getService();
            isBound = true;
            Log.d(TAG, "Connected to SensorService");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            sensorService = null;
            isBound = false;
            Log.d(TAG, "Disconnected from SensorService");
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 绑定传感器服务
        Intent intent = new Intent(getActivity(), SensorService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        // 初始化ViewModel
        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
        monitorViewModel = new ViewModelProvider(this).get(MonitorViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);
        
        textAbnormalItems = view.findViewById(R.id.text_abnormal_items);
        textLowStockItems = view.findViewById(R.id.text_low_stock_items);
        
        // 初始化传感器列表
        recyclerViewSensors = view.findViewById(R.id.recycler_view_sensors);
        recyclerViewSensors.setLayoutManager(new LinearLayoutManager(getContext()));
        sensorAdapter = new SensorListAdapter();
        recyclerViewSensors.setAdapter(sensorAdapter);
        
        // 监测异常环境条件
        inventoryViewModel.getItemsWithAbnormalConditions(28.0, 70.0)
            .observe(getViewLifecycleOwner(), this::updateAbnormalItems);
            
        // 监测低库存
        inventoryViewModel.getLowStockItems(10)
            .observe(getViewLifecycleOwner(), this::updateLowStockItems);
            
        // 监测传感器数据
        monitorViewModel.getAllSensorData().observe(getViewLifecycleOwner(), sensors -> {
            if (sensors != null) {
                sensorAdapter.setSensorList(sensors);
            }
        });
            
        return view;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }

    private void updateAbnormalItems(List<InventoryItem> items) {
        StringBuilder sb = new StringBuilder();
        if (items == null || items.isEmpty()) {
            sb.append("当前没有异常环境条件");
        } else {
            sb.append("异常环境条件物品：\n");
            for (InventoryItem item : items) {
                sb.append(String.format("- %s: 温度%.1f°C, 湿度%.1f%%\n",
                    item.getName(), item.getTemperature(), item.getHumidity()));
            }
        }
        textAbnormalItems.setText(sb.toString());
    }

    private void updateLowStockItems(List<InventoryItem> items) {
        StringBuilder sb = new StringBuilder();
        if (items == null || items.isEmpty()) {
            sb.append("当前没有低库存物品");
        } else {
            sb.append("低库存物品：\n");
            for (InventoryItem item : items) {
                sb.append(String.format("- %s: 剩余%d个\n",
                    item.getName(), item.getQuantity()));
            }
        }
        textLowStockItems.setText(sb.toString());
    }
    
    /**
     * 传感器列表适配器
     */
    private class SensorListAdapter extends RecyclerView.Adapter<SensorViewHolder> {
        private List<Sensor> sensorList;
        private List<StorageArea> areaList;
        
        @NonNull
        @Override
        public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sensor, parent, false);
            return new SensorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
            if (sensorList != null && position < sensorList.size()) {
                Sensor sensor = sensorList.get(position);
                
                // 查找对应区域
                StorageArea area = null;
                if (areaList != null) {
                    for (StorageArea a : areaList) {
                        if (a.getSensorId() != null && a.getSensorId().equals(sensor.getSensorId())) {
                            area = a;
                            break;
                        }
                    }
                }
                
                holder.bind(sensor, area);
            }
        }

        @Override
        public int getItemCount() {
            return sensorList != null ? sensorList.size() : 0;
        }
        
        public void setSensorList(List<Sensor> sensorList) {
            this.sensorList = sensorList;
            
            // 获取区域信息
            monitorViewModel.getAllStorageAreas().observe(getViewLifecycleOwner(), areas -> {
                this.areaList = areas;
                notifyDataSetChanged();
            });
        }
    }
    
    /**
     * 传感器ViewHolder
     */
    private static class SensorViewHolder extends RecyclerView.ViewHolder {
        private final TextView textSensorName;
        private final TextView textTemperature;
        private final TextView textHumidity;
        private final TextView textLocation;
        private final TextView textUpdateTime;
        private final TextView textStatus;
        
        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            textSensorName = itemView.findViewById(R.id.text_sensor_name);
            textTemperature = itemView.findViewById(R.id.text_temperature);
            textHumidity = itemView.findViewById(R.id.text_humidity);
            textLocation = itemView.findViewById(R.id.text_location);
            textUpdateTime = itemView.findViewById(R.id.text_update_time);
            textStatus = itemView.findViewById(R.id.text_status);
        }
        
        public void bind(Sensor sensor, StorageArea area) {
            if (sensor != null) {
                // 显示传感器ID和类型
                textSensorName.setText(String.format("%s (%s)", 
                    sensor.getSensorId(), sensor.getType()));
                
                // 显示温湿度数据
                textTemperature.setText(String.format("温度: %.1f°C", sensor.getTemperature()));
                textHumidity.setText(String.format("湿度: %.1f%%", sensor.getHumidity()));
                
                // 显示位置信息
                if (area != null) {
                    textLocation.setText(String.format("位置: %s - %s", 
                        area.getAreaId(), area.getName()));
                    
                    // 检查环境是否在安全范围内
                    boolean isTempSafe = area.isTemperatureSafe(sensor.getTemperature());
                    boolean isHumiditySafe = area.isHumiditySafe(sensor.getHumidity());
                    
                    if (isTempSafe && isHumiditySafe) {
                        textStatus.setText("状态: 正常");
                        textStatus.setTextColor(0xFF4CAF50); // 绿色
                    } else {
                        textStatus.setText("状态: 异常");
                        textStatus.setTextColor(0xFFF44336); // 红色
                    }
                } else {
                    textLocation.setText("位置: " + sensor.getLocationId());
                    textStatus.setText("状态: 未知");
                    textStatus.setTextColor(0xFF9E9E9E); // 灰色
                }
                
                // 显示更新时间
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String updateTime = sdf.format(new Date(sensor.getLastUpdate()));
                textUpdateTime.setText("更新时间: " + updateTime);
            }
        }
    }
} 