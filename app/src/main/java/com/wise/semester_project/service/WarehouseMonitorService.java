package com.wise.semester_project.service;

import android.content.Context;
import android.util.Log;
import com.wise.semester_project.config.EmailConfig;
import com.wise.semester_project.model.InventoryItem;
import java.util.List;

public class WarehouseMonitorService {
    private static final String TAG = "WarehouseMonitor";
    private final Context context;
    private final EmailService emailService;
    private final InventoryRepository inventoryRepository;

    public WarehouseMonitorService(Context context) {
        this.context = context;
        this.emailService = new EmailService(
            EmailConfig.EMAIL_USERNAME,
            EmailConfig.EMAIL_PASSWORD,
            EmailConfig.SMTP_HOST,
            EmailConfig.SMTP_PORT,
            EmailConfig.RECIPIENT_EMAIL
        );
        this.inventoryRepository = new InventoryRepository(context);
    }

    // 检查库存状态
    public void checkInventoryStatus() {
        List<InventoryItem> items = inventoryRepository.getAllItems();
        for (InventoryItem item : items) {
            if (item.getQuantity() <= EmailConfig.INVENTORY_THRESHOLD) {
                emailService.sendInventoryAlert(
                    item.getName(),
                    item.getQuantity(),
                    EmailConfig.INVENTORY_THRESHOLD
                );
                Log.d(TAG, "Inventory alert sent for item: " + item.getName());
            }
        }
    }

    // 检查环境状态
    public void checkEnvironmentStatus(String location, double temperature, double humidity) {
        if (temperature > EmailConfig.MAX_TEMPERATURE || humidity > EmailConfig.MAX_HUMIDITY) {
            emailService.sendEnvironmentAlert(
                location,
                temperature,
                humidity,
                EmailConfig.MAX_TEMPERATURE,
                EmailConfig.MAX_HUMIDITY
            );
            Log.d(TAG, "Environment alert sent for location: " + location);
        }
    }

    // 启动定期监控
    public void startMonitoring() {
        // 这里可以添加定期检查的逻辑
        // 例如使用Handler或WorkManager来定期执行检查
        checkInventoryStatus();
    }
} 