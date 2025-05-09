package com.wise.semester_project.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.wise.semester_project.model.InventoryItem;
import java.util.List;

@Dao
public interface InventoryDao {
    @Insert
    void insert(InventoryItem item);

    @Update
    void update(InventoryItem item);

    @Delete
    void delete(InventoryItem item);

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    LiveData<List<InventoryItem>> getAllItems();

    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    List<InventoryItem> getAllItemsSync();

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    LiveData<InventoryItem> getItemById(int id);

    @Query("SELECT * FROM inventory_items WHERE rfidTag = :rfidTag")
    LiveData<InventoryItem> getItemByRfid(String rfidTag);

    @Query("SELECT * FROM inventory_items WHERE quantity < :threshold")
    LiveData<List<InventoryItem>> getLowStockItems(int threshold);

    @Query("SELECT * FROM inventory_items WHERE temperature > :maxTemp OR humidity > :maxHumidity")
    LiveData<List<InventoryItem>> getItemsWithAbnormalConditions(double maxTemp, double maxHumidity);

    @Query("SELECT * FROM inventory_items WHERE name = :name AND location = :location LIMIT 1")
    InventoryItem findItemByNameAndLocation(String name, String location);
} 