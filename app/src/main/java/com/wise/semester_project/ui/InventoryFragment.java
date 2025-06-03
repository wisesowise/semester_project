package com.wise.semester_project.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wise.semester_project.R;
import com.wise.semester_project.adapter.InventoryAdapter;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.viewmodel.InventoryViewModel;
import java.util.ArrayList;

public class InventoryFragment extends Fragment {
    private static final String TAG = "InventoryFragment";
    private InventoryViewModel viewModel;
    private RecyclerView recyclerView;
    private InventoryAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate called");
        try {
            viewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);
            Log.d(TAG, "ViewModel initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing ViewModel", e);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        
        try {
            recyclerView = view.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapter = new InventoryAdapter(new ArrayList<>(), viewModel);
            recyclerView.setAdapter(adapter);
            Log.d(TAG, "RecyclerView and Adapter initialized successfully");

            FloatingActionButton fab = view.findViewById(R.id.fab_add_item);
            fab.setOnClickListener(v -> {
                Log.d(TAG, "FAB clicked");
                showAddItemDialog();
            });

            // 观察数据变化
            viewModel.getAllItems().observe(getViewLifecycleOwner(), items -> {
                Log.d(TAG, "Received " + (items != null ? items.size() : 0) + " items from database");
                if (items != null) {
                    adapter.setItems(items);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
        }
        
        return view;
    }

    private void showAddItemDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_name);
        EditText rfidInput = dialogView.findViewById(R.id.edit_rfid);
        EditText quantityInput = dialogView.findViewById(R.id.edit_quantity);
        EditText temperatureInput = dialogView.findViewById(R.id.edit_temperature);
        EditText humidityInput = dialogView.findViewById(R.id.edit_humidity);
        EditText locationInput = dialogView.findViewById(R.id.edit_location);

        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("添加物品")
            .setView(dialogView)
            .setPositiveButton("确定", (dialog, which) -> {
                try {
                    String name = nameInput.getText().toString();
                    String rfid = rfidInput.getText().toString();
                    int quantity = Integer.parseInt(quantityInput.getText().toString());
                    double temperature = Double.parseDouble(temperatureInput.getText().toString());
                    double humidity = Double.parseDouble(humidityInput.getText().toString());
                    String location = locationInput.getText().toString();

                    if (name.isEmpty() || rfid.isEmpty() || location.isEmpty()) {
                        Toast.makeText(getContext(), "请填写所有必填字段", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    InventoryItem newItem = new InventoryItem(name, rfid, quantity, temperature, humidity, location);
                    viewModel.insert(newItem);
                    Toast.makeText(getContext(), "添加成功", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "请输入有效的数字", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("取消", null)
            .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }
} 