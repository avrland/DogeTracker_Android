package kowoof.dogetracker;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 * We implement here listView and his items
 */
public class wallet_list_create extends BaseAdapter {
    private static ArrayList title,notice;
    private static LayoutInflater inflater = null;

    wallet_list_create(Activity a, ArrayList b, ArrayList bod) {
        title = b;
        notice = bod;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return title.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.row_listitem, null);

        TextView wallet_name_list_textView = vi.findViewById(R.id.wallet_name); // title
        String wallet_name_item = title.get(position).toString();
        wallet_name_list_textView.setText(wallet_name_item);


        TextView wallet_balance_list_textView = vi.findViewById(R.id.wallet_doges); // notice
        String wallet_balance_item = notice.get(position).toString();
        wallet_balance_list_textView.setText(wallet_balance_item);

        return vi;
    }
}
