package com.wise.semester_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wise.semester_project.database.AppDatabase;
import com.wise.semester_project.model.InventoryItem;
import com.wise.semester_project.model.User;
import com.wise.semester_project.ui.InventoryFragment;
import com.wise.semester_project.ui.ScanFragment;
import com.wise.semester_project.ui.MonitorFragment;
import com.wise.semester_project.ui.ProfileFragment;
import com.wise.semester_project.service.SensorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "setContentView completed");

            // 获取当前用户信息
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("user")) {
                currentUser = (User) intent.getSerializableExtra("user");
                Log.d(TAG, "User info received: " + (currentUser != null ? currentUser.getUsername() : "null"));
            } else {
                Log.d(TAG, "No user info in intent");
                // 如果没有用户信息，返回登录界面
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }

            // 初始化数据库
            try {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                Log.d(TAG, "Database initialized successfully");
                
                // 插入测试数据
                insertTestData(db);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing database", e);
            }
            
            // 启动传感器服务
            startSensorService();

            bottomNavigationView = findViewById(R.id.bottom_navigation);
            if (bottomNavigationView == null) {
                Log.e(TAG, "bottomNavigationView is null");
                return;
            }
            Log.d(TAG, "bottomNavigationView found");

            bottomNavigationView.setOnItemSelectedListener(item -> {
                Log.d(TAG, "Navigation item selected: " + item.getItemId());
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_inventory) {
                    selectedFragment = new InventoryFragment();
                } else if (itemId == R.id.nav_scan) {
                    selectedFragment = new ScanFragment();
                } else if (itemId == R.id.nav_monitor) {
                    selectedFragment = new MonitorFragment();
                } else if (itemId == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                } else if (itemId == R.id.nav_chat) {
                    // 启动ChatActivity
                    Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                    startActivity(chatIntent);
                    return true;
                }

                if (selectedFragment != null) {
                    Log.d(TAG, "Replacing fragment with: " + selectedFragment.getClass().getSimpleName());
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                    return true;
                }
                return false;
            });

            // Set default fragment
            if (savedInstanceState == null) {
                Log.d(TAG, "Setting default fragment (InventoryFragment)");
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new InventoryFragment())
                    .commit();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 更新当前Intent
        // 将NFC意图传递给当前活动的Fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ScanFragment) {
            ((ScanFragment) currentFragment).handleNfcIntent(intent);
        }
    }

    private void insertTestData(AppDatabase db) {
        executorService.execute(() -> {
            try {
                // 检查是否已有数据
                int count = db.inventoryDao().getAllItems().getValue() != null ? 
                    db.inventoryDao().getAllItems().getValue().size() : 0;
                
                if (count == 0) {
                    Log.d(TAG, "Inserting test data");
                    // 插入测试数据
                    InventoryItem item1 = new InventoryItem("测试物品1", "RFID001", 100, 25.0, 60.0, "A区-01");
                    InventoryItem item2 = new InventoryItem("测试物品2", "RFID002", 50, 26.0, 65.0, "B区-02");
                    db.inventoryDao().insert(item1);
                    db.inventoryDao().insert(item2);
                    Log.d(TAG, "Test data inserted successfully");
                } else {
                    Log.d(TAG, "Database already contains " + count + " items");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error inserting test data", e);
            }
        });
    }

    /**
     * 启动传感器服务
     */
    private void startSensorService() {
        try {
            Intent sensorServiceIntent = new Intent(this, SensorService.class);
            startService(sensorServiceIntent);
            Log.d(TAG, "Sensor service started");
        } catch (Exception e) {
            Log.e(TAG, "Error starting sensor service", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        executorService.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            // 跳转到个人资料页面
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("user", currentUser);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
