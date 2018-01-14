package kowoof.dogetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class wallet_list extends AppCompatActivity {
    ArrayList<String> title_array = new ArrayList<String>();
    ArrayList<String> notice_array = new ArrayList<String>();
    ListView list;
    wallet_list_create adapter;

    static String result = "[\n" +
            "  {\n" +
            "    \"title\": \"First doge\",\n" +
            "    \"notice\": \"many value\",\n" +
            "    \"address\": \"D8c2fhkh26bLGshWuYChyKNugMZ4nG34uq\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"title\": \"Second doge\",\n" +
            "    \"notice\": \"many value\",\n" +
            "    \"address\": \"D8c2fhkh26bLGshWuYChyKNugMZ4nG34uq\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"title\": \"Third doge\",\n" +
            "    \"notice\": \"many value\",\n" +
            "    \"address\": \"D8c2fhkh26bLGshWuYChyKNugMZ4nG34uq\"\n" +
            "  }\n" +
            "]";
    String wallet_name, wallet_address;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);

        // Make toolbar great again, I wanted to add here total amount of doges
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("My wallets");
        toolbar.setSubtitle("Total: 1337Đ ~ 20,05$");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Add wallet button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), wallet_add.class);
                startActivity(i);
                //finish();
            }
        });

        //Find listView and populate it
        list = findViewById(R.id.wallets);
        populate_list();

        //Do something when item from listView is clicked
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO here we need to add going to wallet view after clicking name of that wallet
                Intent i = new Intent(getApplicationContext(), wallet_view.class);
                read_wallet(position);
                i.putExtra("wallet_name", wallet_name); //show wallet_view what wallet I wanna see
                i.putExtra("wallet_address", wallet_address); //show wallet_view what wallet I wanna see
                startActivity(i);
            }
        });
    }

    //testing with test json populating listView
    void populate_list(){
        String response = result.toString();
        try {
            JSONArray new_array = new JSONArray(response);

            for (int i = 0, count = new_array.length(); i < count; i++) {
                try {
                    JSONObject jsonObject = new_array.getJSONObject(i);
                    title_array.add(jsonObject.getString("title").toString());
                    notice_array.add(jsonObject.getString("notice").toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapter = new wallet_list_create(wallet_list.this, title_array, notice_array);
            list.setAdapter(adapter);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //testing with test json populating listView
    void read_wallet(int number){
        String response = result.toString();
        try {
            JSONArray new_array = new JSONArray(response);
            JSONObject jsonObject = new_array.getJSONObject(number);
            wallet_name = jsonObject.getString("title").toString();
            wallet_address = jsonObject.getString("address").toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Letting come back home
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    // Last but not least, useful stuff to make app working
    public void make_toast(String messege_toast){
        Context context = getApplicationContext();
        CharSequence text = messege_toast;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
