package com.wise.semester_project.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.wise.semester_project.R;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.viewmodel.InventoryViewModel;
import java.util.List;

public class MonitorFragment extends Fragment {
    private InventoryViewModel viewModel;
    private TextView textAbnormalItems;
    private TextView textLowStockItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);
        
        viewModel = new ViewModelProvider(this).get(InventoryViewModel.class);
        
        textAbnormalItems = view.findViewById(R.id.text_abnormal_items);
        textLowStockItems = view.findViewById(R.id.text_low_stock_items);
        
        // 监测异常环境条件
        viewModel.getItemsWithAbnormalConditions(28.0, 70.0)
            .observe(getViewLifecycleOwner(), this::updateAbnormalItems);
            
        // 监测低库存
        viewModel.getLowStockItems(10)
            .observe(getViewLifecycleOwner(), this::updateLowStockItems);
            
        return view;
    }

    private void updateAbnormalItems(List<InventoryItem> items) {
        StringBuilder sb = new StringBuilder();
        if (items.isEmpty()) {
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
        if (items.isEmpty()) {
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
} 