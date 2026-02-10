package com.uairouter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class ClusterVisualizationActivity extends Activity {
    private static final String TAG = "ClusterVisualization";
    private TenantManager tenantManager;
    private String currentTenantId;
    private MultiClusterView multiClusterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster_visualization);
        Log.d(TAG, "onCreate: ClusterVisualizationActivity started");

        tenantManager = new TenantManager(this);
        currentTenantId = tenantManager.getCurrentTenantId();

        LinearLayout container = findViewById(R.id.cluster_container);
        multiClusterView = new MultiClusterView(this);

        // Add the multi-cluster view to the container
        container.addView(multiClusterView);

        List<Cluster> clusters = generateDummyClusters();
        multiClusterView.setClusters(clusters);

        Log.d(TAG, "Generated " + clusters.size() + " clusters for tenant: " + currentTenantId);
    }

    private List<Cluster> generateDummyClusters() {
        List<Cluster> clusters = new ArrayList<>();
        clusters.add(new Cluster(currentTenantId, "Cluster 1", 150, new int[]{255, 0, 0}));
        clusters.add(new Cluster(currentTenantId, "Cluster 2", 200, new int[]{0, 255, 0}));
        clusters.add(new Cluster(currentTenantId, "Cluster 3", 100, new int[]{0, 0, 255}));
        clusters.add(new Cluster(currentTenantId, "Cluster 4", 75, new int[]{255, 255, 0}));
        clusters.add(new Cluster(currentTenantId, "Cluster 5", 120, new int[]{255, 0, 255}));
        clusters.add(new Cluster(currentTenantId, "Cluster 6", 180, new int[]{0, 255, 255}));
        clusters.add(new Cluster(currentTenantId, "Cluster 7", 90, new int[]{128, 128, 128}));
        clusters.add(new Cluster(currentTenantId, "Cluster 8", 160, new int[]{255, 128, 0}));
        clusters.add(new Cluster(currentTenantId, "Cluster 9", 140, new int[]{128, 0, 255}));
        return clusters;
    }
}
