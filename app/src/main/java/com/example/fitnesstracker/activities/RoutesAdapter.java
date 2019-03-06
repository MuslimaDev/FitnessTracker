package com.example.fitnesstracker.activities;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.Routes;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.ViewHolder> {
    private List<Routes> items;

    RoutesAdapter(List<Routes> items, RoutesListActivity activity) {
        this.items = items;
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

        void deleteFromDataBase() {
            Realm realm = Realm.getDefaultInstance();
            final RealmResults<Routes> mResults = realm.where(Routes.class).findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    mResults.deleteFromRealm(0);
                }
            });
        }

        ViewHolder(final View itemView) {
            super(itemView);
            distance = itemView.findViewById(R.id.distance);
            time = itemView.findViewById(R.id.time);
            date = itemView.findViewById(R.id.date);
            delete = itemView.findViewById(R.id.delete);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteFromDataBase();
                }
            });
        }
    }
}