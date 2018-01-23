package kowoof.dogetracker;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


public class wallet_list extends AppCompatActivity {
    ArrayList<String> title_array = new ArrayList<String>();
    ArrayList<String> notice_array = new ArrayList<String>();
    ListView list;
    wallet_list_create adapter;
    wallet_memory wallet_memory_handler;

    String wallet_name, wallet_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);

        // Make toolbar wow again, I wanted to add here total amount of doges
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
                finish();
                Intent i = new Intent(getApplicationContext(), wallet_add.class);
                startActivity(i);
            }
        });
        wallet_memory_handler = new wallet_memory(getApplicationContext());
        //Find listView and populate it
        list = findViewById(R.id.wallets);
        populate_list();

        //Do something when item from listView is clicked
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), wallet_view.class);
                read_wallet(position);
                i.putExtra("wallet_id", position);
                i.putExtra("wallet_name", wallet_name); //show wallet_view what wallet I wanna see
                i.putExtra("wallet_address", wallet_address); //show wallet_view what wallet I wanna see
                startActivity(i);
                finish();
            }
        });

    }

    public void onResume() {
        super.onResume();
    }

    //populate list with items saved into json
    void populate_list(){
        try {
            JSONArray new_array = new JSONArray(wallet_memory_handler.read_all_wallets());

            for (int i = 0, count = new_array.length(); i < count; i++) {
                try {
                    JSONObject jsonObject = new_array.getJSONObject(i);
                    title_array.add(jsonObject.getString("title"));
                    notice_array.add(jsonObject.getString("notice"));
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

    //read wallet name and address by position
    void read_wallet(int number){
        try {
            JSONArray new_array = new JSONArray(wallet_memory_handler.read_all_wallets());
            JSONObject jsonObject = new_array.getJSONObject(number);
            wallet_name = jsonObject.getString("title");
            wallet_address = jsonObject.getString("address");
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
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, messege_toast, duration);
        toast.show();
    }
}
