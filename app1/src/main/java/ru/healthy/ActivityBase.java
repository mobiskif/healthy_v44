package ru.healthy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class ActivityBase extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    private FirebaseAnalytics mFirebaseAnalytics;
    int id_content;
    String top_text;
    String btn_text;
    int spinner_id;
    int spinner_arr;
    int card_arr;
    int list_arr;
    String TAG = "jop";
    API api;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TextView mWelcomeTextView;

    public ActivityBase() {
        super();
        api = new API(this);
        id_content = R.layout.activity_base;
        spinner_arr = R.array.lpu;
        card_arr = R.array.lpu;
        list_arr = R.array.lpu;
    }

    void init() {
        top_text = "bla-bla";
        btn_text = "bla";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(id_content);


        mWelcomeTextView = findViewById(R.id.label1);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "Нажата FloatingActionButton", Snackbar.LENGTH_LONG).show();
            }
        });

        TextView textView = findViewById(R.id.textview);
        textView.setOnClickListener(this);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(this);

        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        /*** Request to Base BEGIN ***/
        //String[] spins = getResources().getStringArray(spinner_arr);
        String[] spins = api.getStringArray("doc");
        /*** Request to Base END ***/
        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<>(this, R.layout.item_spinner, spins);
        spinner_adapter.setDropDownViewResource(R.layout.item_spinner);
        spinner.setAdapter(spinner_adapter);

        RecyclerView mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        /*** Request to Base BEGIN ***/
        String[] cards = getResources().getStringArray(card_arr);
        /*** Request to Base END ***/
        mRecyclerView.setAdapter(new CardAdapter(cards, this, btn_text));

        ListView listView = findViewById(R.id.list);
        listView.setOnItemClickListener(this);
        /*** Request to Base BEGIN ***/
        String[] lists = getResources().getStringArray(list_arr);
        /*** Request to Base END ***/
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lists);
        listView.setAdapter(adapter);

        //fetchWelcome();
        init();
    }

    public void fetchWelcome() {
        mWelcomeTextView.setText(mFirebaseRemoteConfig.getString("loading_phrase"));

        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will retrieve values from the service.
        long cacheExpiration = 3600; // 1 hour in seconds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled())
            cacheExpiration = 0;

        // cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        // will use fetch data from the Remote Config service, rather than cached parameter values,
        // if cached parameter values are more than cacheExpiration seconds old.
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(activity, "Fetch Succeeded", Toast.LENGTH_SHORT).show();
                            Log.d("jop", getClass().getName() + " fetch успешный");
                            FirebaseCrash.logcat(Log.DEBUG, TAG, getClass().getName() + " fetch успешный");

                            // After config data is successfully fetched, it must be activated before newly fetched values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            //Toast.makeText(activity, "Fetch Failed", Toast.LENGTH_SHORT).show();
                            Log.d("jop", getClass().getName() + " fetch NOT успешный");
                        }
                        displayWelcome();
                    }
                });
    }

    private void displayWelcome() {
        String welcomeMessage = mFirebaseRemoteConfig.getString("welcome_message");
        mWelcomeTextView.setText(welcomeMessage);
    }


    @Override
    public void onClick(View v) {
        //Toast.makeText(getApplicationContext(), "onClick", Toast.LENGTH_LONG).show();
        mFirebaseAnalytics.setUserProperty("current_area", "Кирровский");
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "onClick");
        bundle.putString(FirebaseAnalytics.Param.VALUE, v.toString());
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(getApplicationContext(), "onItemClick", Toast.LENGTH_LONG).show();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "onItemClick");
        bundle.putString(FirebaseAnalytics.Param.VALUE, ""+position);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        //Toast.makeText(getApplicationContext(), "onItemSelected", Toast.LENGTH_LONG).show();
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "onItemSelected");
        bundle.putString(FirebaseAnalytics.Param.VALUE, ""+position);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //Toast.makeText(this, data.getStringExtra("result"), Toast.LENGTH_SHORT).show();
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "onActivityResult");
            bundle.putString(FirebaseAnalytics.Param.VALUE, data.getDataString());
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        }

    }

}
