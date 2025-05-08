package com.wise.semester_project.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.wise.semester_project.R;
import com.wise.semester_project.model.InventoryItem;

public class InventoryAdapter extends ListAdapter<InventoryItem, InventoryAdapter.InventoryViewHolder> {

    public InventoryAdapter() {
        super(new DiffUtil.ItemCallback<InventoryItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull InventoryItem oldItem, @NonNull InventoryItem newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull InventoryItem oldItem, @NonNull InventoryItem newItem) {
                return oldItem.getName().equals(newItem.getName()) &&
                       oldItem.getQuantity() == newItem.getQuantity() &&
                       oldItem.getTemperature() == newItem.getTemperature() &&
                       oldItem.getHumidity() == newItem.getHumidity();
            }
        });
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = getItem(position);
        holder.bind(item);
    }

    static class InventoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameText;
        private final TextView quantityText;
        private final TextView locationText;
        private final TextView conditionsText;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_name);
            quantityText = itemView.findViewById(R.id.text_quantity);
            locationText = itemView.findViewById(R.id.text_location);
            conditionsText = itemView.findViewById(R.id.text_conditions);
        }

        public void bind(InventoryItem item) {
            nameText.setText(item.getName());
            quantityText.setText(String.format("数量: %d", item.getQuantity()));
            locationText.setText(String.format("位置: %s", item.getLocation()));
            conditionsText.setText(String.format("温度: %.1f°C, 湿度: %.1f%%", 
                item.getTemperature(), item.getHumidity()));
        }
    }
} 