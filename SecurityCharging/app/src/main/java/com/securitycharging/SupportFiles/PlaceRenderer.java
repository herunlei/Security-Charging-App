package com.securitycharging.SupportFiles;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

/**
 * support libs form google maps unity
 */

public class PlaceRenderer extends DefaultClusterRenderer<ClusterMarkerLocation> {

    /**
     * @param context
     * @param map
     * @param clusterManager
     */
    public PlaceRenderer(Context context, GoogleMap map, ClusterManager<ClusterMarkerLocation> clusterManager) {
        super(context, map, clusterManager);
    }

    /**
     * @param location
     * @param markerOptions
     */
    @Override
    protected void onBeforeClusterItemRendered(ClusterMarkerLocation location, MarkerOptions markerOptions) {
        if (location.getAvailable().equals("available")) {
            markerOptions.title(location.getCSName()).snippet(location.getPortName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if (location.getAvailable().equals("not available")) {
            markerOptions.title(location.getCSName()).snippet(location.getPortName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }
    }

    /**
     * @param cluster
     * @return
     */
    @Override
    protected boolean shouldRenderAsCluster(Cluster<ClusterMarkerLocation> cluster) {
        return cluster.getSize() > 1;
    }
}