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
 * Created by Marcin on 14.01.2018.
 *
 * We implement here listView and his items
 */
public class wallet_list_create extends BaseAdapter {
    private Activity activity;
    private static ArrayList title,notice;
    private static LayoutInflater inflater = null;

    public wallet_list_create(Activity a, ArrayList b, ArrayList bod) {
        activity = a;
        this.title = b;
        this.notice=bod;

        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

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

        TextView title2 = vi.findViewById(R.id.wallet_name); // title
        String song = title.get(position).toString();
        title2.setText(song);


        TextView title22 = vi.findViewById(R.id.wallet_doges); // notice
        String song2 = notice.get(position).toString();
        title22.setText(song2);

        return vi;
    }
    public void updateView(int position, String title, String balance){
//        View v = getChildAt( yourListView.getFirstVisiblePosition());
////        if(v == null)
////            return;
//
//        TextView title2 = v.findViewById(R.id.wallet_name); // title
//        title2.setText(title);
//        TextView title22 = v.findViewById(R.id.wallet_doges); // notice
//        title22.setText(balance);
    }
}
