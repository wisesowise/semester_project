package com.wise.semester_project.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.wise.semester_project.model.Sensor;
import java.util.List;

@Dao
public interface SensorDao {
    @Insert
    void insert(Sensor sensor);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Sensor> sensors);

    @Update
    void update(Sensor sensor);

    @Delete
    void delete(Sensor sensor);

    @Query("SELECT * FROM sensors ORDER BY locationId ASC")
    LiveData<List<Sensor>> getAllSensors();

    @Query("SELECT * FROM sensors WHERE sensorId = :sensorId LIMIT 1")
    LiveData<Sensor> getSensorById(String sensorId);

    @Query("SELECT * FROM sensors WHERE sensorId = :sensorId LIMIT 1")
    Sensor getSensorByIdSync(String sensorId);

    @Query("SELECT * FROM sensors WHERE locationId = :locationId LIMIT 1")
    LiveData<Sensor> getSensorByLocation(String locationId);

    @Query("SELECT * FROM sensors WHERE locationId = :locationId LIMIT 1")
    Sensor getSensorByLocationSync(String locationId);
} 