package com.example.unmannedlocker;

import android.app.Activity;
import android.graphics.Point;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class list_key_adaper extends BaseAdapter {
    private final Activity activity;
    private ArrayList<list_key> arrayList = new ArrayList<>();

    public list_key_adaper(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }
    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null)
        {
            LayoutInflater inflater = activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.activity_list_key, parent, false);
        }

        LinearLayout linearLayout = (LinearLayout)convertView;
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        list_key Klist = arrayList.get(position);
        TextView Kname = linearLayout.findViewById(R.id.keyName);
        TextView Kplace = linearLayout.findViewById(R.id.keyPlace);
        TextView Kavailable = linearLayout.findViewById(R.id.keyAvailable);

        //DB 연동 구간
        Kname.setText(Klist.getKeyName());
        Kplace.setText(Klist.getPlace());
        Kavailable.setText(Klist.getAvailable());

        return linearLayout;
    }

    public String getName(int position){
        return arrayList.get(position).getKeyName();
    }

    public String getPlace(int position){
        return arrayList.get(position).getPlace();
    }

    public void add_list(String name, String place, String available){
        list_key Klist = new list_key(name, place, available);
        arrayList.add(Klist);
    }

    public void remove_list(){
        arrayList.clear();
    }
}
