package com.example.cnwlc.testchatting.Chatting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cnwlc.testchatting.R;

import java.util.ArrayList;

public class ChattingFriendListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ChattingFriendListItem> listDatas = new ArrayList<>();

    public ChattingFriendListAdapter(Context context, ArrayList<ChattingFriendListItem> listDatas) {
        this.context = context;
        this.listDatas = listDatas;
    }

    @Override
    public int getCount() {
        return listDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return listDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class ViewHolder {
        TextView t1, t2;
        ImageView i1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_chatting_friend, parent, false);

            holder = new ViewHolder();
            holder.t1 = (TextView) convertView.findViewById(R.id.NameView);
            holder.t2 = (TextView) convertView.findViewById(R.id.cpView);
            holder.i1 = (ImageView) convertView.findViewById(R.id.PictureImgView);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.t1.setText(listDatas.get(position).getSname());
        holder.t2.setText(listDatas.get(position).getScp());

        String str = listDatas.get(position).getSimagepath();
        Glide.with(context).load(str).into(holder.i1);

        return convertView;
    }
}
