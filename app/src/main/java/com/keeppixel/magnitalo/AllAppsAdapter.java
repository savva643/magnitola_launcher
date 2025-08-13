package com.keeppixel.magnitalo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class AllAppsAdapter extends RecyclerView.Adapter<AllAppsAdapter.ViewHolder> {

    private Context context;
    private ArrayList<AppItem> apps;
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onAppClick(AppItem app);
    }

    public AllAppsAdapter(Context context, ArrayList<AppItem> apps) {
        this.context = context;
        this.apps = apps;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_all_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppItem app = apps.get(position);

        holder.appName.setText(app.name);

        // Set icon - prefer drawable over resource
        if (app.iconDrawable != null) {
            holder.appIcon.setImageDrawable(app.iconDrawable);
            // Remove tint for real app icons
            holder.appIcon.setColorFilter(null);
        } else if (app.iconRes != 0) {
            holder.appIcon.setImageResource(app.iconRes);
            // Keep tint for default icons
            holder.appIcon.setColorFilter(context.getResources().getColor(android.R.color.white));
        }

        // Set background color
        try {
            holder.iconBackground.setBackgroundColor(context.getResources().getColor(app.colorRes));
        } catch (Exception e) {
            // Fallback color if resource not found
            holder.iconBackground.setBackgroundColor(context.getResources().getColor(R.color.blue_400));
        }

        // Set click listener
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onAppClick(app);
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
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View iconBackground;
        ImageView appIcon;
        TextView appName;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardAllApp);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            appIcon = itemView.findViewById(R.id.ivAppIcon);
            appName = itemView.findViewById(R.id.tvAppName);
        }
    }
}
