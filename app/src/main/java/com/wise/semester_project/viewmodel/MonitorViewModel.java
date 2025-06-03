package com.wise.semester_project.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.wise.semester_project.dao.SensorDao;
import com.wise.semester_project.dao.StorageAreaDao;
import com.wise.semester_project.database.AppDatabase;
import com.wise.semester_project.model.Sensor;
import com.wise.semester_project.model.StorageArea;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 监控页面的ViewModel，处理传感器数据和仓库区域信息
 */
public class MonitorViewModel extends AndroidViewModel {
    private static final String TAG = "MonitorViewModel";
    
    private final SensorDao sensorDao;
    private final StorageAreaDao areaDao;
    private final LiveData<List<Sensor>> allSensors;
    private final LiveData<List<StorageArea>> allAreas;
    private final ExecutorService executorService;
    
    public MonitorViewModel(@NonNull Application application) {
        super(application);
        
        AppDatabase db = AppDatabase.getInstance(application);
        sensorDao = db.sensorDao();
        areaDao = db.storageAreaDao();
        
        allSensors = sensorDao.getAllSensors();
        allAreas = areaDao.getAllAreas();
        
        executorService = Executors.newSingleThreadExecutor();
    }
    
    /**
     * 获取所有传感器数据
     */
    public LiveData<List<Sensor>> getAllSensorData() {
        return allSensors;
    }
    
    /**
     * 获取所有仓库区域信息
     */
    public LiveData<List<StorageArea>> getAllStorageAreas() {
        return allAreas;
    }
    
    /**
     * 获取指定传感器信息
     */
    public LiveData<Sensor> getSensorById(String sensorId) {
        return sensorDao.getSensorById(sensorId);
    }
    
    /**
     * 获取指定区域信息
     */
    public LiveData<StorageArea> getAreaById(String areaId) {
        return areaDao.getAreaById(areaId);
    }
    
    /**
     * 获取指定区域的传感器信息
     */
    public LiveData<Sensor> getSensorByAreaId(String areaId) {
        // 先获取区域信息，再获取对应的传感器
        MutableLiveData<Sensor> result = new MutableLiveData<>();
        executorService.execute(() -> {
            try {
                StorageArea area = areaDao.getAreaByIdSync(areaId);
                if (area != null && area.getSensorId() != null) {
                    Sensor sensor = sensorDao.getSensorByIdSync(area.getSensorId());
                    result.postValue(sensor);
                } else {
                    result.postValue(null);
                }
            } catch (Exception e) {
                result.postValue(null);
            }
        });
        return result;
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 