package com.keeppixel.magnitalo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class WidgetSettingsAdapter extends RecyclerView.Adapter<WidgetSettingsAdapter.ViewHolder> {

    private ArrayList<WidgetSetting> widgetSettings;
    private OnWidgetToggleListener toggleListener;

    public interface OnWidgetToggleListener {
        void onWidgetToggled(String widgetName, boolean enabled);
    }

    public WidgetSettingsAdapter(ArrayList<WidgetSetting> widgetSettings) {
        this.widgetSettings = widgetSettings;
    }

    public void setOnWidgetToggleListener(OnWidgetToggleListener listener) {
        this.toggleListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_widget_setting, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WidgetSetting setting = widgetSettings.get(position);

        holder.widgetName.setText(setting.name);
        holder.toggleSwitch.setChecked(setting.enabled);

        // Set switch colors based on state
        updateSwitchAppearance(holder.toggleSwitch, setting.enabled);

        // Handle switch toggle
        holder.toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setting.enabled = isChecked;
            updateSwitchAppearance(holder.toggleSwitch, isChecked);
            animateToggle(holder.cardView, isChecked);

            if (toggleListener != null) {
                toggleListener.onWidgetToggled(setting.name, isChecked);
            }
        });

        // Handle card click to toggle switch
        holder.cardView.setOnClickListener(v -> {
            holder.toggleSwitch.setChecked(!holder.toggleSwitch.isChecked());
        });

        // Add entrance animation
        animateItemEntrance(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return widgetSettings.size();
    }

    private void updateSwitchAppearance(Switch toggleSwitch, boolean enabled) {
        if (enabled) {
            toggleSwitch.getThumbDrawable().setTint(
                    toggleSwitch.getContext().getResources().getColor(R.color.white)
            );
            toggleSwitch.getTrackDrawable().setTint(
                    toggleSwitch.getContext().getResources().getColor(R.color.blue_500)
            );
        } else {
            toggleSwitch.getThumbDrawable().setTint(
                    toggleSwitch.getContext().getResources().getColor(R.color.gray_400)
            );
            toggleSwitch.getTrackDrawable().setTint(
                    toggleSwitch.getContext().getResources().getColor(R.color.gray_600)
            );
        }
    }

    private void animateToggle(View view, boolean enabled) {
        // Animate background color change
        int startColor = view.getContext().getResources().getColor(
                enabled ? R.color.white_10 : R.color.blue_500
        );
        int endColor = view.getContext().getResources().getColor(
                enabled ? R.color.blue_500 : R.color.white_10
        );

        ValueAnimator colorAnimator = ValueAnimator.ofArgb(startColor, endColor);
        colorAnimator.setDuration(300);
        colorAnimator.addUpdateListener(animation -> {
            if (view instanceof CardView) {
                ((CardView) view).setCardBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimator.start();

        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.02f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.02f, 1.0f);

        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }

    private void animateItemEntrance(View view, int position) {
        view.setAlpha(0f);
        view.setTranslationX(100f);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", 100f, 0f);

        alpha.setDuration(400);
        translationX.setDuration(400);
        alpha.setStartDelay(position * 50L); // Stagger animation
        translationX.setStartDelay(position * 50L);
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());
        translationX.setInterpolator(new AccelerateDecelerateInterpolator());

        alpha.start();
        translationX.start();
    }

    public void updateWidgetSetting(String widgetName, boolean enabled) {
        for (int i = 0; i < widgetSettings.size(); i++) {
            if (widgetSettings.get(i).name.equals(widgetName)) {
                widgetSettings.get(i).enabled = enabled;
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addWidgetSetting(WidgetSetting setting) {
        widgetSettings.add(setting);
        notifyItemInserted(widgetSettings.size() - 1);
    }

    public void removeWidgetSetting(String widgetName) {
        for (int i = 0; i < widgetSettings.size(); i++) {
            if (widgetSettings.get(i).name.equals(widgetName)) {
                widgetSettings.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView widgetName;
        Switch toggleSwitch;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardWidgetSetting);
            widgetName = itemView.findViewById(R.id.tvWidgetName);
            toggleSwitch = itemView.findViewById(R.id.switchWidget);
        }
    }
}