package com.example.cnwlc.testchatting.FriendInvite;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.cnwlc.testchatting.Chatting.ChattingActivity;
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

import static com.example.cnwlc.testchatting.Main.FriendsListActivity.clientThread_list;

public class FriendInviteActivity extends Activity {
    private static String TAG = "FriendInviteActivity";
    private static final String TAG_JSON = "chu";
    private static final String TAG_CNT = "cnt";
    private static final String TAG_NAME = "friendname";
    private static final String TAG_CP = "friendcp";
    private static final String TAG_IMGPATH = "friendimgpath";

    String mJsonString, cnt, name, cp, imgpath;
    String[] StrBaName, StrBaCellphone, StrBaImgPath;
    int noOfmember;

    ArrayList<FriendInviteListItem> friendInviteListItems = new ArrayList<>();
    FriendInviteListAdapter adapter;
    ListView listView;

    SharedPreferences pref;
    SharedPreferences.Editor edit;

    String Sid, Scp, Simgpath, getCp, getImg, choiceCp="", choiceImgpath="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_invite);

        setDefine();
        setListView();
    }
    void setDefine() {
        Intent getIntentCp = getIntent();
        getCp = getIntentCp.getStringExtra("room_cp");
        getImg = getIntentCp.getStringExtra("room_img");

        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        Sid = pref.getString("id", "no id");

        listView = (ListView) findViewById(R.id.friends_invite_List);
        adapter = new FriendInviteListAdapter(this, friendInviteListItems);
        listView.setAdapter(adapter);

        GetData getData = new GetData();
        getData.execute(Sid);
    }
    void setListView() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getApplicationContext(), "hihi"+position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 버튼 클릭
    public void onClick(View v) {
        switch (v.getId()) {
            /** 맨 위에 관리/설정 버튼 */
            case R.id.cancelBtn :
                finish();
                break;
            case R.id.okBtn:
                // 선택한 친구들 String으로 한번에 묶기
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                int count = adapter.getCount();
                for (int i = count-1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        choiceCp += friendInviteListItems.get(i).getScp() + ".";
                        System.out.println(TAG+" choiceCp = "+choiceCp);
                        choiceImgpath += friendInviteListItems.get(i).getSimagepath() + "%";
                        System.out.println(TAG+" choiceImgpath = "+choiceImgpath);
                    }
                }
                // 맨 뒤에 . 이랑 % 없애기
                String[] splitgetCp = choiceCp.split("[.]");
                String getCp2="";
                for(int i=0; i<splitgetCp.length; i++) {
                    if(i < splitgetCp.length-1) {
                        splitgetCp[i+1] = splitgetCp[i]+"."+splitgetCp[i+1];
                        getCp2 = splitgetCp[i+1];
                    } else if(splitgetCp.length == 1) {
                        getCp2 = splitgetCp[i];
                    }
                }
                String[] splitgetImg = choiceImgpath.split("[%]");
                String getImg2="";
                for(int i=0; i<splitgetImg.length; i++) {
                    if(i < splitgetImg.length-1) {
                        splitgetImg[i+1] = splitgetImg[i]+"%"+splitgetImg[i+1];
                        getImg2 = splitgetImg[i+1];
                    } else if(splitgetImg.length == 1) {
                        getImg2 = splitgetImg[i];
                    }
                }

                // 모든 선택 상태 초기화.
                listView.clearChoices() ;
                adapter.notifyDataSetChanged();
                System.out.println(TAG+" getCp.getCp2 = "+getCp+"."+getCp2);
                System.out.println(TAG+" getImg%getImg2 = "+getImg+"%"+getImg2);

                String[] splitgetCp2 = getCp.split("[.]");
                Scp = splitgetCp2[0];
                for(int i=splitgetCp2.length-1; i>0; i--) {
                    getCp2 = splitgetCp2[i]+"."+getCp2;
                }

                String[] splitgetImg2 = getImg.split("[%]");
                Simgpath = splitgetImg2[0];
                for(int i=splitgetImg2.length-1; i>0; i--) {
                    getImg2 = splitgetImg2[i]+"%"+getImg2;
                }

                System.out.println(TAG+" Scp = "+Scp);
                System.out.println(TAG+" getCp2 = "+getCp2);
                System.out.println(TAG+" Scp.getCp2 = "+Scp+"."+getCp2);
                System.out.println(TAG+" Simgpath%getImg2 = "+Simgpath+"%"+getImg2);

                clientThread_list.send("makeroom]" + Scp + "]" + getCp2 + "]" + Simgpath + "]" + getImg2 + "]" + null + "]with");
                clientThread_list.send("enterroom]" + Scp + "]" + getCp2 + "]" + null + "]with");

                Intent chattingIntent = new Intent(getApplicationContext(), ChattingActivity.class);
                chattingIntent.putExtra("push_cellphone_Me", Scp);
                chattingIntent.putExtra("push_cellphone_Other", getCp2);
                chattingIntent.putExtra("push_path", getImg2);
                startActivity(chattingIntent);
                break;
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
            StrBaCellphone = new String[noOfmember];
            StrBaImgPath = new String[noOfmember];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                name = item.getString(TAG_NAME);
                cp = item.getString(TAG_CP);
                cnt = item.getString(TAG_CNT);
                imgpath = item.getString(TAG_IMGPATH);

                StrBaName[i] = name;
                StrBaCellphone[i] = cp;
                StrBaImgPath[i] = imgpath;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                FriendInviteListItem smli = new FriendInviteListItem(StrBaName[i], StrBaCellphone[i], StrBaImgPath[i]);
                friendInviteListItems.add(smli);
                adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }
}
