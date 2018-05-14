package com.example.cnwlc.testchatting.Main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cnwlc.testchatting.CarPool.CarPoolActivity;
import com.example.cnwlc.testchatting.ChattingRoom.ChattingRoomActivity;
import com.example.cnwlc.testchatting.More.MoreActivity;
import com.example.cnwlc.testchatting.R;
import com.example.cnwlc.testchatting.Setting.SettingMemberActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class FriendsListActivity extends Activity {
    private static String TAG = "FriendsListActivity";
    final int REQUEST_FRIEND_ENTER = 1;

    private static final String TAG_JSON = "chu";
    private static final String TAG_NAME = "friendname";
    private static final String TAG_CP = "friendcp";
    private static final String TAG_CNT = "cnt";
    private static final String TAG_NO = "friendno";
    private static final String TAG_IMGPATH = "friendimgpath";
    String mJsonString;
    String id, name, cp, cnt, no, imgpath;
    String[] StrBaNo, StrBaName, StrBaId, StrBaCellphone, StrBaImgPath;
    int noOfmember, a;

    ArrayList<FriendsListItem> friendslistitem = new ArrayList<>();
    FriendsListAdapter adapter;
    ListView listView;

    Button deleBtn, settBtn, nameBtn, frieBtn, chatBtn, carpBtn, moreBtn;
    public static String Sid, Scellphone, Simgpath, data;
    ImageView pictView;
    EditText findEt;
    TextView membTv;
    CheckBox deleCB;

    SharedPreferences pref;
    SharedPreferences.Editor edit;

    AlertDialog dialog;

    Thread thread;
    public static Socket client;
    public static ClientThread_List clientThread_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);


        // filebase 사용 -> fcm
        FirebaseInstanceId.getInstance().getToken();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("FCM_Token", token);
        // fcm, 단순히 notice 라는 토픽에 등록하는 과정을 구현, 버튼을 만들어서 버튼을 클릭했을때 수행되게 할 수도 있다.
        // 이 예제는 앱이 실행되면 자동으로 news라는 토픽을 구독한다는 의미다. http://cosmosjs.blog.me/220739141098 참고
        FirebaseMessaging.getInstance().subscribeToTopic("notice");
        // 여기서 topic은 api를 사용하여 푸시 알림 전송시 같은 토픽명 그룹 전체에 메세지를 발송 할 수 있습니다.

        Intent getA = getIntent();
        a = getA.getIntExtra("a", 100);
        System.out.println(TAG + " intent_a : " + a);


        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        Sid = pref.getString("id", "no id");
        Scellphone = pref.getString("Scp", "no Scp");
        Simgpath = pref.getString("imgpath", "no imgpath");
        System.out.println(TAG + " pref.getBoolean(\"RoomOpen\", false) : " + pref.getBoolean("RoomOpen", false));

        thread = new Thread() {
            public void run() {
                connect();
            }
        };
        thread.start();

        LoginName loginDB = new LoginName();
        loginDB.execute(Sid);

        setDefine();
        setListView();
        textFilter();

        GetData getData = new GetData();
        getData.execute(Sid);
    }

    public void connect() {
        try {
            if (a == 1) {
                System.out.println(TAG + " intent_a connect : " + a);
                client = new Socket("115.71.238.109", 8888);
//            client = new Socket("192.168.0.4", 8888);
//            client = new Socket("192.168.1.69", 8888);
//            client = new Socket("192.168.25.18", 8888);
                if (client.isConnected()) {
                    clientThread_list = new ClientThread_List(this, client);
                    clientThread_list.start();
                    clientThread_list.send("in]" + Scellphone + "]with");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setDefine() {
        listView = (ListView) findViewById(R.id.friendslist);
        adapter = new FriendsListAdapter(this, friendslistitem);
        listView.setAdapter(adapter);

        pictView = (ImageView) findViewById(R.id.pictureImgView);
        Glide.with(FriendsListActivity.this).load(Simgpath).into(pictView);

        membTv = (TextView) findViewById(R.id.memberView);
        deleBtn = (Button) findViewById(R.id.deleteBtn);
        settBtn = (Button) findViewById(R.id.settingBtn);
        nameBtn = (Button) findViewById(R.id.nameBtn);

        frieBtn = (Button) findViewById(R.id.friendlistBtn);
        chatBtn = (Button) findViewById(R.id.chattingRoomlistBtn);
        carpBtn = (Button) findViewById(R.id.carpoolBtn);
        moreBtn = (Button) findViewById(R.id.moreBtn);

        findEt = (EditText) findViewById(R.id.findEditText);
        deleCB = (CheckBox) findViewById(R.id.deleteCheckBox);
    }

    void setListView() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                // 액티비티 생성 사진/전화번호/채팅/통화 목록 구현
                Intent intent = new Intent(FriendsListActivity.this, FriendsPushActivity.class);
                intent.putExtra("id", Sid);
                intent.putExtra("cellphoneMe", Scellphone);
                intent.putExtra("LoginName", data);
                intent.putExtra("name", friendslistitem.get(position).getSname());
                intent.putExtra("cellphoneOther", friendslistitem.get(position).getScellphone());
                intent.putExtra("path", friendslistitem.get(position).getSimagepath());
//                intent.putExtra("show_Data", friendslistitem.get(position));
//                intent.putExtra("position", position);
                startActivity(intent);

                clientThread_list.send("makeroom]" + Scellphone + "]" + friendslistitem.get(position).getScellphone() + "]"
                        + Simgpath + "]" + friendslistitem.get(position).getSimagepath() + "]" + null + "]with");
            }
        });
    }

    void textFilter() {
        findEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String search = s.toString();
                if (search.length() > 0)
                    listView.setFilterText(search);
                else
                    listView.clearTextFilter();
            }
        });
    }

    // login 한 이름 가져오기
    class LoginName extends AsyncTask<String, Void, String> {
        String errorString = null;
        String idname;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            idname = params[0];
            String serverURL = "http://115.71.238.109/member_loginToFriendA.php";
            String postParameters = "id=" + idname;

            try {
                URL url = new URL(serverURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                data = sb.toString().trim();
                Log.e("data data : ", data);

                return data;
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "response - " + result);
            nameBtn.setText(data);
        }
    }

    // 버튼 클릭
    public void onClick(View v) {
        switch (v.getId()) {
            /** 맨 위에 관리/설정 버튼 */
            case R.id.deleteBtn:
                // 삭제 체크박스 나오게 하기
                Toast.makeText(getApplicationContext(), "수정 중", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settingBtn:
                LayoutInflater inflater = getLayoutInflater();

                //res폴더>>layout폴더>>dialog_addmember.xml 레이아웃 리소스 파일로 View 객체 생성
                //Dialog의 listener에서 사용하기 위해 final로 참조변수 선언
                final View dialogView = inflater.inflate(R.layout.custom_dialog_setting, null);

                //멤버의 세부내역 입력 Dialog 생성 및 보이기
                AlertDialog.Builder buider = new AlertDialog.Builder(this); //AlertDialog.Builder 객체 생성
                buider.setTitle("설정"); //Dialog 제목
                buider.setIcon(android.R.drawable.ic_menu_add); //제목옆의 아이콘 이미지(원하는 이미지 설정)
                buider.setView(dialogView); //위에서 inflater가 만든 dialogView 객체 세팅 (Customize)

                final Button Bmember = (Button) dialogView.findViewById(R.id.memberBtn);

                Bmember.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent settingIt = new Intent(getApplicationContext(), SettingMemberActivity.class);
                        settingIt.putExtra("setting_id", Sid);
                        startActivityForResult(settingIt, REQUEST_FRIEND_ENTER);

                        dialog.dismiss();
                    }
                });

                //설정한 값으로 AlertDialog 객체 생성
                dialog = buider.create();
                //Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정
                dialog.setCanceledOnTouchOutside(true);
                //Dialog 보이기
                dialog.show();
                break;

            /** 내 프로필 버튼 */
            case R.id.nameBtn:
                // 내 이름 선택 시 프로필 수정 A 띄우기
                Toast.makeText(getApplicationContext(), "수정 중 nameBtn", Toast.LENGTH_SHORT).show();
                break;
            case R.id.pictureImgView:
                // nameBtn 이랑 똑같이
                Toast.makeText(getApplicationContext(), "수정 중 pictureImgView", Toast.LENGTH_SHORT).show();
                break;

            /** 맨 밑 버튼 */
            case R.id.friendlistBtn:
                Intent friendit = new Intent(this, FriendsListActivity.class);
                startActivity(friendit);
                break;
            case R.id.chattingRoomlistBtn:
                Intent chatroomit = new Intent(this, ChattingRoomActivity.class);
                startActivity(chatroomit);
                break;
            case R.id.carpoolBtn:
                Intent carpoolit = new Intent(this, CarPoolActivity.class);
                startActivity(carpoolit);
                break;
            case R.id.moreBtn:
                Intent friendIt = new Intent(this, MoreActivity.class);
                startActivity(friendIt);
                break;
        }
    }

    // settingmember 에서 등록한 친구 받아오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FRIEND_ENTER && resultCode == RESULT_OK) {
            FriendsListItem fli = data.getParcelableExtra("FriendEnter");
            System.out.println("fli : " + fli);

            if (!fli.equals("")) {
                friendslistitem.add(0, fli);
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "친구가 등록되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 가입된 멤버 가져오기
    private class GetData extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String sid = params[0];
            String serverURL = "http://115.71.238.109/member_friendlist.php";
            String postParameters = "id=" + sid;

            try {
                URL url = new URL(serverURL);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                String data = sb.toString().trim();

                return data;
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "response  - " + result);
            mJsonString = result;
            showResult();
        }
    }
    private void showResult() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            JSONObject jsonNo = jsonArray.getJSONObject(0);
            String Sno = jsonNo.getString(TAG_CNT);
            noOfmember = Integer.parseInt(Sno);

            // 입력받은 값(id, name, cp, no)들을 배열로 받기
            StrBaName = new String[noOfmember];
            StrBaId = new String[noOfmember];
            StrBaCellphone = new String[noOfmember];
            StrBaNo = new String[noOfmember];
            StrBaImgPath = new String[noOfmember];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                name = item.getString(TAG_NAME);
                cp = item.getString(TAG_CP);
                cnt = item.getString(TAG_CNT);
                no = item.getString(TAG_NO);
                imgpath = item.getString(TAG_IMGPATH);

                membTv.setText(cnt);

                StrBaId[i] = id;
                StrBaName[i] = name;
                StrBaCellphone[i] = cp;
                StrBaNo[i] = no;
                StrBaImgPath[i] = imgpath;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
//                clientThread_list.send("makeroom]" + Scellphone + "]" + StrBaCellphone[i] + "]" + StrBaImgPath[i] + "]with");

                FriendsListItem smli = new FriendsListItem(StrBaName[i], StrBaCellphone[i], StrBaNo[i], StrBaImgPath[i]);
//                System.out.println("smli : "+smli);
                friendslistitem.add(smli);
                adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    // 뒤로가기 종료
    public void onBackPressed() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("프로그램 종료")
                .setMessage("프로그램을 종료 하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { // 프로세스 종료.
                        moveTaskToBack(true);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                }).setNegativeButton("아니오", null).show();
    }

}
