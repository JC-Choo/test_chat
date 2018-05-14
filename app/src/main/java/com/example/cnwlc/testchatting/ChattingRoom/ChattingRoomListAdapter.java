package com.example.cnwlc.testchatting.ChattingRoom;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cnwlc.testchatting.R;

import java.util.ArrayList;

public class ChattingRoomListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ChattingRoomListItem> listDatas = new ArrayList<>();

    public ChattingRoomListAdapter(Context context, ArrayList<ChattingRoomListItem> listDatas) {
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
        TextView t1, t2, t3, t4, t5, t6;
        ImageView i1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_chatting_room, parent, false);

            holder = new ViewHolder();
            holder.t1 = (TextView) convertView.findViewById(R.id.nameView);
            holder.t2 = (TextView) convertView.findViewById(R.id.cpView);
            holder.t3 = (TextView) convertView.findViewById(R.id.dateView);
            holder.t4 = (TextView) convertView.findViewById(R.id.contentView);
            holder.t5 = (TextView) convertView.findViewById(R.id.noView);
            holder.t6 = (TextView) convertView.findViewById(R.id.remainMsg);
            holder.i1 = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        String str = listDatas.get(position).getSimagepath();
        Glide.with(context).load(str).into(holder.i1);

        holder.t1.setText(listDatas.get(position).getSname());
        holder.t2.setText(listDatas.get(position).getScp());
        holder.t3.setText(listDatas.get(position).getSdate());
        holder.t4.setText(listDatas.get(position).getScontent());
        holder.t5.setText(listDatas.get(position).getSno());
        holder.t6.setText(listDatas.get(position).getSre_msg());
        System.out.println("listDatas.get(position).getSre_msg() : "+listDatas.get(position).getSre_msg());
        if(holder.t6.getText().equals("")) {
            holder.t6.setVisibility(View.GONE);
        } else {
            holder.t6.setVisibility(View.VISIBLE);
        }


//        holder.t1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, ChattingActivity.class);
//                intent.putExtra("push_cellphone_Me", Scp);
//                intent.putExtra("push_cellphone_Other", listDatas.get(position).getScp());
//                intent.putExtra("push_path", listDatas.get(position).getSimagepath());
//                context.startActivity(intent);
//                clientThread_list.send("enterroom]" + Scp + "]" + listDatas.get(position).getScp() + "]with");
//            }
//        });

        return convertView;
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(String _no, String _name, String _cp, String _date, String _cont, String _img, String _msg) {
        listDatas.add(new ChattingRoomListItem(_no, _name, _cp, _date, _cont, _img, _msg));
    }
}
