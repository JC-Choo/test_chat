package com.example.cnwlc.testchatting.ChattingRoom;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cnwlc.testchatting.CarPool.CarPoolActivity;
import com.example.cnwlc.testchatting.Chatting.ChattingActivity;
import com.example.cnwlc.testchatting.Main.FriendsListActivity;
import com.example.cnwlc.testchatting.More.MoreActivity;
import com.example.cnwlc.testchatting.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.example.cnwlc.testchatting.Chatting.ChattingActivity.my_n;
import static com.example.cnwlc.testchatting.Main.FriendsListActivity.clientThread_list;

public class ChattingRoomActivity extends Activity {

    private static String TAG = "ChattingRoomActivity";
    private static final String TAG_JSON = "chu";
    private static final String TAG_CNT = "cnt";
    private static final String TAG_NO = "no";
    private static final String TAG_NAME = "name";
    private static final String TAG_CP = "other_cp";
    private static final String TAG_DATE = "last_send_date";
    private static final String TAG_CONT = "last_send_cont";
    private static final String TAG_IMGPATH = "img_path";
    int noOfmember;
    String mJsonString, data, _name;
    String no, name, cp, date, cont, imgpath;
    public static String[] StrBaNo;
    String[] StrBaName, StrBaCp, StrBaDate, StrBaCont, StrBaImgpath;

    ArrayList<ChattingRoomListItem> chattingRoomListItems = new ArrayList<>();
    ChattingRoomListAdapter chattingRoomListAdapter;
    ListView listview;

    SharedPreferences pref;
    SharedPreferences.Editor edit;

