package com.securitycharging.Activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.securitycharging.R;
import com.securitycharging.SupportFiles.Client;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etUserName;    //username that user enter
    private EditText etPassword;    //password that user enter
    private String run;             //feedback result from server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUserName = (EditText) findViewById(R.id.sign_in_username);
        etPassword = (EditText) findViewById(R.id.sign_in_password);
        Button bLogin = (Button) findViewById(R.id.sign_in_button);
        TextView registerLink = (TextView) findViewById(R.id.sign_up);

        //set up listener to listen if content is clicked or not
        bLogin.setOnClickListener(this);
        registerLink.setOnClickListener(this);
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:   //if the clicked content is sign in button
                try {
                    run = new check().execute("login," + etUserName.getText() + "//"    //pass the command to the server through client and get result back
                            + etPassword.getText()).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                if (run.equals("1")) {  //if returned result is 1, it mean account create successfully
                    etPassword.setText(""); //set password field to blank for security
                    Intent menu = new Intent(this, MenuActivity.class);  //create a new activity intent to start
                    menu.putExtra("Username", etUserName.getText().toString()); //pass username to the new activity to make sure we are work on the same user for the next activities
                    startActivity(menu);
                } else {
                    Toast.makeText(LoginActivity.this,  //pop a message if account is not validated or enter wrong information
                            "Account information is wrong or not activated, please try again",
                            Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.sign_up:
                startActivity(new Intent(this, RegisterActivity.class));    //start new user register activity if user try to create a account
        }
    }

    //to communicate with server through app client
    private class check extends AsyncTask<String, String, String> {

        /**
         * @param message
         * @return
         */
        @Override
        protected String doInBackground(String... message) {
            Client client = new Client(message[0]); //create a new client for communicating with server
            String temp = client.getMsg();  //temporary variable for holding message come back form server
            while (temp.equals("404")) {    //get message until communication be finished
                temp = client.getMsg();
            }
            return temp;    //return the message
        }
    }
}
