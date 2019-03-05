package com.example.fitnesstracker.activities;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.Routes;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

public class RoutesAdapter extends RealmBaseAdapter<Routes> implements ListAdapter {

    private RoutesListActivity activity;

    private static class ViewHolder {
        TextView date, time, distance;
    }

    RoutesAdapter(RoutesListActivity activity, OrderedRealmCollection<Routes> data) {
        super(data);
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.routes_list, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.date = convertView.findViewById(R.id.date);
            viewHolder.time = convertView.findViewById(R.id.time);
            viewHolder.distance = convertView.findViewById(R.id.distance);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            Routes routes = adapterData.get(position);
            viewHolder.date.setText(routes.getDate());
            viewHolder.time.setText(routes.getTime());
            viewHolder.distance.setText(routes.getDistance());
        }
        return convertView;
    }
}
