package com.keeppixel.magnitalo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import java.util.ArrayList;

public class QuickAppAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<QuickApp> quickApps;
    private LayoutInflater inflater;

    public QuickAppAdapter(Context context, ArrayList<QuickApp> quickApps) {
        this.context = context;
        this.quickApps = quickApps;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return quickApps.size();
    }

    @Override
    public Object getItem(int position) {
        return quickApps.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_quick_app, parent, false);
            holder = new ViewHolder();
            holder.cardView = convertView.findViewById(R.id.cardQuickApp);
            holder.iconBackground = convertView.findViewById(R.id.iconBackground);
            holder.icon = convertView.findViewById(R.id.ivAppIcon);
            holder.name = convertView.findViewById(R.id.tvAppName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QuickApp app = quickApps.get(position);

        // Set app data
        holder.icon.setImageResource(app.iconRes);
        holder.name.setText(app.name);
        holder.iconBackground.setBackgroundColor(context.getResources().getColor(app.colorRes));

        // Set click listener with animation
        holder.cardView.setOnClickListener(v -> {
            animateClick(v);
            handleAppClick(app);
        });

        // Add hover effect
        holder.cardView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    animatePress(v, true);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    animatePress(v, false);
                    break;
            }
            return false;
        });

        return convertView;
    }

    private void animateClick(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.95f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.95f, 1.0f);

        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }

    private void animatePress(View view, boolean pressed) {
        float scale = pressed ? 0.98f : 1.0f;
        float elevation = pressed ? 2f : 8f;

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale);
        ObjectAnimator elevationAnim = ObjectAnimator.ofFloat(view, "elevation", elevation);

        scaleX.setDuration(100);
        scaleY.setDuration(100);
        elevationAnim.setDuration(100);

        scaleX.start();
        scaleY.start();
        elevationAnim.start();
    }

    private void handleAppClick(QuickApp app) {
        switch (app.name) {
            case "Navigation":
                openNavigationApp();
                break;
            case "Phone":
                openPhoneApp();
                break;
            case "Settings":
                openSettingsApp();
                break;
            case "Music":
                openMusicApp();
                break;
            default:
                // Handle custom apps
                break;
        }
    }

    private void openNavigationApp() {
        try {
            android.content.Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage("com.google.android.apps.maps");
            if (intent != null) {
                context.startActivity(intent);
            } else {
                // Fallback to generic navigation intent
                android.content.Intent navIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                navIntent.setData(android.net.Uri.parse("geo:0,0?q=navigation"));
                context.startActivity(navIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openPhoneApp() {
        try {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSettingsApp() {
        try {
            android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_SETTINGS);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openMusicApp() {
        try {
            android.content.Intent intent = new android.content.Intent("android.intent.action.MUSIC_PLAYER");
            context.startActivity(intent);
        } catch (Exception e) {
            // Fallback to generic music intent
            android.content.Intent musicIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
            musicIntent.setType("audio/*");
            context.startActivity(musicIntent);
        }
    }

    static class ViewHolder {
        CardView cardView;
        View iconBackground;
        ImageView icon;
        TextView name;
    }
}