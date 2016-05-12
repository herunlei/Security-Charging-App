package com.securitycharging.Fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.securitycharging.R;

public class ResultFragment extends Fragment implements View.OnClickListener {
    private double[] destination;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        destination = getArguments().getDoubleArray("Destination");
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton navigation = (ImageButton) getActivity().findViewById(R.id.navigation_result);

        navigation.setOnClickListener(this);
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.navigation_result) {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + destination[0] + ","
                    + destination[1]);
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }
    }
}
