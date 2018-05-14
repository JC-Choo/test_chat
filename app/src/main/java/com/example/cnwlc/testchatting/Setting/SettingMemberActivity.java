package com.example.cnwlc.testchatting.Setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cnwlc.testchatting.Main.FriendsListActivity;
import com.example.cnwlc.testchatting.Main.FriendsListItem;
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

public class SettingMemberActivity extends Activity {
    private static String TAG = "phptest_SettingMemberActivity";
    private static final String TAG_JSON = "chu";
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_CELLPHONE = "cp";
    private static final String TAG_CNT = "cnt";
    private static final String TAG_FRIEND_NO = "no";
    private static final String TAG_IMGPATH = "imgpath";

    ArrayList<SettingMemberListItem> arrayList = new ArrayList<>();
    SettingMemberListAdapter settingMemberListAdapter;
    ListView listview;

    String mJsonString;
    String id, name, cp, cnt, no;
    String loginID, friendName, friendCP, friendImgpath, friendNo, imgpath;
    String[] StrBaName, StrBaId, StrBaCellphone, StrBaFriendNo, StrBaImgPath;
    int noOfmember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_member);

        setDefine();
        setListView();

        Intent getID = getIntent();
        loginID = getID.getStringExtra("setting_id");

        GetData task = new GetData();
        task.execute(loginID);
    }
    void setDefine() {
        listview = (ListView) findViewById(R.id.memberlist);
        settingMemberListAdapter = new SettingMemberListAdapter(this, arrayList);
        listview.setAdapter(settingMemberListAdapter);
    }
    void setListView() {
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if( arrayList.get(position).getSname().equals("등록할 친구가 없습니다.") ) {

                } else {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(SettingMemberActivity.this);
                    dlg.setTitle(" 친구등록 ")
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage("선택한 친구를 등록하시겠습니까?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    friendName = StrBaName[position];
                                    friendCP = StrBaCellphone[position];
                                    friendImgpath = StrBaImgPath[position];
                                    friendNo = StrBaFriendNo[position];

                                    InsertData insertData = new InsertData();
                                    insertData.execute(loginID, friendNo, friendName, friendCP, friendImgpath);
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
                }
            }
        });
    }

    // 내아이디, 친구이름, 친구전화번호, 친구사진 -> db에 데이터 넣기
    class InsertData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 현재 액티비티, 제목, 메시지, 진행정도 확정/불확정 여부, 취소 가능 여부
        }
        @Override
        protected String doInBackground(String... params) {
            String myid = params[0];
            String friendno = params[1];
            String friendname = params[2];
            String friendcp = params[3];
            String friendimgpath = params[4];

            String serverURL = "http://115.71.238.109/member_meANDfriend.php";
            String postParameters = "myname=" + myid + "&friendno=" + friendno + "&frname=" + friendname + "&cp=" + friendcp + "&path=" + friendimgpath;

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
                Log.d(TAG, "POST response code - " + responseStatusCode);

                // [2-3]. 연결 요청 확인.
                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                // 읽어온 결과물 리턴. 요청한 URL의 출력물을 BufferedReader로 받는다.
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while( (line = bufferedReader.readLine()) != null ) {
                    sb.append(line);
                }

                bufferedReader.close();
                return sb.toString();
            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }
        }

        // 백그라운드 작업이 완료된 후 결과값을 얻습니다.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "POST response  - " + result);

            FriendsListItem smli = new FriendsListItem(friendName, friendCP, friendNo, friendImgpath);

            Intent enterIntent = new Intent(getApplicationContext(), FriendsListActivity.class);
            enterIntent.putExtra("FriendEnter", smli);
            setResult(RESULT_OK, enterIntent);
            finish();
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
            String serverURL = "http://115.71.238.109/member_total.php";
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
            if(jsonArray.length() > 0) {
                JSONObject jsonNo = jsonArray.getJSONObject(0);
                String Sno = jsonNo.getString(TAG_CNT);
                noOfmember = Integer.parseInt(Sno);

                // 입력받은 값(id, name, cp, no)들을 배열로 받기
                StrBaName = new String[noOfmember];
                StrBaId = new String[noOfmember];
                StrBaCellphone = new String[noOfmember];
                StrBaFriendNo = new String[noOfmember];
                StrBaImgPath = new String[noOfmember];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject item = jsonArray.getJSONObject(i);

                    id = item.getString(TAG_ID);
                    name = item.getString(TAG_NAME);
                    cp = item.getString(TAG_CELLPHONE);
                    cnt = item.getString(TAG_CNT);
                    no = item.getString(TAG_FRIEND_NO);
                    imgpath = item.getString(TAG_IMGPATH);

                    StrBaId[i] = id;
                    StrBaName[i] = name;
                    StrBaCellphone[i] = cp;
                    StrBaFriendNo[i] = no;
                    StrBaImgPath[i] = imgpath;
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    SettingMemberListItem smli = new SettingMemberListItem(StrBaName[i], StrBaImgPath[i]);
                    arrayList.add(smli);
                    settingMemberListAdapter.notifyDataSetChanged();
                }

                for(int i=0; i<noOfmember; i++) {
                    Log.w("StrBaImgPath[" + i + "] : ", StrBaImgPath[i]);
                }
            } else {
                SettingMemberListItem smli = new SettingMemberListItem("등록할 친구가 없습니다.", null);
                arrayList.add(smli);
                settingMemberListAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}
