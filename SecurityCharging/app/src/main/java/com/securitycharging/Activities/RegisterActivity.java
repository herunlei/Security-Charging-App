package com.securitycharging.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.securitycharging.R;
import com.securitycharging.SupportFiles.Client;

import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(  //validate email format
            "[a-zA-Z0-9+._%-+]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" +
                    "(" +
                    "." +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" +
                    ")+"
    );
    private EditText etUserName;    //username that user entered
    private EditText etPassword;    //password that user entered
    private EditText etEmail;       //email that user entered
    private String run;             //returned message holder

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button bRegister = (Button) findViewById(R.id.sign_up_continue_button);
        etUserName = (EditText) findViewById(R.id.sign_up_username);
        etPassword = (EditText) findViewById(R.id.sign_up_password);
        etEmail = (EditText) findViewById(R.id.sign_up_email);

        bRegister.setOnClickListener(this);
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up_continue_button:  //redirect to payment information activity
                String message = "create," + etUserName.getText() + "//" + etPassword.getText() //collect user information
                        + "//" + etEmail.getText();
                if (checkEmail(etEmail.getText().toString())) { //check if the email user entered is validate or not
                    try {
                        run = new upload().execute(message).get();  //process command to client and record return result
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    if (run.equals("true")) {   //if the return result is true, it means user name or email is available
                        Intent payment = new Intent(this, PaymentActivity.class);
                        payment.putExtra("Username", etUserName.getText().toString());
                        startActivity(payment);
                    } else {
                        Toast.makeText(RegisterActivity.this, "The Username or email already signed!", Toast.LENGTH_SHORT).show();  //the username or email is already signed
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "The email is not validate!", Toast.LENGTH_SHORT).show(); //pop a message if entered email is not in validate format
                }
        }
    }

    /**
     * @param email
     * @return
     */
    private boolean checkEmail(String email) {  //use to check if the email format is correct or not
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    //task to communicate with server and collect returned message
    private class upload extends AsyncTask<String, String, String> {

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
