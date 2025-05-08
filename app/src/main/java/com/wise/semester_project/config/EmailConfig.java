package com.wise.semester_project.config;

public class EmailConfig {
    // 邮箱配置
    public static final String EMAIL_USERNAME = "your-email@gmail.com"; // 发件人邮箱
    public static final String EMAIL_PASSWORD = "your-app-password"; // 邮箱授权码
    public static final String SMTP_HOST = "smtp.gmail.com"; // SMTP服务器地址
    public static final String SMTP_PORT = "587"; // SMTP服务器端口
    public static final String RECIPIENT_EMAIL = "recipient@example.com"; // 收件人邮箱

    // 环境监控阈值
    public static final double MAX_TEMPERATURE = 30.0; // 最高允许温度（摄氏度）
    public static final double MAX_HUMIDITY = 80.0; // 最高允许湿度（百分比）

    // 库存预警阈值
    public static final int INVENTORY_THRESHOLD = 10; // 库存预警阈值
} 