package com.wise.semester_project.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sensors")
public class Sensor {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String sensorId;      // 传感器唯一标识符
    private String type;          // 传感器类型，如"DHT22"
    private String locationId;    // 关联的仓库区域ID
    private double temperature;   // 当前温度数据
    private double humidity;      // 当前湿度数据
    private long lastUpdate;      // 最后更新时间戳

    public Sensor(String sensorId, String type, String locationId) {
        this.sensorId = sensorId;
        this.type = type;
        this.locationId = locationId;
        this.temperature = 25.0;  // 默认温度
        this.humidity = 50.0;     // 默认湿度
        this.lastUpdate = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
        this.lastUpdate = System.currentTimeMillis();
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
        this.lastUpdate = System.currentTimeMillis();
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    // 更新温湿度数据
    public void updateReadings(double temperature, double humidity) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.lastUpdate = System.currentTimeMillis();
    }
} 