package com.wise.semester_project.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "storage_areas")
public class StorageArea {
    @PrimaryKey
    @NonNull
    private String areaId;       // 区域ID
    private String name;         // 区域名称
    private String description;  // 区域描述
    private double idealTemperature; // 理想温度
    private double idealHumidity;    // 理想湿度
    private double tempThreshold;    // 温度阈值（正负偏差范围）
    private double humidityThreshold; // 湿度阈值（正负偏差范围）
    private String sensorId;         // 关联的传感器ID

    public StorageArea(@NonNull String areaId, String name, String description, 
                      double idealTemperature, double idealHumidity,
                      double tempThreshold, double humidityThreshold) {
        this.areaId = areaId;
        this.name = name;
        this.description = description;
        this.idealTemperature = idealTemperature;
        this.idealHumidity = idealHumidity;
        this.tempThreshold = tempThreshold;
        this.humidityThreshold = humidityThreshold;
    }

    @NonNull
    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(@NonNull String areaId) {
        this.areaId = areaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getIdealTemperature() {
        return idealTemperature;
    }

    public void setIdealTemperature(double idealTemperature) {
        this.idealTemperature = idealTemperature;
    }

    public double getIdealHumidity() {
        return idealHumidity;
    }

    public void setIdealHumidity(double idealHumidity) {
        this.idealHumidity = idealHumidity;
    }

    public double getTempThreshold() {
        return tempThreshold;
    }

    public void setTempThreshold(double tempThreshold) {
        this.tempThreshold = tempThreshold;
    }

    public double getHumidityThreshold() {
        return humidityThreshold;
    }

    public void setHumidityThreshold(double humidityThreshold) {
        this.humidityThreshold = humidityThreshold;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
    
    // 检查温度是否在安全范围内
    public boolean isTemperatureSafe(double temperature) {
        return Math.abs(temperature - idealTemperature) <= tempThreshold;
    }
    
    // 检查湿度是否在安全范围内
    public boolean isHumiditySafe(double humidity) {
        return Math.abs(humidity - idealHumidity) <= humidityThreshold;
    }
} 