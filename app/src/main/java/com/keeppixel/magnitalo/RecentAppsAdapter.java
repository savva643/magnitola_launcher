package com.keeppixel.magnitalo;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RecentAppsAdapter extends RecyclerView.Adapter<RecentAppsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MainActivity.RecentAppItem> recentApps;
    private OnRecentAppActionListener actionListener;

    public interface OnRecentAppActionListener {
        void onAppClick(MainActivity.RecentAppItem app);
        void onAppClose(MainActivity.RecentAppItem app, int position);
        void onAppLock(MainActivity.RecentAppItem app);
    }

    public RecentAppsAdapter(Context context, ArrayList<MainActivity.RecentAppItem> recentApps) {
        this.context = context;
        this.recentApps = recentApps;
    }

    public void setOnRecentAppActionListener(OnRecentAppActionListener listener) {
        this.actionListener = listener;
    }

    public void removeApp(int position) {
        if (position >= 0 && position < recentApps.size()) {
            recentApps.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, recentApps.size());
        }
    }

    public void updateApps(ArrayList<MainActivity.RecentAppItem> newApps) {
        this.recentApps = newApps;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.RecentAppItem app = recentApps.get(position);

        // Set app name
        holder.appName.setText(app.name);

        // Set app icon
        if (app.iconDrawable != null) {
            holder.appIcon.setImageDrawable(app.iconDrawable);
        } else if (app.iconRes != 0) {
            holder.appIcon.setImageResource(app.iconRes);
        }

        // Set app preview/screenshot
        if (app.screenshot != null) {
            holder.appPreview.setImageBitmap(app.screenshot);
            holder.appPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            // Create a placeholder preview
            holder.appPreview.setImageBitmap(createPlaceholderPreview(app.name));
            holder.appPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        // Set last used time
        holder.lastUsedTime.setText(app.lastUsedTime);

        // Set memory usage if available
        if (app.memoryUsage > 0) {
            holder.memoryUsage.setText(formatMemoryUsage(app.memoryUsage));
            holder.memoryUsage.setVisibility(View.VISIBLE);
        } else {
            holder.memoryUsage.setVisibility(View.GONE);
        }

        // Set lock state
        holder.lockButton.setImageResource(app.isLocked ?
                R.drawable.outline_lock_24 : R.drawable.round_lock_open_24);

        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAppClick(app);
            }

            // Add click animation
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        });

        holder.closeButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAppClose(app, holder.getAdapterPosition());
            }

            // Add close animation
            v.animate()
                    .rotation(90f)
                    .setDuration(200)
                    .withEndAction(() -> v.setRotation(0f))
                    .start();
        });

        holder.lockButton.setOnClickListener(v -> {
            if (actionListener != null) {
                app.isLocked = !app.isLocked;
                holder.lockButton.setImageResource(app.isLocked ?
                        R.drawable.outline_lock_24 : R.drawable.round_lock_open_24);
                actionListener.onAppLock(app);
            }
        });

        // Set background color based on app
        try {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(app.colorRes));
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.black_alpha_80));
        }
    }

    private Bitmap createPlaceholderPreview(String appName) {
        int width = 200;
        int height = 300;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Background gradient
        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#1C1C1E"));
        canvas.drawRect(0, 0, width, height, backgroundPaint);

        // App name text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Draw app name in center
        Rect textBounds = new Rect();
        textPaint.getTextBounds(appName, 0, appName.length(), textBounds);
        float textX = width / 2f;
        float textY = height / 2f + textBounds.height() / 2f;
        canvas.drawText(appName, textX, textY, textPaint);

        // Draw some placeholder UI elements
        Paint uiPaint = new Paint();
        uiPaint.setColor(Color.parseColor("#007AFF"));

        // Top bar
        canvas.drawRect(0, 0, width, 40, uiPaint);

        // Bottom navigation
        uiPaint.setColor(Color.parseColor("#2C2C2E"));
        canvas.drawRect(0, height - 60, width, height, uiPaint);

        return bitmap;
    }

    private String formatMemoryUsage(long memoryBytes) {
        if (memoryBytes < 1024 * 1024) {
            return String.format("%.1f KB", memoryBytes / 1024.0);
        } else if (memoryBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", memoryBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", memoryBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public int getItemCount() {
        return recentApps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView appIcon, appPreview;
        TextView appName, lastUsedTime, memoryUsage;
        ImageButton closeButton, lockButton;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardRecentApp);
            appIcon = itemView.findViewById(R.id.ivAppIcon);
            appPreview = itemView.findViewById(R.id.ivAppPreview);
            appName = itemView.findViewById(R.id.tvAppName);
            lastUsedTime = itemView.findViewById(R.id.tvLastUsedTime);
            memoryUsage = itemView.findViewById(R.id.tvMemoryUsage);
            closeButton = itemView.findViewById(R.id.btnCloseApp);
            lockButton = itemView.findViewById(R.id.btnLockApp);
        }
    }
}
