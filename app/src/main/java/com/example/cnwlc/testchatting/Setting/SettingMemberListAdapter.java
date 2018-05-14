package com.example.cnwlc.testchatting.Setting;

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

public class SettingMemberListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SettingMemberListItem> listDatas = new ArrayList<>();

    public SettingMemberListAdapter(Context context, ArrayList<SettingMemberListItem> listDatas) {
        this.context = context;
        this.listDatas = listDatas;
    }

    // 리스트 뷰의 전체 아이템의 수
    @Override
    public int getCount() {
        return listDatas.size();
    }

    // 리스트뷰의 포지션에 맞는 아이템을 보여줌
    @Override
    public Object getItem(int position) {
        return listDatas.get(position);
    }

    // 리스트뷰의 포지션에 맞는 아이템의 아이디를 보여줌
    @Override
    public long getItemId(int position) {
        return position;
    }

    // ViewHolder
    class ViewHolder {
        TextView t1;
        ImageView i1;
    }
    // data set안에 특정 position의 data가 있는 View를 얻는 것이며, 아이템과 xml을 연결하여 화면에 표시해주는 부분.
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.listitem_member, null);

            holder = new ViewHolder();
            holder.t1 = (TextView) convertView.findViewById(R.id.NameView);
            holder.i1 = (ImageView) convertView.findViewById(R.id.PictureImgView);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        String str = listDatas.get(position).getSimagepath();
        Glide.with(context).load(str).into(holder.i1);

        holder.t1.setText(listDatas.get(position).getSname());

        return convertView;
    }
}
