package com.wise.semester_project.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.wise.semester_project.dao.UserDao;
import com.wise.semester_project.database.AppDatabase;
import com.wise.semester_project.model.User;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executorService;

    public UserRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void register(User user, RegisterCallback callback) {
        executorService.execute(() -> {
            if (userDao.checkEmailExists(user.getEmail()) > 0) {
                callback.onError("该邮箱已被注册");
                return;
            }
            userDao.insert(user);
            callback.onSuccess();
        });
    }

    public void login(String email, String password, LoginCallback callback) {
        executorService.execute(() -> {
            User user = userDao.login(email, password);
            if (user != null) {
                callback.onSuccess(user);
            } else {
                callback.onError("邮箱或密码错误");
            }
        });
    }

    public LiveData<List<User>> getAllUsers() {
        return userDao.getAllUsers();
    }

    public interface RegisterCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String error);
    }
} 