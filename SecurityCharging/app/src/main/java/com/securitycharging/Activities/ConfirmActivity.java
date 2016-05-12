package com.securitycharging.Activities;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.securitycharging.Fragments.ResultFragment;
import com.securitycharging.R;
import com.securitycharging.SupportFiles.Client;

import java.util.concurrent.ExecutionException;

public class ConfirmActivity extends AppCompatActivity implements View.OnClickListener {

    private String username;
    private String paid;
    private String run;
    private String[] destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        paid = "5";

        username = getIntent().getStringExtra("Username");
        String[] place = getIntent().getStringArrayExtra("Place");
        destination = getIntent().getStringArrayExtra("Destination");
        Button done = (Button) findViewById(R.id.button_done_confirm);
        Button cancel = (Button) findViewById(R.id.button_cancel_confirm);
        TextView content = (TextView) findViewById(R.id.content_confirm);

        String contents = username + "\nReserver charging station " + place[0] + ", port "
                + place[1] + ".\n\n\n\nTotal: " + paid
                + "\n\n\nThanks for using our service!\n\nSecurity Charging Team";

        content.setText(contents);

        done.setOnClickListener(this);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_done_confirm) {
            try {
                run = new confirm().execute("pay," + paid + "//" + username).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            if (run.equals("1")) {
                FragmentManager fm = getFragmentManager();
                Bundle bundle = new Bundle();
                bundle.putStringArray("Destination", destination);
                ResultFragment resultFragment = new ResultFragment();
                resultFragment.setArguments(bundle);
                fm.beginTransaction().replace(R.id.content_frame, resultFragment).commit();
                finish();
            } else if (run.equals("0")) {
                Toast.makeText(this, "Confirmation fail, please try again!", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.button_cancel_confirm) {
            finish();
        }
    }

    private class confirm extends AsyncTask<String, String, String> {

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
