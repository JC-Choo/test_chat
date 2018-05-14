package com.example.cnwlc.testchatting.Login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.cnwlc.testchatting.Main.ClientThread_List;
import com.example.cnwlc.testchatting.Main.FriendsListActivity;
import com.example.cnwlc.testchatting.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

public class LoadingActivity extends Activity {
    private static String TAG = "LoadingActivity";
    String data;

    SharedPreferences pref;
    SharedPreferences.Editor edit;
    String Sid;

    AutoLogin autoLogin;
    int a=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        Sid = pref.getString("id", "no id");

        autoLogin = new AutoLogin();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                autoLogin.execute(Sid);
            }
        }, 1000);
    }

    // login 한 이름과 사진 가져오기
    class AutoLogin extends AsyncTask<String, Void, String> {
        String errorString = null;
        String id;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            id = params[0];
            Log.e(TAG, " AutoLogin id : "+id);
            String serverURL = "http://115.71.238.109/member_loading.php";
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
            Log.d(TAG, "result - " + result);
            Log.d(TAG, "data - " + data);
            a=1;
            System.out.println(TAG+" intent_a : "+a);

            if( data.equals("N") ) {
                Log.e("Main data NO : ", data);

                Intent nintent = new Intent(getApplicationContext(), LoginActivity.class);
                nintent.putExtra("a", a);
                startActivity(nintent);
            } else if( data.equals("Y") ){
                Log.e("Main data YES : ", data);

                Intent yintent = new Intent(getApplicationContext(), FriendsListActivity.class);
                yintent.putExtra("a", a);
                startActivity(yintent);
            } else if( data.equals("")  ){
                Log.e("Main data null : ", data);

                Intent nintent = new Intent(getApplicationContext(), LoginActivity.class);
                nintent.putExtra("a", a);
                startActivity(nintent);
            } else {
                Log.e("Main data Error : ", data);
            }
        }
    }
}
