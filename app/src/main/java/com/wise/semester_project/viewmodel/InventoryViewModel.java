package com.wise.semester_project.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.wise.semester_project.InventoryApplication;
import com.wise.semester_project.database.AppDatabase;
import com.wise.semester_project.database.InventoryDao;
import com.wise.semester_project.model.InventoryItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryViewModel extends AndroidViewModel {
    private static final String TAG = "InventoryViewModel";
    private final InventoryDao inventoryDao;
    private final LiveData<List<InventoryItem>> allItems;
    private final ExecutorService executorService;

    public InventoryViewModel(Application application) {
        super(application);
        Log.d(TAG, "Initializing InventoryViewModel");
        AppDatabase db = AppDatabase.getInstance(application);
        inventoryDao = db.inventoryDao();
        allItems = inventoryDao.getAllItems();
        executorService = Executors.newSingleThreadExecutor();
        
        // 在后台线程中检查并合并重名物品
        executorService.execute(() -> {
            try {
                Log.d(TAG, "Checking for duplicate items on startup");
                List<InventoryItem> items = inventoryDao.getAllItemsSync();
                Map<String, List<InventoryItem>> itemsByLocationAndName = new HashMap<>();
                
                // 按位置和名称分组
                for (InventoryItem item : items) {
                    String key = item.getLocation() + ":" + item.getName();
                    itemsByLocationAndName.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
                }
                
                // 检查每个分组，如果有多个物品则合并
                for (List<InventoryItem> group : itemsByLocationAndName.values()) {
                    if (group.size() > 1) {
                        Log.d(TAG, "Found " + group.size() + " duplicate items for " + 
                              group.get(0).getName() + " in " + group.get(0).getLocation());
                        
                        // 合并所有物品到第一个物品
                        InventoryItem mergedItem = group.get(0);
                        int totalQuantity = mergedItem.getQuantity();
                        double latestTemp = mergedItem.getTemperature();
                        double latestHumidity = mergedItem.getHumidity();
                        
                        // 合并其他物品
                        for (int i = 1; i < group.size(); i++) {
                            InventoryItem item = group.get(i);
                            totalQuantity += item.getQuantity();
                            // 使用最新的温度和湿度
                            if (item.getTemperature() > latestTemp) {
                                latestTemp = item.getTemperature();
                            }
                            if (item.getHumidity() > latestHumidity) {
                                latestHumidity = item.getHumidity();
                            }
                            // 删除重复的物品
                            inventoryDao.delete(item);
                        }
                        
                        // 更新合并后的物品
                        mergedItem.setQuantity(totalQuantity);
                        mergedItem.setTemperature(latestTemp);
                        mergedItem.setHumidity(latestHumidity);
                        inventoryDao.update(mergedItem);
                        
                        Log.d(TAG, "Merged items: " + mergedItem.getName() + 
                              " in " + mergedItem.getLocation() + 
                              ", new quantity: " + totalQuantity);
                    }
                }
                Log.d(TAG, "Finished checking for duplicate items");
            } catch (Exception e) {
                Log.e(TAG, "Error checking for duplicate items", e);
            }
        });
        
        Log.d(TAG, "InventoryViewModel initialized successfully");
    }

    public LiveData<List<InventoryItem>> getAllItems() {
        Log.d(TAG, "Getting all items");
        return allItems;
    }

    public void insert(InventoryItem item) {
        Log.d(TAG, "Inserting item: " + item.getName());
        executorService.execute(() -> {
            try {
                // 检查是否存在相同名称和位置的物品
                InventoryItem existingItem = inventoryDao.findItemByNameAndLocation(item.getName(), item.getLocation());
                if (existingItem != null) {
                    // 如果存在，合并数量
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                    // 更新温度和湿度为最新值
                    existingItem.setTemperature(item.getTemperature());
                    existingItem.setHumidity(item.getHumidity());
                    inventoryDao.update(existingItem);
                    Log.d(TAG, "Updated existing item: " + existingItem.getName() + 
                          ", new quantity: " + existingItem.getQuantity());
                } else {
                    // 如果不存在，插入新物品
                    inventoryDao.insert(item);
                    Log.d(TAG, "Inserted new item: " + item.getName());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting item", e);
            }
        });
    }

    public void update(InventoryItem item) {
        Log.d(TAG, "Updating item: " + item.getName());
        executorService.execute(() -> {
            try {
                inventoryDao.update(item);
                Log.d(TAG, "Item updated successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error updating item", e);
            }
        });
    }

    public void delete(InventoryItem item) {
        Log.d(TAG, "Deleting item: " + item.getName());
        executorService.execute(() -> {
            try {
                inventoryDao.delete(item);
                Log.d(TAG, "Item deleted successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting item", e);
            }
        });
    }

    public LiveData<InventoryItem> getItemById(int id) {
        Log.d(TAG, "Getting item by id: " + id);
        return inventoryDao.getItemById(id);
    }

    public LiveData<InventoryItem> getItemByRfid(String rfidTag) {
        Log.d(TAG, "Getting item by RFID: " + rfidTag);
        return inventoryDao.getItemByRfid(rfidTag);
    }

    public LiveData<List<InventoryItem>> getLowStockItems(int threshold) {
        Log.d(TAG, "Getting low stock items with threshold: " + threshold);
        return inventoryDao.getLowStockItems(threshold);
    }

    public LiveData<List<InventoryItem>> getItemsWithAbnormalConditions(double maxTemp, double maxHumidity) {
        Log.d(TAG, "Getting items with abnormal conditions. Max temp: " + maxTemp + ", max humidity: " + maxHumidity);
        return inventoryDao.getItemsWithAbnormalConditions(maxTemp, maxHumidity);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ViewModel is being cleared");
        executorService.shutdown();
    }
} 