package com.keeppixel.magnitalo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HomeGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_SPEED_WIDGET = 0;
    private static final int TYPE_NAVIGATION_WIDGET = 1;
    private static final int TYPE_MUSIC_PLAYER = 2;
    private static final int TYPE_QUICK_APP = 3;

    private Context context;
    private ArrayList<HomeGridItem> items;
    private ArrayList<MusicSource> musicSources;
    private String currentSource;
    private boolean isPlaying = false;

    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(HomeGridItem item);
        void onMusicSourceSelected(String source);
        void onPlayPauseClick();
        void onPreviousClick();
        void onNextClick();
    }

    public HomeGridAdapter(Context context, ArrayList<HomeGridItem> items,
                           ArrayList<MusicSource> musicSources, String currentSource) {
        this.context = context;
        this.items = items;
        this.musicSources = musicSources;
        this.currentSource = currentSource;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void updateMusicSources(ArrayList<MusicSource> sources) {
        this.musicSources = sources;
        notifyDataSetChanged();
    }

    public void updateCurrentSource(String source) {
        this.currentSource = source;
        notifyDataSetChanged();
    }

    public void updatePlayState(boolean playing) {
        this.isPlaying = playing;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        switch (items.get(position).type) {
            case SPEED_WIDGET:
                return TYPE_SPEED_WIDGET;
            case NAVIGATION_WIDGET:
                return TYPE_NAVIGATION_WIDGET;
            case MUSIC_PLAYER:
                return TYPE_MUSIC_PLAYER;
            case QUICK_APP:
                return TYPE_QUICK_APP;
            default:
                return TYPE_QUICK_APP;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case TYPE_SPEED_WIDGET:
                return new SpeedWidgetViewHolder(inflater.inflate(R.layout.item_speed_widget, parent, false));
            case TYPE_NAVIGATION_WIDGET:
                return new NavigationWidgetViewHolder(inflater.inflate(R.layout.item_navigation_widget, parent, false));
            case TYPE_MUSIC_PLAYER:
                return new MusicPlayerViewHolder(inflater.inflate(R.layout.item_music_player, parent, false));
            case TYPE_QUICK_APP:
                return new QuickAppViewHolder(inflater.inflate(R.layout.item_quick_app_grid, parent, false));
            default:
                return new QuickAppViewHolder(inflater.inflate(R.layout.item_quick_app_grid, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HomeGridItem item = items.get(position);

        switch (holder.getItemViewType()) {
            case TYPE_SPEED_WIDGET:
                bindSpeedWidget((SpeedWidgetViewHolder) holder, item);
                break;
            case TYPE_NAVIGATION_WIDGET:
                bindNavigationWidget((NavigationWidgetViewHolder) holder, item);
                break;
            case TYPE_MUSIC_PLAYER:
                bindMusicPlayer((MusicPlayerViewHolder) holder, item);
                break;
            case TYPE_QUICK_APP:
                bindQuickApp((QuickAppViewHolder) holder, item);
                break;
        }
    }

    private void bindSpeedWidget(SpeedWidgetViewHolder holder, HomeGridItem item) {
        holder.speedValue.setText("65");
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    private void bindNavigationWidget(NavigationWidgetViewHolder holder, HomeGridItem item) {
        holder.instruction.setText("Поверните направо через 500м");
        holder.address.setText("ул. Примерная, 123");
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    private void bindMusicPlayer(MusicPlayerViewHolder holder, HomeGridItem item) {
        // Update music sources
        holder.sourcesLayout.removeAllViews();
        for (MusicSource source : musicSources) {
            View sourceView = LayoutInflater.from(context).inflate(R.layout.item_music_source, holder.sourcesLayout, false);
            TextView sourceText = sourceView.findViewById(R.id.tvSourceName);
            ImageView sourceIcon = sourceView.findViewById(R.id.ivSourceIcon);
            CardView sourceCard = sourceView.findViewById(R.id.cardSource);

            sourceText.setText(source.name);
            sourceIcon.setImageResource(source.iconRes);

            if (source.name.equals(currentSource)) {
                sourceCard.setCardBackgroundColor(context.getResources().getColor(source.colorRes));
            } else {
                sourceCard.setCardBackgroundColor(context.getResources().getColor(R.color.white_10));
            }

            sourceView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onMusicSourceSelected(source.name);
                }
            });

            holder.sourcesLayout.addView(sourceView);
        }

        // Update play/pause button
        holder.btnPlayPause.setImageResource(isPlaying ? R.drawable.round_pause_24 : R.drawable.round_play_arrow_24);

        // Set click listeners
        holder.btnPlayPause.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPlayPauseClick();
            }
        });

        holder.btnPrevious.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onPreviousClick();
            }
        });

        holder.btnNext.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onNextClick();
            }
        });

        holder.currentSource.setText("Источник: " + currentSource);
    }

    private void bindQuickApp(QuickAppViewHolder holder, HomeGridItem item) {
        holder.appName.setText(item.title);
        holder.appIcon.setImageResource(item.iconRes);
        holder.cardView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder classes
    static class SpeedWidgetViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView speedValue;

        SpeedWidgetViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardSpeedWidget);
            speedValue = itemView.findViewById(R.id.tvSpeedValue);
        }
    }

    static class NavigationWidgetViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView instruction, address;

        NavigationWidgetViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardNavigationWidget);
            instruction = itemView.findViewById(R.id.tvInstruction);
            address = itemView.findViewById(R.id.tvAddress);
        }
    }

    static class MusicPlayerViewHolder extends RecyclerView.ViewHolder {
        LinearLayout sourcesLayout;
        ImageButton btnPlayPause, btnPrevious, btnNext;
        TextView trackName, currentSource;
        SeekBar seekBar;

        MusicPlayerViewHolder(View itemView) {
            super(itemView);
            sourcesLayout = itemView.findViewById(R.id.musicSourcesLayout);
            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
            btnPrevious = itemView.findViewById(R.id.btnPrevious);
            btnNext = itemView.findViewById(R.id.btnNext);
            trackName = itemView.findViewById(R.id.tvTrackName);
            currentSource = itemView.findViewById(R.id.tvCurrentSource);
            seekBar = itemView.findViewById(R.id.seekBarProgress);
        }
    }

    static class QuickAppViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView appIcon;
        TextView appName;

        QuickAppViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardQuickApp);
            appIcon = itemView.findViewById(R.id.ivAppIcon);
            appName = itemView.findViewById(R.id.tvAppName);
        }
    }
}