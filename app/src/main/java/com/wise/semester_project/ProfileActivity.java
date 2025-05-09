package com.wise.semester_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.wise.semester_project.model.User;

public class ProfileActivity extends AppCompatActivity {
    private TextView nameText;
    private TextView emailText;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 获取传递过来的用户信息
        currentUser = (User) getIntent().getSerializableExtra("user");
        if (currentUser == null) {
            // 如果没有用户信息，返回登录界面
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        nameText = findViewById(R.id.nameText);
        emailText = findViewById(R.id.emailText);

        // 显示用户信息
        nameText.setText(currentUser.getName());
        emailText.setText(currentUser.getEmail());

        // 设置退出登录按钮点击事件
        findViewById(R.id.logoutButton).setOnClickListener(v -> logout());
    }

    private void logout() {
        // 清除当前用户信息
        currentUser = null;
        
        // 返回登录界面
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
} 