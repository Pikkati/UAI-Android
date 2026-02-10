package com.uairouter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class ClusterAdapter extends ArrayAdapter<Cluster> {
    private static final String TAG = "ClusterAdapter";

    public ClusterAdapter(Context context, List<Cluster> clusters) {
        super(context, 0, clusters);
        Log.d(TAG, "Adapter created with " + clusters.size() + " items");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "getView called for position " + position);
        Cluster cluster = getItem(position);

        if (convertView == null) {
            Log.d(TAG, "Inflating new view for position " + position);
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_cluster, parent, false);
        }

        TextView nameView = convertView.findViewById(R.id.cluster_name);
        TextView sizeView = convertView.findViewById(R.id.cluster_size);
        ClusterView vizView = convertView.findViewById(R.id.cluster_viz);

        if (nameView != null && sizeView != null && vizView != null) {
            nameView.setText(cluster.name);
            sizeView.setText("Size: " + cluster.size);
            vizView.setCluster(cluster);
            Log.d(TAG, "Set data for cluster " + cluster.name);
        } else {
            Log.e(TAG, "One or more views are null in getView");
        }

        return convertView;
    }
}
