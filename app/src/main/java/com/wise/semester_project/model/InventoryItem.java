package com.wise.semester_project.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_items")
public class InventoryItem {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String rfidTag;
    private int quantity;
    private double temperature;
    private double humidity;
    private String location;
    private long lastUpdated;

    public InventoryItem(String name, String rfidTag, int quantity, double temperature, 
                        double humidity, String location) {
        this.name = name;
        this.rfidTag = rfidTag;
        this.quantity = quantity;
        this.temperature = temperature;
        this.humidity = humidity;
        this.location = location;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRfidTag() { return rfidTag; }
    public void setRfidTag(String rfidTag) { this.rfidTag = rfidTag; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    
    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
} 