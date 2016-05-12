package com.securitycharging.Fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.securitycharging.R;
import com.securitycharging.SupportFiles.Client;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class PaymentFragment extends Fragment implements View.OnClickListener {

    private String username;
    private double[] destination;
    private String[] place;
    private EditText etName;
    private EditText etNumber;
    private EditText etCVC;
    private String etMonth;
    private String etYear;
    private String type;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        username = getArguments().getString("Username");
        type = getArguments().getString("Type");
        destination = getArguments().getDoubleArray("Destination");
        place = getArguments().getStringArray("Place");
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner1 = (Spinner) getActivity().findViewById(R.id.card_type_frag);
        ArrayList<String> cardType = new ArrayList<>();
        cardType.add("card type");
        cardType.add("Visa");
        cardType.add("MasterCard");
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, cardType);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter1);

        Spinner spinner2 = (Spinner) getActivity().findViewById(R.id.card_month_frag);
        final ArrayList<String> cardMonth = new ArrayList<>();
        cardMonth.add("month");
        cardMonth.add("1");
        cardMonth.add("2");
        cardMonth.add("3");
        cardMonth.add("4");
        cardMonth.add("5");
        cardMonth.add("6");
        cardMonth.add("7");
        cardMonth.add("8");
        cardMonth.add("9");
        cardMonth.add("10");
        cardMonth.add("11");
        cardMonth.add("12");
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, cardMonth);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter2);

        Spinner spinner3 = (Spinner) getActivity().findViewById(R.id.card_year_frag);
        final ArrayList<String> cardYear = new ArrayList<>();
        cardYear.add("year");
        cardYear.add("2016");
        cardYear.add("2017");
        cardYear.add("2018");
        cardYear.add("2019");
        cardYear.add("2020");
        cardYear.add("2021");
        cardYear.add("2022");
        cardYear.add("2023");
        cardYear.add("2024");
        cardYear.add("2025");
        cardYear.add("2026");
        cardYear.add("2027");
        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, cardYear);
        dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(dataAdapter3);

        etName = (EditText) getActivity().findViewById(R.id.card_holder_name_frag);
        etNumber = (EditText) getActivity().findViewById(R.id.card_number_frag);
        etCVC = (EditText) getActivity().findViewById(R.id.card_cvc_frag);
        Button bDone = (Button) getActivity().findViewById(R.id.button_done_frag);
        Button bCancel = (Button) getActivity().findViewById(R.id.button_cancel_payment_save_frag);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                etMonth = cardMonth.get(parent.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                etYear = cardYear.get(parent.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        bDone.setOnClickListener(this);
        bCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_done_frag) {
            if (checkDone()) {
                Card card = new Card(etNumber.getText().toString(),
                        Integer.parseInt(etMonth),
                        Integer.parseInt(etYear),
                        etCVC.getText().toString());
                if (card.validateCard()) {
                    new Stripe().createToken(
                            card,
                            "pk_test_KgEv2RGjJYn29WYKPXOfflnC",
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    String run = null;
                                    try {
                                        run = new upload().execute("store," + token.getId() + "//" + username).get();
                                    } catch (InterruptedException | ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                    if (run.equals("1")) {
                                        FragmentManager fm = getFragmentManager();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("Username", username);
                                        if (type.equals("account")) {
                                            AccountFragment accountFragment = new AccountFragment();
                                            accountFragment.setArguments(bundle);
                                            fm.beginTransaction().replace(R.id.content_frame, accountFragment).commit();
                                        } else if (type.equals("map")) {
                                            ConfirmFragment confirmFragment = new ConfirmFragment();
                                            bundle.putStringArray("Place", place);
                                            bundle.putDoubleArray("Destination", destination);
                                            confirmFragment.setArguments(bundle);
                                            fm.beginTransaction().replace(R.id.content_frame, confirmFragment).commit();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(),
                                                "Create payment fail, please try again!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }

                                public void onError(Exception error) {
                                    error.printStackTrace();
                                }
                            }
                    );
                } else {
                    Toast.makeText(getActivity(),
                            "Please enter validate card information!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(),
                        "Please fill all information that needed!", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.button_cancel_payment_save_frag) {
            FragmentManager fm = getFragmentManager();
            Bundle bundle = new Bundle();
            bundle.putString("Username", username);
            if (type.equals("account")) {
                AccountFragment accountFragment = new AccountFragment();
                accountFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.content_frame, accountFragment).commit();
            } else if (type.equals("map")) {
                MapsFragment mapsFragment = new MapsFragment();
                fm.beginTransaction().replace(R.id.content_frame, mapsFragment).commit();
            }
        }
    }

    private boolean checkDone() {
        return (!etName.getText().toString().equals("")) &&
                (!etNumber.getText().toString().equals("")) &&
                (!etCVC.getText().toString().equals("")) &&
                (!etMonth.equals("")) &&
                (!etYear.equals(""));
    }

    private class upload extends AsyncTask<String, String, String> {

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
