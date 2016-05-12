package com.securitycharging.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.securitycharging.R;
import com.securitycharging.SupportFiles.Client;

import java.util.concurrent.ExecutionException;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private String username;    //current user's username

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        username = getArguments().getString("Username");
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView card = (TextView) getActivity().findViewById(R.id.payment_card);
        TextView addCard = (TextView) getActivity().findViewById(R.id.add_new_card);
        TextView history = (TextView) getActivity().findViewById(R.id.payment_history);

        try {
            String cardNumber = new request().execute("card," + username).get();        //request and collect user's payment method to/form server
            String cardHistory = new request().execute("history," + username).get();    //request and collect user's payment history to/form server

            if (cardNumber.equals("null")) {    //if returned message is null, means user has no payment method
                card.setText("no card added");  //then user can add a new payment
                addCard.setVisibility(View.VISIBLE);
            } else {                            //else show the payment information with card type and last 4 digits
                card.setText(cardNumber);
                addCard.setVisibility(View.INVISIBLE);
            }

            if (cardHistory.equals("null")) {           //if returned message is null, means user has no payment history
                history.setText("no history found");    //then show this message
            } else {                                    //otherwise, show the payment history
                System.out.println("The history is: " + cardHistory);
                String[] temp = cardHistory.split("/mark/");
                cardHistory = "";
                for (String aTemp : temp) {
                    cardHistory += aTemp + "\n";
                }
                history.setText(cardHistory);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        addCard.setOnClickListener(this);
    }

    /**
     * ]
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_new_card) {   //redirect to payment page if user click add new card
            FragmentManager fm = getFragmentManager();
            Bundle bundle = new Bundle();
            bundle.putString("Username", username);
            bundle.putString("Type", "account");
            PaymentFragment paymentFragment = new PaymentFragment();
            paymentFragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, paymentFragment);
            fragmentTransaction.commit();
        }
    }

    private class request extends AsyncTask<String, String, String> {

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
