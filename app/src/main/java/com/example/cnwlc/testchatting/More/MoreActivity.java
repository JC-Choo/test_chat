package com.example.cnwlc.testchatting.More;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.cnwlc.testchatting.CarPool.CarPoolActivity;
import com.example.cnwlc.testchatting.Chatting.ChattingListItem;
import com.example.cnwlc.testchatting.ChattingRoom.ChattingRoomActivity;
import com.example.cnwlc.testchatting.Login.LoginActivity;
import com.example.cnwlc.testchatting.Main.FriendsListActivity;
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

public class MoreActivity extends Activity{
    String TAG = "MOREACTIVITY";
    String data;

    SharedPreferences pref;
    SharedPreferences.Editor edit;
    String sid;

    TextView tvName, tvCp, tvId, tvName2, tvCp2, tvId2;
    ImageView myImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        sid = pref.getString("id", "no id");
        Log.e(TAG, "sid : "+sid);

        setDefine();
    }
    public void setDefine() {
        tvName = (TextView) findViewById(R.id.nameTv);
        tvCp = (TextView) findViewById(R.id.cpTv);
        tvId = (TextView) findViewById(R.id.idTv);
        tvName2 = (TextView) findViewById(R.id.nameTv2);
        tvCp2 = (TextView) findViewById(R.id.cpTv2);
        tvId2 = (TextView) findViewById(R.id.idTv2);
        myImg = (ImageView) findViewById(R.id.myImgView);

        tvId2.setText(sid);

        LoginName loginName = new LoginName();
        loginName.execute(sid);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.modifyBtn :
                Toast.makeText(getApplicationContext(), "수정중입니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.logoutBtn :
                AlertDialog.Builder builder = new AlertDialog.Builder(MoreActivity.this);
                builder.setTitle("알림");
                builder.setMessage("로그아웃 하시겠습니까?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Logout logout = new Logout();
                        logout.execute(sid);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                break;

            /** 맨 밑 버튼 */
            case R.id.friendlistBtn :
                Intent friendit = new Intent(this, FriendsListActivity.class);
                startActivity(friendit);
                break;
            case R.id.chattingRoomlistBtn :
                // 채팅방 목록 A 띄우기
                Intent chatroomit = new Intent(this, ChattingRoomActivity.class);
                startActivity(chatroomit);
                break;
            case R.id.carpoolBtn :
                // 채널 목록 A 띄우기(인터넷, 블로그만)
                Intent carpoolit = new Intent(this, CarPoolActivity.class);
                startActivity(carpoolit);
                break;
            case R.id.moreBtn :
                Intent friendIt = new Intent(this, MoreActivity.class);
                startActivity(friendIt);
                break;
        }
    }

    // login 한 이름과 사진 가져오기
    class LoginName extends AsyncTask<String, Void, String> {
        String errorString = null;
        String id;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            id = params[0];
            String serverURL = "http://115.71.238.109/login_information.php";
            String postParameters = "id=" + id;

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
                Log.e(TAG+" data : ", data);

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

            if (data.contains("%")) {
                Log.e("Main data contain(%) : ", data);
                String[] idNameImg = data.split("%");
                tvName2.setText(idNameImg[0]);
                tvCp2.setText(idNameImg[1]);
                if( idNameImg[2].equals("null") ) {
                    idNameImg[2] = "http://115.71.238.109/upload/basic.jpg";
                }
                Glide.with(MoreActivity.this).load(idNameImg[2]).into(myImg);
            } else {
                Log.e("Main data : ", data);
            }
        }
    }

    // 로그아웃하기
    class Logout extends AsyncTask<String, Void, String> {
        String errorString = null;
        String id, verify;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            id = params[0];
            String serverURL = "http://115.71.238.109/member_logout.php";
            String postParameters = "id=" + id;;

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

                while ( (line = bufferedReader.readLine()) != null ) {
                    sb.append(line);
                }

                bufferedReader.close();
                data = sb.toString().trim();
                Log.e(TAG, " data : "+data);

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

            if(data.equals("Success")) {
                Intent no = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(no);
            } else {
                Log.e(TAG, " error : "+data);
            }
        }
    }
}
