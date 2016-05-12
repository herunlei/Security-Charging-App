package com.securitycharging.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.securitycharging.R;
import com.securitycharging.SupportFiles.Client;
import com.securitycharging.SupportFiles.ClusterMarkerLocation;
import com.securitycharging.SupportFiles.PlaceAlgorithm;
import com.securitycharging.SupportFiles.PlaceRenderer;

import java.util.concurrent.ExecutionException;

public class MapsFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener,
        ClusterManager.OnClusterClickListener<ClusterMarkerLocation>,
        ClusterManager.OnClusterItemClickListener<ClusterMarkerLocation>, LocationListener {

    private GoogleMap mMap;                         //google map
    private double[] destination;                   //select item's location
    private String username;                        //current user's username
    private String[] place;                         //select item information
    private Dialog dialog;                          //select item's info window
    private ClusterMarkerLocation clickedLocation;  //represent for charging station port
    private LatLng myLocation;                      //user current location
    private LocationManager locationManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dialog = new Dialog(getActivity());
        username = getArguments().getString("Username");
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MapFragment fragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);

        FloatingActionButton fix = (FloatingActionButton) getActivity()
                .findViewById(R.id.button_fix);

        fix.setOnClickListener(this);
    }

    /**
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        int i, j;

        //unable google map UI settings
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        //able my locaiton
        mMap.setMyLocationEnabled(true);

        //get user current location
        locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        } else {
            locationManager.requestLocationUpdates(provider, 0, 0, this);
        }

        //faked charging station location and information
        String[] CSs = new String[]{"fake 1st charging station,1,45.419794,-75.674495,available",
                "fake 1st charging station,2,45.419773,-75.674469,not available",
                "fake 2nd charging station,1,45.421144,-75.672496,available",
                "fake 2nd charging station,2,45.421123,-75.672463,available",
                "fake 2nd charging station,3,45.421089,-75.672528,available",
                "fake 3rd charging station,1,45.421046,-75.689963,not available",
                "fake 3rd charging station,2,45.421045,-75.689926,not available",
                "fake 3rd charging station,3,45.421074,-75.689904,available",
                "fake 4th charging station,1,45.416205,-75.667396,not available",
                "fake 4th charging station,2,45.416174,-75.667510,not available"};

        String[][] info = new String[CSs.length][]; //format charging station data for easy to use
        for (i = 0; i < CSs.length; i++) {
            info[i] = CSs[i].split(",");
        }

        // Cluster manager
        ClusterManager<ClusterMarkerLocation> clusterManager =
                new ClusterManager<>(getActivity(), mMap);
        clusterManager.setRenderer(new PlaceRenderer(getActivity().getApplicationContext(),
                mMap, clusterManager));
        clusterManager.setAlgorithm(new PlaceAlgorithm());
        mMap.setOnCameraChangeListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager.getMarkerManager());
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());

        // Add clustered marker to map
        for (j = 0; j < CSs.length; j++) {
            clusterManager.addItem(new ClusterMarkerLocation(info[j][0], info[j][1],
                    (new LatLng(Double.parseDouble(info[j][2]), Double.parseDouble(info[j][3]))),
                    info[j][4]));
        }

        clusterManager.setOnClusterClickListener(this);
        clusterManager.setOnClusterItemClickListener(this);

        // Move camera to fit start point and destination point in screen
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_fix) { //move screen to current location
            // Move camera to fit start point and destination point in screen
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));
        } else if (v.getId() == R.id.charging) {    //make reservation and charging
            if (clickedLocation.getAvailable().equals("available")) {   //check if the charging station port is available or not
                dialog.dismiss();

                //to check if the account has payment already
                try {
                    String run = new check().execute("check," + username).get();    //process charging payment
                    if (run.equals("1")) {  //if return is 1, means charging payment success
                        FragmentManager fm = getFragmentManager();
                        Bundle bundle = new Bundle();
                        bundle.putString("Username", username);
                        bundle.putStringArray("Place", place);
                        bundle.putDoubleArray("Destination", destination);
                        ConfirmFragment confirmFragment = new ConfirmFragment();
                        confirmFragment.setArguments(bundle);
                        fm.beginTransaction().replace(R.id.content_frame, confirmFragment).commit();
                    } else if (run.equals("0")) {   //otherwise, charging payment fail
                        FragmentManager fm = getFragmentManager();
                        Bundle bundle = new Bundle();
                        bundle.putString("Username", username);
                        bundle.putStringArray("Place", place);
                        bundle.putDoubleArray("Destination", destination);
                        bundle.putString("Type", "map");
                        PaymentFragment paymentFragment = new PaymentFragment();
                        paymentFragment.setArguments(bundle);
                        fm.beginTransaction().replace(R.id.content_frame, paymentFragment).commit();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), "The Selected charging station port is not available!", Toast.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.navigation) {  //call google map to navigate
            if (destination == null) {
                Toast.makeText(getActivity(), "Nothing Select!", Toast.LENGTH_SHORT).show();
            } else {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination[0] + ","
                        + destination[1]);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        }
    }

    /**
     * @param cluster
     * @return
     */
    @Override
    public boolean onClusterClick(Cluster<ClusterMarkerLocation> cluster) {
        //Calculate the markers to get their position
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ClusterMarkerLocation clusterMarkerLocation : cluster.getItems()) {
            builder.include(clusterMarkerLocation.getPosition());
        }
        LatLngBounds bounds = builder.build();
        //Change the padding as per needed
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 5);
        mMap.animateCamera(cameraUpdate);
        return true;
    }

    /**
     * @param clusterMarkerLocation
     * @return
     */
    @Override
    public boolean onClusterItemClick(ClusterMarkerLocation clusterMarkerLocation) {
        clickedLocation = clusterMarkerLocation;    //get select charging port
        destination = new double[]{clusterMarkerLocation.getPosition().latitude,    //get select charging port location
                clusterMarkerLocation.getPosition().longitude};

        dialog.setContentView(R.layout.custominfowindow);   //show the detail info window

        TextView chargingStation = (TextView) dialog.findViewById(R.id.charging_station);
        TextView chargingPort = (TextView) dialog.findViewById(R.id.charging_port);
        ImageButton charging = (ImageButton) dialog.findViewById(R.id.charging);
        ImageButton navigation = (ImageButton) dialog.findViewById(R.id.navigation);

        chargingStation.setText(clusterMarkerLocation.getCSName() + ",");
        chargingPort.setText("port " + clusterMarkerLocation.getPortName());

        place = new String[]{clusterMarkerLocation.getCSName(), clusterMarkerLocation.getPortName()};

        navigation.setOnClickListener(this);
        charging.setOnClickListener(this);

        dialog.show();
        return true;
    }

    /**
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class check extends AsyncTask<String, String, String> {

        /**
         * @param message
         * @return
         */
        @Override
        protected String doInBackground(String... message) {
            Client client = new Client(message[0]);
            String temp = client.getMsg();
            while (temp.equals("404")) {
                temp = client.getMsg();
            }
            return temp;
        }
    }
}
