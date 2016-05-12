package com.securitycharging.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etName;    //card holder name
    private EditText etNumber;  //card number
    private EditText etCVC;     //card security code
    private String etMonth;     //card expiry month
    private String etYear;      //card expiry year
    private String username;    //current user's username

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        username = getIntent().getStringExtra("Username");  //get username from previous activity
        Spinner spinner1 = (Spinner) findViewById(R.id.card_type);  //create spinner for card type
        ArrayList<String> cardType = new ArrayList<>();
        cardType.add("card type");
        cardType.add("Visa");
        cardType.add("MasterCard");
        ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cardType);
        dataAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(dataAdapter1);

        Spinner spinner2 = (Spinner) findViewById(R.id.card_month); //create spinner for expiry month
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
        ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cardMonth);
        dataAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(dataAdapter2);

        Spinner spinner3 = (Spinner) findViewById(R.id.card_year);  //create spinner for expiry year
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
        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cardYear);
        dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(dataAdapter3);

        etName = (EditText) findViewById(R.id.card_holder_name);
        etNumber = (EditText) findViewById(R.id.card_number);
        etCVC = (EditText) findViewById(R.id.card_cvc);
        Button bDone = (Button) findViewById(R.id.button_done);
        Button bCancel = (Button) findViewById(R.id.button_cancel_payment_save);

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                etMonth = cardMonth.get(parent.getSelectedItemPosition()); //collect select month
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                etYear = cardYear.get(parent.getSelectedItemPosition());    //collect select year
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
        if (v.getId() == R.id.button_done) {    //it will create stripe card token if card information correct
            if (checkDone()) {
                Card card = new Card(etNumber.getText().toString(), //create stripe card
                        Integer.parseInt(etMonth),
                        Integer.parseInt(etYear),
                        etCVC.getText().toString());
                if (card.validateCard()) {  //check card is validate or not
                    new Stripe().createToken(   //if validated, create card token
                            card,
                            //this is the test stripe api code
                            //need to replace when test or publish
                            "pk_test_KgEv2RGjJYn29WYKPXOfflnC",
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    try {
                                        new upload().execute("store," + token.getId() + "//" + username).get(); //communicate with server with save payment command
                                    } catch (InterruptedException | ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                }

                                public void onError(Exception error) {
                                    error.printStackTrace();
                                }
                            }
                    );
                } else {
                    Toast.makeText(PaymentActivity.this, "Please enter validate card information!", Toast.LENGTH_SHORT).show(); //pop a message if card is not validate
                }
            } else {
                Toast.makeText(PaymentActivity.this, "Please fill all information that needed!", Toast.LENGTH_SHORT).show();    //pop a message if form is not completely filled
            }
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else if (v.getId() == R.id.button_cancel_payment_save) {  //skip fill payment information if user click
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    /**
     * @return
     */
    private boolean checkDone() {   //check all fields is filled or not
        return (!etName.getText().toString().equals("")) &&
                (!etNumber.getText().toString().equals("")) &&
                (!etCVC.getText().toString().equals("")) &&
                (!etMonth.equals("")) &&
                (!etYear.equals(""));
    }

    //task to communicate with server
    private class upload extends AsyncTask<String, String, String> {

        /**
         * @param message
         * @return
         */
        @Override
        protected String doInBackground(String... message) {
            new Client(message[0]);
            return null;
        }
    }
}
