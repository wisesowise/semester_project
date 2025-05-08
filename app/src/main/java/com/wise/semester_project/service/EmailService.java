package com.wise.semester_project.service;

import android.util.Log;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    private static final String TAG = "EmailService";
    private final String username; // 发件人邮箱
    private final String password; // 邮箱授权码
    private final String smtpHost; // SMTP服务器地址
    private final String smtpPort; // SMTP服务器端口
    private final String recipientEmail; // 收件人邮箱

    public EmailService(String username, String password, String smtpHost, 
                       String smtpPort, String recipientEmail) {
        this.username = username;
        this.password = password;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.recipientEmail = recipientEmail;
    }

    public void sendAlert(String subject, String content) {
        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", smtpPort);

                Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
                message.setSubject(subject);
                message.setText(content);

                Transport.send(message);
                Log.d(TAG, "Alert email sent successfully");
            } catch (MessagingException e) {
                Log.e(TAG, "Failed to send alert email", e);
            }
        }).start();
    }

    // 发送库存异常提醒
    public void sendInventoryAlert(String itemName, int currentQuantity, int threshold) {
        String subject = "库存预警: " + itemName;
        String content = String.format(
            "物品 '%s' 的库存数量 (%d) 已低于警戒值 (%d)。\n" +
            "请及时补充库存。\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            itemName, currentQuantity, threshold
        );
        sendAlert(subject, content);
    }

    // 发送环境异常提醒
    public void sendEnvironmentAlert(String location, double temperature, 
                                   double humidity, double maxTemp, double maxHumidity) {
        String subject = "环境异常警告: " + location;
        String content = String.format(
            "位置 '%s' 的环境数据异常：\n" +
            "当前温度: %.1f°C (最高允许: %.1f°C)\n" +
            "当前湿度: %.1f%% (最高允许: %.1f%%)\n\n" +
            "请及时检查环境控制设备。\n\n" +
            "此邮件由系统自动发送，请勿回复。",
            location, temperature, maxTemp, humidity, maxHumidity
        );
        sendAlert(subject, content);
    }
} 