    public static String Scp, Simgpath;
    public static Handler handler_ChattingRoomActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chtting_room_list);

        setDefine();
        setListView();
    }
    @SuppressLint("HandlerLeak")
    void setDefine() {
        chattingRoomListAdapter = new ChattingRoomListAdapter(this, chattingRoomListItems);
        listview = (ListView) findViewById(R.id.chatting_room_List);
        listview.setAdapter(chattingRoomListAdapter);

        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        Scp = pref.getString("Scp", "no Scp");
        Simgpath = pref.getString("imgpath", "no imgpath");

        GetData getData = new GetData();
        getData.execute(Scp);

        // handler로 읽지 않은 메세지 가져오기
        handler_ChattingRoomActivity = new Handler() {
            @Override
            public void handleMessage(Message message) {
                super.handleMessage(message);

                Bundle bundle = message.getData();
                String send_name = bundle.getString("name");
                String say_msg = bundle.getString("msg");
                String re_msg_count = bundle.getString("re_msg_count");
                String room_title = bundle.getString("room_title");
                String enter_member = bundle.getString("enter_member");
                String re_roomN = bundle.getString("roomNumber");

                System.out.println(TAG+" handler_send_name : "+send_name);
                System.out.println(TAG+" handler_say_msg : "+say_msg);
                System.out.println(TAG+" handler_re_msg_count : "+re_msg_count);
                System.out.println(TAG+" handler_room_title : "+room_title);
                System.out.println(TAG+" handler_enter_member : "+enter_member);
                System.out.println(TAG+" handler_re_roomN : "+re_roomN);

                for (int i = 0; i < StrBaNo.length; i++) {
                    if ( StrBaNo[i].equals(re_roomN) ) {
                        ChattingRoomListItem smli = new ChattingRoomListItem(StrBaNo[i], StrBaName[i], StrBaCp[i], StrBaDate[i], say_msg, StrBaImgpath[i], re_msg_count);
                        chattingRoomListItems.set(i, smli);
                        chattingRoomListAdapter.notifyDataSetChanged();

                        System.out.println(TAG+" handler__name : "+_name);
                        System.out.println(TAG+" handler_Scp : "+Scp);
                        System.out.println(TAG+" handler_my_n : "+my_n);

                        // 밥 먹고 와서 채팅 자바 소스에서 "re_msg]보낸사람번호]내용]보낸개수]방번호" 순서로 해서 보내자
                        // 그리고 나온 사람 번호로 이름 찾아서 my_n에 넣어주고 그거랑 firebaseMessagingService에서 비교해서 자신한테 안오게끔 하자.

                        Fcm_Msg fcm_msg = new Fcm_Msg();
                        fcm_msg.execute(Scp, send_name, say_msg, room_title, enter_member, re_roomN);
                    }
                }
            }
        };
    }
    void setListView() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(ChattingRoomActivity.this, ChattingActivity.class);
                intent.putExtra("push_cellphone_Me", Scp);
                intent.putExtra("push_cellphone_Other", chattingRoomListItems.get(position).getScp());
                intent.putExtra("push_path", chattingRoomListItems.get(position).getSimagepath());
                intent.putExtra("push_no", chattingRoomListItems.get(position).getSno());
                startActivity(intent);

                clientThread_list.send("enterroom]" + Scp + "]" + chattingRoomListItems.get(position).getScp() + "]" + chattingRoomListItems.get(position).getSno() + "]with");
            }
        });
    }

    // 채팅한 멤버 가져오기
    private class GetData extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String scp = params[0];
            String serverURL = "http://115.71.238.109/chatting_room.php";
            String postParameters = "cp=" + scp;

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
                Log.d(TAG + " data : ", data);

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
            Log.d(TAG, "chattingroom result  - " + result);
            Log.d(TAG, "chattingroom data  - " + data);

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
            StrBaNo = new String[noOfmember];
            StrBaName = new String[noOfmember];
            StrBaCp = new String[noOfmember];
            StrBaDate = new String[noOfmember];
            StrBaCont = new String[noOfmember];
            StrBaImgpath = new String[noOfmember];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                no = item.getString(TAG_NO);
                name = item.getString(TAG_NAME);
                cp = item.getString(TAG_CP);
                date = item.getString(TAG_DATE);
                cont = item.getString(TAG_CONT);
                imgpath = item.getString(TAG_IMGPATH);

                System.out.println("ChattingRoomA no : " + no);
                System.out.println("ChattingRoomA cp : " + cp);
                System.out.println("ChattingRoomA date : " + date);

                StringTokenizer st = new StringTokenizer(date, ".");
                String[] arr = new String[st.countTokens()];
                for (int j = 0; st.hasMoreTokens(); j++) {
                    arr[j] = st.nextToken();
                }
                if (!date.equals("null")) {
                    date = arr[1];
                }
                System.out.println("ChattingRoomA date2 : " + date);

                StrBaNo[i] = no;
                StrBaName[i] = name;
                StrBaCp[i] = cp;
                StrBaDate[i] = date;
                StrBaCont[i] = cont;
                StrBaImgpath[i] = imgpath;

                System.out.println("ChattingRoomA StrBaName[" + i + "] : " + StrBaName[i]);
                System.out.println("ChattingRoomA StrBaCp[" + i + "] : " + StrBaCp[i]);
                System.out.println("ChattingRoomA StrBaDate[" + i + "] : " + StrBaDate[i]);
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                if (!StrBaCont[i].equals("null") && !StrBaDate[i].equals("null")) {
                    ChattingRoomListItem smli = new ChattingRoomListItem(StrBaNo[i], StrBaName[i], StrBaCp[i], StrBaDate[i], StrBaCont[i], StrBaImgpath[i], null);
                    chattingRoomListItems.add(smli);
                    chattingRoomListAdapter.notifyDataSetChanged();
                }
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                clientThread_list.send("makeroom]" + Scp + "]" + StrBaCp[i] + "]" + Simgpath + "]" + StrBaImgpath[i] + "]" + StrBaNo[i] + "]with");
            }

            // 방 개설 후 shared로 값 저장해서 다시 만드는거 불가능하게
            // jsonArray.length() 값을 shared 해서 그 값이랑 비교했을때 같으면 만들지 않고 다르면 다시 makeroom 실시~
        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    // fcm으로 msg 보내기
    class Fcm_Msg extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            System.out.println("params[0] : "+params[0]);
            System.out.println("params[1] : "+params[1]);
            System.out.println("params[2] : "+params[2]);
            System.out.println("params[3] : "+params[3]);
            System.out.println("params[4] : "+params[4]);
            System.out.println("params[5] : "+params[5]);

            String serverURL = "http://115.71.238.109/fcm_push_notification.php";
            String postParameters = "cp=" + params[0] + "&name=" + params[1] + "&message=" + params[2]
                    + "&room_title=" + params[3] + "&enter_member=" + params[4] + "&re_roomN=" + params[5];

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));   // 출력 스트림에 출력
                outputStream.flush();   // 출력 스트림을 플러시(비운다)하고 버퍼링 된 모든 출력 바이트를 강제 실행
                outputStream.close();   // 출력 스트림을 닫고 모든 시스템 자원을 해제

                int responseStatusCode = httpURLConnection.getResponseCode();

                // [2-3]. 연결 요청 확인.
                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while( (line = bufferedReader.readLine()) != null ) {
                    sb.append(line);
                }

                bufferedReader.close();
                data = sb.toString().trim();
                Log.e("RECV DATA",data);
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
            }
            return null;
        }

        // 백그라운드 작업이 완료된 후 결과값을 얻습니다.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "POST response  - " + result);

            /* 서버에서 응답 */
            Log.e("RECV DATA",data);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            /** 맨 위에 관리/설정 버튼 */
            case R.id.deleteBtn:
                // 삭제 체크박스 나오게 하기
                Toast.makeText(getApplicationContext(), "수정 중", Toast.LENGTH_SHORT).show();
                break;

            /** 맨 밑 버튼 */
            case R.id.friendlistBtn:
                Intent friendit = new Intent(this, FriendsListActivity.class);
                startActivity(friendit);
                break;
            case R.id.chattingRoomlistBtn:
                // 채팅방 목록 A 띄우기
                Intent chatroomit = new Intent(this, ChattingRoomActivity.class);
                startActivity(chatroomit);
                break;
            case R.id.carpoolBtn:
                // 채널 목록 A 띄우기(인터넷, 블로그만)
                Intent carpoolit = new Intent(this, CarPoolActivity.class);
                startActivity(carpoolit);
                break;
            case R.id.moreBtn:
                Intent friendIt = new Intent(this, MoreActivity.class);
                startActivity(friendIt);
                break;
        }
    }
}
