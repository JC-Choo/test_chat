package com.example.cnwlc.testchatting.FriendInvite;

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

public class FriendInviteListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<FriendInviteListItem> listDatas = new ArrayList<>();

    public FriendInviteListAdapter(Context context, ArrayList<FriendInviteListItem> listDatas) {
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
            convertView = inflater.inflate(R.layout.listitem_friend_invite, parent, false);

            holder = new ViewHolder();
            holder.t1 = (TextView) convertView.findViewById(R.id.nameTextView);
            holder.t2 = (TextView) convertView.findViewById(R.id.cpTextView);
            holder.i1 = (ImageView) convertView.findViewById(R.id.imgView);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.t1.setText(listDatas.get(position).getSname());
        holder.t2.setText(listDatas.get(position).getScp());

        String str = listDatas.get(position).getSimagepath();
        Glide.with(context).load(str).into(holder.i1);

        return convertView;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String name, String cp, String imgpath) {
        listDatas.add(new FriendInviteListItem(name, cp, imgpath));
    }
}
