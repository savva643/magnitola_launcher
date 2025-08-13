package com.keeppixel.magnitalo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecentCallsAdapter extends RecyclerView.Adapter<RecentCallsAdapter.CallViewHolder> {

    private Context context;
    private List<MainActivity.CallItem> calls;
    private OnCallClickListener listener;

    public interface OnCallClickListener {
        void onCallClick(MainActivity.CallItem callItem);
    }

    public RecentCallsAdapter(Context context, List<MainActivity.CallItem> calls) {
        this.context = context;
        this.calls = calls;
    }

    public void setOnCallClickListener(OnCallClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CallViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recent_call, parent, false);
        return new CallViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallViewHolder holder, int position) {
        MainActivity.CallItem call = calls.get(position);

        holder.callerName.setText(call.name);
        holder.callTime.setText(call.time);

        // Set call type icon and color
        switch (call.type) {
            case INCOMING:
                holder.callType.setText("↓");
                holder.callType.setBackgroundResource(R.drawable.call_type_incoming);
                break;
            case OUTGOING:
                holder.callType.setText("↑");
                holder.callType.setBackgroundResource(R.drawable.call_type_outgoing);
                break;
            case MISSED:
                holder.callType.setText("✕");
                holder.callType.setBackgroundResource(R.drawable.call_type_missed);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCallClick(call);
            }
        });
    }

    @Override
    public int getItemCount() {
        return calls.size();
    }

    static class CallViewHolder extends RecyclerView.ViewHolder {
        TextView callType, callerName, callTime;

        CallViewHolder(@NonNull View itemView) {
            super(itemView);
            callType = itemView.findViewById(R.id.callType);
            callerName = itemView.findViewById(R.id.callerName);
            callTime = itemView.findViewById(R.id.callTime);
        }
    }
}
