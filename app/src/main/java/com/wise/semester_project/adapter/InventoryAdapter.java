package com.wise.semester_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.wise.semester_project.R;
import com.wise.semester_project.model.InventoryItem;
import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {
    private List<InventoryItem> items;

    public InventoryAdapter(List<InventoryItem> items) {
        this.items = items != null ? items : new ArrayList<>();
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

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            rfidText = itemView.findViewById(R.id.rfidText);
            quantityText = itemView.findViewById(R.id.quantityText);
            temperatureText = itemView.findViewById(R.id.temperatureText);
            humidityText = itemView.findViewById(R.id.humidityText);
            locationText = itemView.findViewById(R.id.locationText);
        }
    }
} 