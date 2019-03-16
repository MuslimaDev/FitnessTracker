package com.example.fitnesstracker.activities.routes;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.interfaces.CallBackRealm;
import com.example.fitnesstracker.models.Routes;

import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {
    private List<Routes> items;
    private CallBackRealm mCallBackRealm;

    RoutesAdapter(List<Routes> items, CallBackRealm callBackRealm) {
        this.items = items;
        this.mCallBackRealm =  callBackRealm;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.routes_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routes model = items.get(position);
        holder.distance.setText(model.getDistance());
        holder.time.setText(model.getTime());
        holder.date.setText(model.getDate());
        holder.delete.setTag(model.getId());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView distance, time, date;
        ImageView delete;

        ViewHolder(final View itemView) {
            super(itemView);
            distance = itemView.findViewById(R.id.distance);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            delete = itemView.findViewById(R.id.delete);

            delete.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mCallBackRealm.deleteRealmObject(items, (int)view.getTag());
                    return false;
                }
            });
        }
    }
}