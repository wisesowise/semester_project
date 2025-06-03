package com.wise.semester_project.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.wise.semester_project.model.StorageArea;
import java.util.List;

@Dao
public interface StorageAreaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StorageArea area);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StorageArea> areas);

    @Update
    void update(StorageArea area);

    @Delete
    void delete(StorageArea area);

    @Query("SELECT * FROM storage_areas ORDER BY name ASC")
    LiveData<List<StorageArea>> getAllAreas();

    @Query("SELECT * FROM storage_areas ORDER BY name ASC")
    List<StorageArea> getAllAreasSync();

    @Query("SELECT * FROM storage_areas WHERE areaId = :areaId LIMIT 1")
    LiveData<StorageArea> getAreaById(String areaId);

    @Query("SELECT * FROM storage_areas WHERE areaId = :areaId LIMIT 1")
    StorageArea getAreaByIdSync(String areaId);

    @Query("SELECT * FROM storage_areas WHERE name LIKE :searchQuery")
    LiveData<List<StorageArea>> searchAreas(String searchQuery);

    @Query("SELECT * FROM storage_areas WHERE sensorId = :sensorId")
    LiveData<StorageArea> getAreaBySensorId(String sensorId);

    @Query("UPDATE storage_areas SET sensorId = :sensorId WHERE areaId = :areaId")
    void assignSensorToArea(String areaId, String sensorId);
} 