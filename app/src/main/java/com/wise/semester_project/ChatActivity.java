package com.wise.semester_project;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.util.Base64;
import android.util.Log;
import okhttp3.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.net.HttpURLConnection;
import io.noties.markwon.Markwon;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final int PERMISSION_REQUEST_CODE = 1;
    private RecyclerView chatRecyclerView;
    private TextInputEditText messageInput;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String API_KEY = "262073245ff746d88b6c981354068d0f.gRC1xHUKFdXy9X2K";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Gson gson;
    private OkHttpClient client;
    private StringBuilder currentResponse = new StringBuilder();
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private Markwon markwon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 初始化Markwon，添加所有需要的插件
        markwon = Markwon.builder(this)
                .usePlugin(HtmlPlugin.create())
                .usePlugin(ImagesPlugin.create())
                .usePlugin(TablePlugin.create(this))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(TaskListPlugin.create(this))
                .build();

        // 检查网络权限
        checkNetworkPermission();

        // 初始化网络管理器
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // 注册网络回调
        registerNetworkCallback();

        gson = new Gson();
        messages = new ArrayList<>();
        
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        
        chatAdapter = new ChatAdapter(messages, markwon);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // 配置OkHttpClient
        client = new OkHttpClient.Builder()
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // 测试网络连接
        testNetworkConnection();

        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());
    }

    private void testNetworkConnection() {
        new Thread(() -> {
            try {
                // 测试DNS解析
                Log.d(TAG, "开始DNS解析测试...");
                InetAddress[] addresses = InetAddress.getAllByName("open.bigmodel.cn");
                for (InetAddress address : addresses) {
                    Log.d(TAG, "DNS解析结果: " + address.getHostAddress());
                }

                // 测试HTTP连接
                Log.d(TAG, "开始HTTP连接测试...");
                URL url = new URL("https://open.bigmodel.cn");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int responseCode = conn.getResponseCode();
                Log.d(TAG, "HTTP连接测试结果: " + responseCode);
                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "网络测试失败: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void checkNetworkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void registerNetworkCallback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(Network network) {
                    Log.d(TAG, "网络已连接");
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "网络已连接", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onLost(Network network) {
                    Log.d(TAG, "网络已断开");
                    runOnUiThread(() -> {
                        Toast.makeText(ChatActivity.this, "网络已断开", Toast.LENGTH_SHORT).show();
                    });
                }
            };

            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }

    private void checkNetworkConnection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        Log.d(TAG, "使用WiFi网络");
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        Log.d(TAG, "使用移动数据网络");
                    }
                }
            }
        }

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            Log.d(TAG, "网络已连接: " + activeNetworkInfo.getTypeName());
        } else {
            Log.e(TAG, "网络未连接");
            Toast.makeText(this, "请检查网络连接", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkConnection();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCallback != null && connectivityManager != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        executorService.shutdown();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // 添加用户消息到聊天界面
        ChatMessage userMessage = new ChatMessage(messageText, true);
        messages.add(userMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
        messageInput.setText("");

        // 添加助手消息
        ChatMessage assistantMessage = new ChatMessage("", false);
        messages.add(assistantMessage);
        int assistantMessagePosition = messages.size() - 1;
        chatAdapter.notifyItemInserted(assistantMessagePosition);
        chatRecyclerView.smoothScrollToPosition(assistantMessagePosition);

        // 重置当前响应
        currentResponse.setLength(0);

        // 准备请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "glm-4-plus");
        requestBody.addProperty("stream", true);

        JsonArray messagesArray = new JsonArray();
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("role", "user");
        messageObject.addProperty("content", messageText);
        messagesArray.add(messageObject);
        requestBody.add("messages", messagesArray);

        Log.d(TAG, "Request body: " + requestBody.toString());

        // 创建请求
        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Host", "open.bigmodel.cn")
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "keep-alive")
                .post(RequestBody.create(
                        MediaType.parse("application/json"),
                        requestBody.toString()))
                .build();

        Log.d(TAG, "Sending request to: " + API_URL);
        Log.d(TAG, "Authorization header: Bearer " + API_KEY);

        // 发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Request failed", e);
                runOnUiThread(() -> {
                    messages.remove(assistantMessage);
                    chatAdapter.notifyItemRemoved(assistantMessagePosition);
                    Toast.makeText(ChatActivity.this, "请求失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "Response code: " + response.code());
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Error response: " + errorBody);
                    runOnUiThread(() -> {
                        messages.remove(assistantMessage);
                        chatAdapter.notifyItemRemoved(assistantMessagePosition);
                        Toast.makeText(ChatActivity.this, "请求失败: " + response.code() + " - " + errorBody, Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().byteStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.d(TAG, "Received line: " + line);
                        if (line.startsWith("data: ")) {
                            String jsonStr = line.substring(6);
                            if (jsonStr.equals("[DONE]")) {
                                Log.d(TAG, "Stream completed");
                                break;
                            }
                            try {
                                JsonObject json = JsonParser.parseString(jsonStr).getAsJsonObject();
                                JsonArray choices = json.getAsJsonArray("choices");
                                if (choices != null && choices.size() > 0) {
                                    JsonObject choice = choices.get(0).getAsJsonObject();
                                    JsonObject delta = choice.getAsJsonObject("delta");
                                    if (delta != null && delta.has("content")) {
                                        String content = delta.get("content").getAsString();
                                        currentResponse.append(content);
                                        final String finalResponse = currentResponse.toString();
                                        runOnUiThread(() -> {
                                            chatAdapter.updateMessage(assistantMessagePosition, finalResponse);
                                            chatRecyclerView.smoothScrollToPosition(assistantMessagePosition);
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing JSON: " + jsonStr, e);
                            }
                        }
                    }
                }
            }
        });
    }
} 