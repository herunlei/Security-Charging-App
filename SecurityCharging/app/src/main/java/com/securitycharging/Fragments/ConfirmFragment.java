package com.securitycharging.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.securitycharging.R;
import com.securitycharging.SupportFiles.Client;

import java.util.concurrent.ExecutionException;

public class ConfirmFragment extends Fragment implements View.OnClickListener {

    private String username;        //current user's username
    private String paid;            //current payment amount
    private String run;             //returned message form server
    private double[] destination;   //collection about select charging station location
    private String[] place;         //collection about select charging station information

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        paid = "5"; //faked amount price for charging

        username = getArguments().getString("Username");
        place = getArguments().getStringArray("Place");
        destination = getArguments().getDoubleArray("Destination");
        return inflater.inflate(R.layout.fragment_confirm, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button done = (Button) getActivity().findViewById(R.id.button_done_confirm);
        Button cancel = (Button) getActivity().findViewById(R.id.button_cancel_confirm);
        TextView content = (TextView) getActivity().findViewById(R.id.content_confirm);

        //message that will showing about detail information
        String contents = username + "\n\nReserved charging station " + place[0] + ", port "
                + place[1] + ".\n\n\nTotal: " + paid
                + "\n\n\nThanks for using our service!\n\nSecurity Charging Team";

        content.setText(contents);

        done.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_done_confirm) {    //processes payment
            try {
                run = new confirm().execute("pay," + paid + "//" + username).get(); //collect result that send back form server
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (run.equals("1")) {  //if result is 1, means reservation is done successfully
                FragmentManager fm = getFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putDoubleArray("Destination", destination);
                ResultFragment resultFragment = new ResultFragment();
                resultFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.content_frame, resultFragment).commit(); //show the recipe page
            } else if (run.equals("0")) {   //if result is 0, means reservation is not done successfully
                Toast.makeText(getActivity(), "Confirmation fail, please try again!", Toast.LENGTH_SHORT).show();   //a error message pop up
            }
        } else if (v.getId() == R.id.button_cancel_confirm) {   //cancel current reservation progress
            FragmentManager fragmentManager = getFragmentManager();
            Bundle bundle = new Bundle();
            bundle.putString("Username", username);
            MapsFragment mapsFragment = new MapsFragment();
            mapsFragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.content_frame, mapsFragment).commit();
        }
    }

    private class confirm extends AsyncTask<String, String, String> {

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
