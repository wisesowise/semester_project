package com.wise.semester_project.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.wise.semester_project.R;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.viewmodel.InventoryViewModel;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private List<InventoryItem> items;
    private InventoryViewModel viewModel;

    public InventoryAdapter(List<InventoryItem> items, InventoryViewModel viewModel) {
        this.items = items != null ? items : new ArrayList<>();
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.nameText.setText(item.getName());
        holder.rfidText.setText(item.getRfidTag());
        holder.quantityText.setText(String.valueOf(item.getQuantity()));
        holder.temperatureText.setText(String.format("%.1f°C", item.getTemperature()));
        holder.humidityText.setText(String.format("%.1f%%", item.getHumidity()));
        holder.locationText.setText(item.getLocation());
        
        holder.editButton.setIcon(holder.itemView.getContext().getDrawable(R.drawable.ic_edit));
        holder.editButton.setOnClickListener(v -> showEditQuantityDialog(holder, item));
    }

    private void showEditQuantityDialog(InventoryViewHolder holder, InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("编辑数量");
        
        // 创建输入框
        final EditText input = new EditText(holder.itemView.getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(item.getQuantity()));
        builder.setView(input);
        
        builder.setPositiveButton("确定", (dialog, which) -> {
            try {
                int newQuantity = Integer.parseInt(input.getText().toString());
                if (newQuantity >= 0) {
                    item.setQuantity(newQuantity);
                    viewModel.update(item);
                    holder.quantityText.setText(String.valueOf(newQuantity));
                    
                    // 如果数量为0，询问是否要删除物品
                    if (newQuantity == 0) {
                        showDeleteConfirmDialog(holder, item);
                    }
                } else {
                    Toast.makeText(holder.itemView.getContext(), "数量不能为负数", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(holder.itemView.getContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }
    
    private void showDeleteConfirmDialog(InventoryViewHolder holder, InventoryItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
        builder.setTitle("删除确认");
        builder.setMessage("物品数量为0，是否从库存中删除？");
        
        builder.setPositiveButton("是", (dialog, which) -> {
            viewModel.delete(item);
            // 适配器会通过ViewModel的LiveData观察到变化并自动更新
        });
        
        builder.setNegativeButton("否", (dialog, which) -> dialog.cancel());
        
        builder.show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<InventoryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView rfidText;
        TextView quantityText;
        TextView temperatureText;
        TextView humidityText;
        TextView locationText;
        MaterialButton editButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            rfidText = itemView.findViewById(R.id.rfidText);
            quantityText = itemView.findViewById(R.id.quantityText);
            temperatureText = itemView.findViewById(R.id.temperatureText);
            humidityText = itemView.findViewById(R.id.humidityText);
            locationText = itemView.findViewById(R.id.locationText);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }
} 