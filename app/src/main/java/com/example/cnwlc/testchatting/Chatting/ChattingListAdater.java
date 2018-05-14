package com.example.cnwlc.testchatting.Chatting;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cnwlc.testchatting.R;

import java.util.ArrayList;

public class ChattingListAdater extends BaseAdapter {

    private Context context;
    private ArrayList<ChattingListItem> m_List = new ArrayList<>();

    public ChattingListAdater(Context context, ArrayList<ChattingListItem> listDatas) {
        this.context = context;
        this.m_List = listDatas;
    }

    @Override
    public int getCount() {
        return m_List.size();
    }

    @Override
    public Object getItem(int position) {
        return m_List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class CustomHolder {
        TextView m_NameTextView, m_ContTextView, m_TimeTextView, m_DateTextView;
        LinearLayout layout;
        View viewRight, viewLeft;
        ImageView imgView, sendimgView;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CustomHolder holder;
        // 리스트가 길어지면서 현재 화면에 보이지 않는 아이템은 converView가 null인 상태로 들어 옴
        if (convertView == null) {
            // view가 null일 경우 커스텀 레이아웃을 얻어 옴
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listitem_chatting, parent, false);

            // 홀더 생성 및 Tag로 등록
            holder = new CustomHolder();
            holder.m_NameTextView = (TextView) convertView.findViewById(R.id.nameTvL);
            holder.m_ContTextView = (TextView) convertView.findViewById(R.id.text);
            holder.m_TimeTextView = (TextView) convertView.findViewById(R.id.time);
            holder.m_DateTextView = (TextView) convertView.findViewById(R.id.date);
            holder.layout = (LinearLayout) convertView.findViewById(R.id.layout);
            holder.viewRight = (View) convertView.findViewById(R.id.imageViewright);
            holder.viewLeft = (View) convertView.findViewById(R.id.imageViewleft);
            holder.imgView = (ImageView) convertView.findViewById(R.id.imgView);
            holder.sendimgView = (ImageView) convertView.findViewById(R.id.sendImgView);
            convertView.setTag(holder);
        } else {
            holder = (CustomHolder) convertView.getTag();
        }

        // Text 등록
        holder.m_NameTextView.setText(m_List.get(position).getSname());
        holder.m_ContTextView.setText(m_List.get(position).getScontent());
        holder.m_TimeTextView.setText(m_List.get(position).getStime());
        holder.m_DateTextView.setText(m_List.get(position).getSdate());
        String str = m_List.get(position).getSimagepath();
        Glide.with(context).load(str).into(holder.imgView);

//        String strSend = m_List.get(position).getSsendimagepath();
//        System.out.println("strSend : "+strSend);
//        Glide.with(context).load(strSend).into(holder.sendimgView);
//        if( strSend != null ) {
//            sendimg.setVisibility(View.VISIBLE);
//        } else {
//            sendimg.setVisibility(View.GONE);
//        }


        // 0 : 상대방 / 1 : 본인 / 2: 중앙
        if (m_List.get(position).type == 0) {
            holder.m_NameTextView.setVisibility(View.VISIBLE);
            holder.m_ContTextView.setBackgroundResource(R.drawable.inbox2);
            holder.m_TimeTextView.setVisibility(View.VISIBLE);
            holder.m_DateTextView.setVisibility(View.GONE);
            holder.layout.setGravity(Gravity.LEFT);
            holder.viewRight.setVisibility(View.GONE);
            holder.viewLeft.setVisibility(View.GONE);
        } else if (m_List.get(position).type == 1) {
            holder.m_NameTextView.setVisibility(View.GONE);
            holder.m_ContTextView.setBackgroundResource(R.drawable.outbox2);
            holder.m_TimeTextView.setVisibility(View.GONE);
            holder.m_DateTextView.setVisibility(View.VISIBLE);
            holder.layout.setGravity(Gravity.RIGHT);
            holder.viewRight.setVisibility(View.GONE);
            holder.viewLeft.setVisibility(View.GONE);
        } else if (m_List.get(position).type == 2) {
            holder.m_ContTextView.setBackgroundResource(R.drawable.datebg);
            holder.layout.setGravity(Gravity.CENTER);
            holder.viewRight.setVisibility(View.VISIBLE);
            holder.viewLeft.setVisibility(View.VISIBLE);
        } else if (m_List.get(position).type == 3) {
            System.out.println("m_List.get(position).getScontent() : "+m_List.get(position).getScontent());
            System.out.println("holder.m_ContTextView : "+holder.m_ContTextView.getText());
            holder.m_ContTextView.setBackgroundResource(R.drawable.datebg);
            holder.layout.setGravity(Gravity.CENTER);
            holder.viewRight.setVisibility(View.GONE);
            holder.viewLeft.setVisibility(View.GONE);
        }

        return convertView;
    }

    // 외부에서 아이템 추가 요청 시 사용
    public void add(String _name, String _msg, String _time, String _date, String _imgpath, int _type) {
        m_List.add(new ChattingListItem(_name, _msg, _time, _date, _imgpath, _type));
    }

    // 외부에서 아이템 삭제 요청 시 사용
    public void remove(int _position) {
        m_List.remove(_position);
    }
}