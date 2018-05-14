package com.example.cnwlc.testchatting.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cnwlc.testchatting.Chatting.ChattingActivity;
import com.example.cnwlc.testchatting.R;

import static com.example.cnwlc.testchatting.Main.FriendsListActivity.clientThread_list;

public class FriendsPushActivity extends Activity {
    ImageView basicImg;
    ImageView myImg;

    TextView nameTv;
    Button cellphoneBtn;
    Button chattingBtn;

    String Sid, SLoginName, Sname, Spath, ScellphoneMe, ScellphoneOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_push);

        setDefine();
    }

    void setDefine() {
        basicImg = (ImageView) findViewById(R.id.basicImgView);

        myImg = (ImageView) findViewById(R.id.myImgView);
        nameTv = (TextView) findViewById(R.id.nameTextView);
        cellphoneBtn = (Button) findViewById(R.id.numberBtn);
        chattingBtn = (Button) findViewById(R.id.chattingBtn);

        Intent intent = getIntent();
        Sid = intent.getStringExtra("id");
        Sname = intent.getStringExtra("name");
        SLoginName = intent.getStringExtra("LoginName");
        Spath = intent.getStringExtra("path");
        ScellphoneMe = intent.getStringExtra("cellphoneMe");
        ScellphoneOther = intent.getStringExtra("cellphoneOther");

        nameTv.setText(Sname);
        Glide.with(this).load(Spath).into(myImg);
        cellphoneBtn.setText(ScellphoneOther);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.numberBtn:
                AlertDialog.Builder dlg = new AlertDialog.Builder(FriendsPushActivity.this);
                dlg.setTitle(" 전화 ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setMessage("친구와 통화하시겠습니까?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + ScellphoneOther));
                                try {
                                    startActivity(callIntent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("fuck");
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                AlertDialog al = dlg.create();
                al.setCanceledOnTouchOutside(false);
                al.show();
                break;
            case R.id.chattingBtn:
                clientThread_list.send("enterroom]" + ScellphoneMe + "]" + ScellphoneOther + "]" + null + "]with");

                Intent chattingIntent = new Intent(getApplicationContext(), ChattingActivity.class);
                chattingIntent.putExtra("push_cellphone_Me", ScellphoneMe);
                chattingIntent.putExtra("push_cellphone_Other", ScellphoneOther);
                chattingIntent.putExtra("push_path", Spath);
                startActivity(chattingIntent);
                break;
        }
    }
}
