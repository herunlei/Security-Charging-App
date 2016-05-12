package com.securitycharging.SupportFiles;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarkerLocation implements ClusterItem {

    private String CSName;
    private String portName;
    private LatLng position;
    private String availability;

    /**
     * @param name
     * @param port
     * @param latLng
     * @param available
     */
    public ClusterMarkerLocation(String name, String port, LatLng latLng, String available) {
        CSName = name;
        portName = port;
        position = latLng;
        availability = available;
    }

    /**
     * @return
     */
    public String getCSName() {
        return CSName;
    }

    /**
     * @return
     */
    @Override
    public LatLng getPosition() {
        return position;
    }

    /**
     * @return
     */
    public String getPortName() {
        return portName;
    }

    /**
     * @return
     */
    public String getAvailable() {
        return availability;
    }
}