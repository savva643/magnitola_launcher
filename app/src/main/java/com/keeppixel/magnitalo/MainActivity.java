package com.keeppixel.magnitalo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Views - Status Bar
    private TextView tvTime, tvDate, tvTemperature, tvVoltage;

    // Main container
    private FrameLayout mainContentContainer;
    private DraggableHomeButton draggableHomeButton;

    // Content Views
    private View homeView, multitaskView, appsView;

    // Home view widgets
    private SpeedometerView speedometerView;
    private RecyclerView recentCallsList;

    // Bottom navigation apps
    private ImageView appPhone, appMessages, appMusic, appMaps, appSettings, appCamera;

    // Adapters and data
    private RecentCallsAdapter recentCallsAdapter;
    private ArrayList<CallItem> recentCalls;
    private AllAppsAdapter allAppsAdapter;
    private ArrayList<AppItem> allApps;
    private RecentAppsAdapter recentAppsAdapter;
    private ArrayList<RecentAppItem> recentApps;
    private TextView tvAppsCount;
    private TextView btnClearAll;
    private LinearLayout emptyStateLayout;

    // State
    private Handler timeHandler;
    private Handler voltageHandler;
    private Handler speedHandler;
    private ViewMode currentViewMode = ViewMode.HOME;
    private Random random = new Random();

    public enum ViewMode {
        HOME, MULTITASK, APPS
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hideSystemUI();
        initializeViews();
        setupTimeUpdater();
        setupVoltageUpdater();
        setupSpeedUpdater();
        setupData();
        setupAdapters();
        setupDraggableHomeButton();
        setupClickListeners();

        // Set initial view mode
        switchToViewMode(ViewMode.HOME);
    }

    private void hideSystemUI() {
        // Для Android 11 (API 30) и выше
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        }
        // Для версий ниже Android 11
        else {
            // Включение immersive mode
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    private void initializeViews() {
        // Status bar views
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvVoltage = findViewById(R.id.tvVoltage);

        // Main container
        mainContentContainer = findViewById(R.id.mainContentContainer);

        // Content views
        homeView = findViewById(R.id.homeView);
        multitaskView = findViewById(R.id.multitaskView);
        appsView = findViewById(R.id.appsView);

        // Home view widgets
        speedometerView = homeView.findViewById(R.id.speedometerView);
        recentCallsList = homeView.findViewById(R.id.recentCallsList);

        RecyclerView allAppsRecycler = appsView.findViewById(R.id.allAppsRecycler);

        // Bottom navigation apps
        appPhone = findViewById(R.id.appPhone);
        appMessages = findViewById(R.id.appMessages);
        appMusic = findViewById(R.id.appMusic);
        appMaps = findViewById(R.id.appMaps);
        appSettings = findViewById(R.id.appSettings);
        appCamera = findViewById(R.id.appCamera);

        // Draggable home button
        draggableHomeButton = findViewById(R.id.draggableHomeButton);
    }

    private void setupTimeUpdater() {
        timeHandler = new Handler();
        updateTime();
    }

    private void updateTime() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM", new Locale("ru"));

        tvTime.setText(timeFormat.format(now));
        tvDate.setText(dateFormat.format(now));

        timeHandler.postDelayed(this::updateTime, 60000); // Update every minute
    }

    private void setupVoltageUpdater() {
        voltageHandler = new Handler();
        updateVoltage();
    }

    private void updateVoltage() {
        String[] voltages = {"12.8V", "13.2V", "13.4V", "12.9V", "13.1V"};
        String randomVoltage = voltages[random.nextInt(voltages.length)];
        tvVoltage.setText(randomVoltage);

        voltageHandler.postDelayed(this::updateVoltage, 10000); // Update every 10 seconds
    }

    private void setupSpeedUpdater() {
        speedHandler = new Handler();
        updateSpeed();
    }

    private void updateSpeed() {
        if (speedometerView != null) {
            // Simulate realistic speed changes
            float currentSpeed = speedometerView.getCurrentSpeed();
            float speedChange = (random.nextFloat() - 0.5f) * 20f; // ±10 km/h change
            float newSpeed = Math.max(0, Math.min(120, currentSpeed + speedChange));

            speedometerView.setSpeed(newSpeed, true);
        }

        // Schedule next update in 2-5 seconds
        int delay = 2000 + random.nextInt(3000);
        speedHandler.postDelayed(this::updateSpeed, delay);
    }

    private void setupData() {
        // Recent calls data
        recentCalls = new ArrayList<>();
        recentCalls.add(new CallItem("Мама", "2 мин назад", CallItem.CallType.INCOMING));
        recentCalls.add(new CallItem("Работа", "15 мин назад", CallItem.CallType.MISSED));
        recentCalls.add(new CallItem("Анна", "1 час назад", CallItem.CallType.OUTGOING));
        recentCalls.add(new CallItem("Сергей", "2 часа назад", CallItem.CallType.INCOMING));

        // Initialize all apps list
        allApps = new ArrayList<>();
        loadInstalledApps();
    }

    private void setupAdapters() {
        // Recent calls adapter
        if (recentCallsList != null) {
            recentCallsAdapter = new RecentCallsAdapter(this, recentCalls);
            recentCallsAdapter.setOnCallClickListener(callItem -> {
                // Handle call click
                makeCall(callItem.name);
            });

            recentCallsList.setLayoutManager(new LinearLayoutManager(this));
            recentCallsList.setAdapter(recentCallsAdapter);
        }

        // All apps adapter
        RecyclerView allAppsRecycler = appsView.findViewById(R.id.allAppsRecycler);
        if (allAppsRecycler != null) {
            int spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 8 : 6;
            allAppsRecycler.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, spanCount));

            allAppsAdapter = new AllAppsAdapter(this, allApps);
            allAppsAdapter.setOnItemClickListener(app -> {
                launchAppByPackageName(app.packageName);
                // Return to home after launching app
                draggableHomeButton.hideAllMenus();
            });
            allAppsRecycler.setAdapter(allAppsAdapter);
        }

        // Setup recent apps adapter
        setupRecentAppsAdapter();
    }

    private void launchAppByPackageName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);
        allApps.clear();

        for (ResolveInfo app : apps) {
            String appName = app.loadLabel(pm).toString();
            String packageName = app.activityInfo.packageName;

            // Don't include our own app
            if (!packageName.equals(getPackageName())) {
                try {
                    android.graphics.drawable.Drawable icon = pm.getApplicationIcon(packageName);
                    allApps.add(new AppItem(appName, packageName, icon, getColorForApp(appName)));
                } catch (PackageManager.NameNotFoundException e) {
                    // Use default icon if app icon not found
                    allApps.add(new AppItem(appName, packageName, R.drawable.round_phone_android_24, R.color.blue_400));
                }
            }
        }

        // Sort apps alphabetically
        allApps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));

        if (allAppsAdapter != null) {
            allAppsAdapter.notifyDataSetChanged();
        }
    }

    private int getColorForApp(String appName) {
        // Assign colors based on app name hash for consistency
        int hash = appName.hashCode();
        int[] colors = {
                R.color.blue_400, R.color.green_400, R.color.orange_400,
                R.color.pink_400, R.color.gray_400
        };
        return colors[Math.abs(hash) % colors.length];
    }

    private void setupDraggableHomeButton() {
        if (draggableHomeButton != null) {
            draggableHomeButton.setParentContainer(mainContentContainer);
            draggableHomeButton.setMenuLayout(appsView);
            draggableHomeButton.setMultitaskingLayout(multitaskView);

            draggableHomeButton.setDragListener(new DraggableHomeButton.OnDragListener() {
                @Override
                public void onMenuShow() {
                    switchToViewMode(ViewMode.APPS);
                }

                @Override
                public void onMenuHide() {
                    switchToViewMode(ViewMode.HOME);
                }

                @Override
                public void onMultitaskShow() {
                    switchToViewMode(ViewMode.MULTITASK);
                    // Refresh recent apps when multitask view is shown
                    loadRecentApps();
                }

                @Override
                public void onMultitaskHide() {
                    switchToViewMode(ViewMode.HOME);
                }

                @Override
                public void onReturnToHome() {
                    switchToViewMode(ViewMode.HOME);
                }
            });
        }
    }

    private void setupClickListeners() {
        // Bottom navigation app clicks
        if (appPhone != null) {
            appPhone.setOnClickListener(v -> {
                animateAppClick(v);
                launchApp("phone");
            });
        }
        if (appMessages != null) {
            appMessages.setOnClickListener(v -> {
                animateAppClick(v);
                launchApp("messages");
            });
        }
        if (appMusic != null) {
            appMusic.setOnClickListener(v -> {
                animateAppClick(v);
                launchApp("music");
            });
        }
        if (appMaps != null) {
            appMaps.setOnClickListener(v -> {
                animateAppClick(v);
                launchApp("maps");
            });
        }
        if (appSettings != null) {
            appSettings.setOnClickListener(v -> {
                animateAppClick(v);
                launchApp("settings");
            });
        }
        if (appCamera != null) {
            appCamera.setOnClickListener(v -> {
                animateAppClick(v);
                launchApp("camera");
            });
        }
    }

    private void animateAppClick(View view) {
        // Scale animation for app click feedback
        ObjectAnimator scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.9f);
        scaleDown.setDuration(100);
        ObjectAnimator scaleUp = ObjectAnimator.ofFloat(view, "scaleX", 0.9f, 1.0f);
        scaleUp.setDuration(100);
        scaleUp.setStartDelay(100);

        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.9f);
        scaleDownY.setDuration(100);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.9f, 1.0f);
        scaleUpY.setDuration(100);
        scaleUpY.setStartDelay(100);

        scaleDown.start();
        scaleUp.start();
        scaleDownY.start();
        scaleUpY.start();
    }

    private void launchApp(String appName) {
        try {
            String packageName = getPackageNameForApp(appName);
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                startActivity(intent);
            } else {
                // Fallback to system apps
                launchSystemApp(appName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            launchSystemApp(appName);
        }
    }

    private void launchSystemApp(String appName) {
        Intent intent = new Intent();
        switch (appName.toLowerCase()) {
            case "phone":
                intent.setAction(Intent.ACTION_DIAL);
                break;
            case "messages":
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
                break;
            case "music":
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MUSIC);
                break;
            case "maps":
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_MAPS);
                break;
            case "settings":
                intent.setAction(android.provider.Settings.ACTION_SETTINGS);
                break;
            case "camera":
                intent.setAction(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                break;
            default:
                return;
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPackageNameForApp(String appName) {
        switch (appName.toLowerCase()) {
            case "phone":
                return "com.android.dialer";
            case "messages":
                return "com.android.mms";
            case "music":
                return "com.android.music";
            case "maps":
                return "com.google.android.apps.maps";
            case "settings":
                return "com.android.settings";
            case "camera":
                return "com.android.camera2";
            default:
                return "com.android.settings"; // Fallback
        }
    }

    private void makeCall(String contactName) {
        // Simulate making a call
        Intent intent = new Intent(Intent.ACTION_DIAL);
        startActivity(intent);

        // Add haptic feedback
        draggableHomeButton.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private void switchToViewMode(ViewMode mode) {
        currentViewMode = mode;

        // Hide all views
        multitaskView.setVisibility(View.GONE);
        appsView.setVisibility(View.GONE);

        // Show selected view
        View targetView;
        switch (mode) {
            case HOME:
                targetView = homeView;
                break;
            case MULTITASK:
                targetView = multitaskView;
                break;
            case APPS:
                targetView = appsView;
                break;
            default:
                targetView = homeView;
        }

        targetView.setVisibility(View.VISIBLE);


    }

    @Override
    public void onBackPressed() {
        if (currentViewMode != ViewMode.HOME) {
            draggableHomeButton.hideAllMenus();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null) {
            timeHandler.removeCallbacksAndMessages(null);
        }
        if (voltageHandler != null) {
            voltageHandler.removeCallbacksAndMessages(null);
        }
        if (speedHandler != null) {
            speedHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Handle configuration changes if needed
    }

    // Data classes
    public static class CallItem {
        public enum CallType {
            INCOMING, OUTGOING, MISSED
        }

        public String name;
        public String time;
        public CallType type;

        public CallItem(String name, String time, CallType type) {
            this.name = name;
            this.time = time;
            this.type = type;
        }
    }






    // RecentAppItem class
    public static class RecentAppItem {
        public String name;
        public String packageName;
        public int iconRes;
        public android.graphics.drawable.Drawable iconDrawable;
        public int colorRes;
        public android.graphics.Bitmap screenshot;
        public String lastUsedTime;
        public long memoryUsage;
        public boolean isLocked;
        public int taskId;

        public RecentAppItem(String name, String packageName, android.graphics.drawable.Drawable iconDrawable,
                             int colorRes, String lastUsedTime, int taskId) {
            this.name = name;
            this.packageName = packageName;
            this.iconDrawable = iconDrawable;
            this.colorRes = colorRes;
            this.lastUsedTime = lastUsedTime;
            this.taskId = taskId;
            this.isLocked = false;
            this.memoryUsage = 0;
            this.screenshot = null;
        }
    }

    private void setupRecentAppsAdapter() {
        RecyclerView recentAppsRecycler = multitaskView.findViewById(R.id.recentAppsRecycler);
        tvAppsCount = multitaskView.findViewById(R.id.tvAppsCount);
        btnClearAll = multitaskView.findViewById(R.id.btnClearAll);
        emptyStateLayout = multitaskView.findViewById(R.id.emptyStateLayout);

        if (recentAppsRecycler != null) {
            // Use horizontal layout manager for card-style recent apps
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recentAppsRecycler.setLayoutManager(layoutManager);

            recentApps = new ArrayList<>();
            loadRecentApps();

            recentAppsAdapter = new RecentAppsAdapter(this, recentApps);
            recentAppsAdapter.setOnRecentAppActionListener(new RecentAppsAdapter.OnRecentAppActionListener() {
                @Override
                public void onAppClick(RecentAppItem app) {
                    // Switch to the app
                    switchToApp(app);
                }

                @Override
                public void onAppClose(RecentAppItem app, int position) {
                    // Close the app
                    closeApp(app, position);
                }

                @Override
                public void onAppLock(RecentAppItem app) {
                    // Toggle app lock state
                    // Locked apps won't be closed when "Clear All" is pressed
                }
            });

            recentAppsRecycler.setAdapter(recentAppsAdapter);

            // Clear all button
            if (btnClearAll != null) {
                btnClearAll.setOnClickListener(v -> clearAllApps());
            }

            updateAppsCount();
        }
    }

    private void loadRecentApps() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager packageManager = getPackageManager();

        recentApps.clear();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                // Для Android 5.0+ используем UsageStatsManager как более надежный способ
                loadRecentAppsUsingUsageStats();
            } catch (SecurityException e) {
                // Fallback для случаев, когда нет разрешения
                createDummyRecentApps();
            }
        } else {
            // Для старых версий Android (до 5.0)
            try {
                @SuppressWarnings("deprecation")
                List<ActivityManager.RecentTaskInfo> recentTasks = activityManager.getRecentTasks(20,
                        ActivityManager.RECENT_WITH_EXCLUDED);

                for (ActivityManager.RecentTaskInfo taskInfo : recentTasks) {
                    processTaskInfo(taskInfo, packageManager);
                }
            } catch (SecurityException e) {
                createDummyRecentApps();
            }
        }

        if (recentAppsAdapter != null) {
            recentAppsAdapter.notifyDataSetChanged();
        }
        updateAppsCount();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadRecentAppsUsingUsageStats() {
        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long endTime = System.currentTimeMillis();
        long beginTime = endTime - 1000 * 60 * 60 * 24; // За последние 24 часа

        // Проверяем, есть ли у нас разрешение
        if (!hasUsageStatsPermission()) {
            requestUsageStatsPermission();
            return;
        }

        Map<String, UsageStats> stats = usageStatsManager.queryAndAggregateUsageStats(beginTime, endTime);
        List<UsageStats> sortedStats = new ArrayList<>(stats.values());
        Collections.sort(sortedStats, (o1, o2) -> Long.compare(o2.getLastTimeUsed(), o1.getLastTimeUsed()));

        PackageManager pm = getPackageManager();
        int count = 0;

        for (UsageStats usageStats : sortedStats) {
            if (count >= 10) break; // Ограничиваем количество приложений

            try {
                String packageName = usageStats.getPackageName();
                if (packageName.equals(getPackageName()) || isSystemApp(packageName)) {
                    continue;
                }

                ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
                String appName = pm.getApplicationLabel(appInfo).toString();
                Drawable icon = pm.getApplicationIcon(packageName);

                RecentAppItem recentApp = new RecentAppItem(
                        appName,
                        packageName,
                        icon,
                        getColorForApp(appName),
                        getTimeAgo(usageStats.getLastTimeUsed()),
                        -1 // taskId неизвестен при этом подходе
                );

                recentApps.add(recentApp);
                count++;
            } catch (PackageManager.NameNotFoundException e) {
                // Пропускаем приложения, которые не найдены
            }
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                getPackageName()
        );
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void requestUsageStatsPermission() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }

    private void processTaskInfo(ActivityManager.RecentTaskInfo taskInfo, PackageManager packageManager) {
        if (taskInfo.baseIntent != null && taskInfo.baseIntent.getComponent() != null) {
            String packageName = taskInfo.baseIntent.getComponent().getPackageName();

            // Более мягкая фильтрация системных приложений
            if (packageName.equals(getPackageName()) ||
                    packageName.startsWith("com.android.systemui") ||
                    packageName.equals("android")) {
                return;
            }

            try {
                ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                Drawable icon = packageManager.getApplicationIcon(packageName);

                String lastUsedTime = "getTimeAgo(taskInfo.lastActiveTime)";
                RecentAppItem recentApp = new RecentAppItem(
                        appName, packageName, icon,
                        getColorForApp(appName), lastUsedTime, taskInfo.id
                );

                recentApp.memoryUsage = getAppMemoryUsage(packageName);
                recentApps.add(recentApp);
            } catch (PackageManager.NameNotFoundException e) {
                // Пропускаем приложения, которые не найдены
            }
        }
    }

    private void createDummyRecentApps() {
        // Create some dummy recent apps for demonstration
        try {
            android.graphics.drawable.Drawable defaultIcon = getResources().getDrawable(R.drawable.round_phone_android_24);

            recentApps.add(new RecentAppItem("Chrome", "com.android.chrome", defaultIcon,
                    R.color.blue_400, "2 мин назад", 1));
            recentApps.add(new RecentAppItem("WhatsApp", "com.whatsapp", defaultIcon,
                    R.color.green_400, "5 мин назад", 2));
            recentApps.add(new RecentAppItem("YouTube", "com.google.android.youtube", defaultIcon,
                    R.color.pink_400, "10 мин назад", 3));
            recentApps.add(new RecentAppItem("Telegram", "org.telegram.messenger", defaultIcon,
                    R.color.blue_400, "15 мин назад", 4));
            recentApps.add(new RecentAppItem("Spotify", "com.spotify.music", defaultIcon,
                    R.color.green_400, "1 час назад", 5));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSystemApp(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return (appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60 * 1000) {
            return "Только что";
        } else if (diff < 60 * 60 * 1000) {
            int minutes = (int) (diff / (60 * 1000));
            return minutes + " мин назад";
        } else if (diff < 24 * 60 * 60 * 1000) {
            int hours = (int) (diff / (60 * 60 * 1000));
            return hours + " ч назад";
        } else {
            int days = (int) (diff / (24 * 60 * 60 * 1000));
            return days + " дн назад";
        }
    }

    private long getAppMemoryUsage(String packageName) {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();

            for (ActivityManager.RunningAppProcessInfo processInfo : runningApps) {
                if (processInfo.processName.equals(packageName)) {
                    android.os.Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(new int[]{processInfo.pid});
                    if (memoryInfos.length > 0) {
                        return memoryInfos[0].getTotalPss() * 1024; // Convert KB to bytes
                    }
                }
            }
        } catch (Exception e) {
            // Return random memory usage for demo
            return (long) (50 + Math.random() * 200) * 1024 * 1024; // 50-250 MB
        }

        return 0;
    }

    private void switchToApp(RecentAppItem app) {
        try {
            ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.moveTaskToFront(app.taskId, ActivityManager.MOVE_TASK_WITH_HOME);

            // Return to home after switching
            draggableHomeButton.hideAllMenus();

        } catch (Exception e) {
            // Fallback: try to launch the app normally
            launchAppByPackageName(app.packageName);
            draggableHomeButton.hideAllMenus();
        }
    }

    private void closeApp(RecentAppItem app, int position) {
        try {
            if (!app.isLocked) {
                ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

                // For newer Android versions, we can't kill other apps directly
                // So we'll just remove from our list and show animation
                recentAppsAdapter.removeApp(position);
                updateAppsCount();

                // Add haptic feedback
                draggableHomeButton.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearAllApps() {
        // Remove all unlocked apps
        ArrayList<RecentAppItem> lockedApps = new ArrayList<>();
        for (RecentAppItem app : recentApps) {
            if (app.isLocked) {
                lockedApps.add(app);
            }
        }

        recentApps.clear();
        recentApps.addAll(lockedApps);

        recentAppsAdapter.updateApps(recentApps);
        updateAppsCount();

        // Add haptic feedback
        draggableHomeButton.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY);
    }

    private void updateAppsCount() {
        if (tvAppsCount != null) {
            int count = recentApps.size();
            if (count == 0) {
                tvAppsCount.setText("Нет открытых приложений");
                if (emptyStateLayout != null) {
                    emptyStateLayout.setVisibility(View.VISIBLE);
                }
            } else {
                tvAppsCount.setText(count + " открытых приложений");
                if (emptyStateLayout != null) {
                    emptyStateLayout.setVisibility(View.GONE);
                }
            }
        }
    }
}
