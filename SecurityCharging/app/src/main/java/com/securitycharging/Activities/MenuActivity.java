package com.securitycharging.Activities;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.securitycharging.Fragments.AccountFragment;
import com.securitycharging.Fragments.MapsFragment;
import com.securitycharging.R;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String username;    //username form previous activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = getIntent().getStringExtra("Username");  //username form previous activity
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fm = getFragmentManager();  //create a new map fragment that will show the map
        Bundle bundle = new Bundle(); //create a new bundle to holding variables that pass to another activity or fragment
        bundle.putString("Username", username); //put current user's username in the bundle
        MapsFragment mapsFragment = new MapsFragment();
        mapsFragment.setArguments(bundle);
        fm.beginTransaction().replace(R.id.content_frame, mapsFragment).commit();   //start map fragment when menu already loaded
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        FragmentManager fm = getFragmentManager();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_account) {   //it will show user's payment method and payment history if user select account profit
            Bundle bundle = new Bundle();
            bundle.putString("Username", username);
            AccountFragment accountFragment = new AccountFragment();
            accountFragment.setArguments(bundle);
            fm.beginTransaction().replace(R.id.content_frame, accountFragment).commit();
        } else if (id == R.id.nav_ev_station) { //it will show the map
            Bundle bundle = new Bundle();
            bundle.putString("Username", username);
            MapsFragment mapsFragment = new MapsFragment();
            mapsFragment.setArguments(bundle);
            fm.beginTransaction().replace(R.id.content_frame, mapsFragment).commit();
        } else if (id == R.id.nav_setting) {    //setting option
            // TODO: do something
        } else if (id == R.id.logout) { //logout
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
