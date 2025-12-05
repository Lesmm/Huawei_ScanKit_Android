package com.example.scankitdemo.activity;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 定制UI/阶段/事件
 */
public abstract class ActivityCustomizer {
    public void onCreate(@NonNull Activity activity) {
    }

    public void onResume(@NonNull Activity activity) {
    }

    public void onPause(@NonNull Activity activity) {
    }

    public void onDestroy(@NonNull Activity activity) {
    }

    // 定制者栈
    private static final List<ActivityCustomizer> customizers = new ArrayList<>();

    public static void clear() {
        customizers.clear();
    }

    public static void push(@NonNull ActivityCustomizer customizer) {
        customizers.add(customizer);
    }

    @Nullable
    public static ActivityCustomizer pop() {
        return customizers.isEmpty() ? null : customizers.remove(customizers.size() - 1);
    }
}
