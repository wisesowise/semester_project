package com.wise.semester_project.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.Nullable;
import com.wise.semester_project.dao.InventoryDao;
import com.wise.semester_project.dao.SensorDao;
import com.wise.semester_project.dao.StorageAreaDao;
import com.wise.semester_project.database.AppDatabase;
import com.wise.semester_project.model.Sensor;
import com.wise.semester_project.model.StorageArea;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 传感器服务，模拟DHT22传感器读取温湿度数据，并更新对应区域物品的环境数据
 */
public class SensorService extends Service {
    private static final String TAG = "SensorService";
    private final IBinder binder = new LocalBinder();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    
    // 传感器数据更新间隔（毫秒）
    private static final long UPDATE_INTERVAL_MS = 30000; // 30秒更新一次
    
    // 数据访问对象
    private SensorDao sensorDao;
    private StorageAreaDao storageAreaDao;
    private InventoryDao inventoryDao;
    
    // 是否正在运行模拟
    private boolean isRunning = false;
    
    // 模拟传感器波动范围
    private static final double TEMP_FLUCTUATION = 2.0; // 温度波动范围（摄氏度）
    private static final double HUMIDITY_FLUCTUATION = 5.0; // 湿度波动范围（百分比）

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SensorService created");
        
        // 获取数据访问对象
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        sensorDao = db.sensorDao();
        storageAreaDao = db.storageAreaDao();
        inventoryDao = db.inventoryDao();
        
        // 初始化默认传感器和区域
        initDefaultSensorsAndAreas();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SensorService started");
        startSensorSimulation();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "SensorService destroyed");
        stopSensorSimulation();
        executorService.shutdown();
        super.onDestroy();
    }

    /**
     * 启动传感器模拟
     */
    public void startSensorSimulation() {
        if (isRunning) return;
        
        isRunning = true;
        Log.d(TAG, "Starting sensor simulation");
        
        // 循环更新传感器数据
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isRunning) return;
                
                // 更新所有传感器数据
                updateAllSensors();
                
                // 安排下一次更新
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        });
    }
    
    /**
     * 停止传感器模拟
     */
    public void stopSensorSimulation() {
        Log.d(TAG, "Stopping sensor simulation");
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 初始化默认传感器和仓库区域
     */
    private void initDefaultSensorsAndAreas() {
        executorService.execute(() -> {
            try {
                // 检查是否已有区域数据
                List<StorageArea> areas = storageAreaDao.getAllAreasSync();
                if (areas == null || areas.isEmpty()) {
                    // 创建默认区域
                    StorageArea areaA = new StorageArea(
                        "A区", "A区 - 常温存储区", "普通物品存储区域", 
                        25.0, 50.0, 5.0, 10.0);
                    
                    StorageArea areaB = new StorageArea(
                        "B区", "B区 - 低温存储区", "温度敏感物品存储区域", 
                        18.0, 40.0, 3.0, 5.0);
                        
                    StorageArea areaC = new StorageArea(
                        "C区", "C区 - 高温存储区", "高温物品存储区域", 
                        30.0, 35.0, 2.0, 5.0);
                    
                    // 创建传感器
                    Sensor sensorA = new Sensor("DHT22-001", "DHT22", "A区");
                    sensorA.updateReadings(25.0, 50.0);
                    
                    Sensor sensorB = new Sensor("DHT22-002", "DHT22", "B区");
                    sensorB.updateReadings(18.0, 40.0);
                    
                    Sensor sensorC = new Sensor("DHT22-003", "DHT22", "C区");
                    sensorC.updateReadings(30.0, 35.0);
                    
                    // 关联传感器与区域
                    areaA.setSensorId(sensorA.getSensorId());
                    areaB.setSensorId(sensorB.getSensorId());
                    areaC.setSensorId(sensorC.getSensorId());
                    
                    // 保存到数据库
                    storageAreaDao.insert(areaA);
                    storageAreaDao.insert(areaB);
                    storageAreaDao.insert(areaC);
                    
                    sensorDao.insert(sensorA);
                    sensorDao.insert(sensorB);
                    sensorDao.insert(sensorC);
                    
                    Log.d(TAG, "Default sensors and areas created");
                } else {
                    Log.d(TAG, "Areas already exist in database, skipping initialization");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error initializing default sensors and areas", e);
            }
        });
    }

    /**
     * 更新所有传感器数据并同步到库存
     */
    private void updateAllSensors() {
        executorService.execute(() -> {
            try {
                // 获取所有区域
                List<StorageArea> areas = storageAreaDao.getAllAreasSync();
                if (areas != null) {
                    for (StorageArea area : areas) {
                        // 获取区域对应的传感器
                        Sensor sensor = sensorDao.getSensorByIdSync(area.getSensorId());
                        if (sensor != null) {
                            // 模拟新的传感器读数（基于理想值的随机波动）
                            double newTemp = area.getIdealTemperature() + 
                                (random.nextDouble() * TEMP_FLUCTUATION * 2 - TEMP_FLUCTUATION);
                                
                            double newHumidity = area.getIdealHumidity() + 
                                (random.nextDouble() * HUMIDITY_FLUCTUATION * 2 - HUMIDITY_FLUCTUATION);
                                
                            // 更新传感器数据
                            sensor.updateReadings(newTemp, newHumidity);
                            sensorDao.update(sensor);
                            
                            Log.d(TAG, String.format("Updated sensor %s in %s: %.1f°C, %.1f%%", 
                                sensor.getSensorId(), area.getName(), newTemp, newHumidity));
                            
                            // 更新该区域内所有物品的环境数据
                            long timestamp = System.currentTimeMillis();
                            inventoryDao.updateEnvironmentDataByLocation(
                                area.getAreaId() + "%", newTemp, newHumidity, timestamp);
                            
                            // 检查是否环境条件超出安全范围
                            if (!area.isTemperatureSafe(newTemp) || !area.isHumiditySafe(newHumidity)) {
                                Log.w(TAG, String.format("ALERT: %s environmental conditions out of range! Temp: %.1f°C, Humidity: %.1f%%",
                                    area.getName(), newTemp, newHumidity));
                                // 这里可以发送通知或警报
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating sensor data", e);
            }
        });
    }

    /**
     * 服务绑定本地接口
     */
    public class LocalBinder extends Binder {
        public SensorService getService() {
            return SensorService.this;
        }
    }
} 