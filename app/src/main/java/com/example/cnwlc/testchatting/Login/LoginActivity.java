package com.example.cnwlc.testchatting.Login;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.cnwlc.testchatting.Main.FriendsListActivity;
import com.example.cnwlc.testchatting.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity {
    private static String TAG = "LoginActivity";

    private List<String> permissionNeeds = Arrays.asList("email");
    private CallbackManager callbackManager;    // 세션연결 콜백관리자.

    CheckBox AutoLoginCheckBox;
    EditText Eid, Epw;
    String data, Sid, Sname, Spw, Scp, Simgpath, YorN = "N", SavingCp;

    SharedPreferences pref = null;
    SharedPreferences.Editor edit;

    boolean possibleId = false;
    int A;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
        setContentView(R.layout.activity_login);

        Intent getA = getIntent();
        A = getA.getIntExtra("a", 100);
        System.out.println(TAG+" intent_a : "+A);

        setDefine();
    }

    void setDefine() {
        AutoLoginCheckBox = (CheckBox) findViewById(R.id.autologinCB);
        AutoLoginCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AutoLoginCheckBox.isChecked()) {
                    YorN = "Y";
                } else {
                    YorN = "N";
                }
            }
        });

        Eid = (EditText) findViewById(R.id.id);
        Epw = (EditText) findViewById(R.id.pw);
        callbackManager = CallbackManager.Factory.create();     // onActivityResult 설정

        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginBtn:
                // Eid or Epw 가 비었을 경우
                if (Eid.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "아이디를 입력하세요", Toast.LENGTH_SHORT).show();
                } else if (Epw.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                }
                // Eid or Epw 를 채웠을 경우
                else {
                    LoginDB loginDB = new LoginDB();
                    loginDB.execute(Eid.getText().toString(), Epw.getText().toString(), YorN);
                }
                break;
            case R.id.registBtn:
                Intent intent = new Intent(getApplicationContext(), LoginRegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.facebookBtn:
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, permissionNeeds);
                // 페이스북 로그아웃 : http://egloos.zum.com/mightyfine/v/315652
                break;
        }
    }

    // 페이스북 정보 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // id -> db에서 체크 -> onPause에서 페북 아이디 체크용도로 사용
    class FaceBookidDBcheck extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected String doInBackground(String... params) {
            String sid = params[0];
            String serverURL = "http://115.71.238.109/member_idcheck.php";
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
                data = sb.toString().trim();

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

            if (data.equals("1")) {
                Log.w("RESULT", "사용 가능한 아이디");
                possibleId = true;
            } else if (data.equals("0")) {
                Log.w("RESULT", "이미 사용중인 아이디");
                possibleId = false;
            } else {
                Log.w("RESULT", "에러 발생! ERRCODE = " + data);

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("알림");
                builder.setMessage("등록중 에러가 발생했습니다! errcode : " + data);
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
//                                finish();
                    }
                });
                builder.show();
            }
        }
    }

    // facebook id, pw, cellphone, name -> db에 데이터 넣기
    class InsertData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 현재 액티비티, 제목, 메시지, 진행정도 확정/불확정 여부, 취소 가능 여부
        }

        @Override
        protected String doInBackground(String... params) {
            String name = params[0];
            String id = params[1];
            String pw = params[2];
            String cp = params[3];
            String imgpath = params[4];

            String serverURL = "http://115.71.238.109/member_register.php";
            String postParameters = "name=" + name + "&id=" + id + "&pw=" + pw + "&cp=" + cp + "&imgpath=" + imgpath;

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
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                // 읽어온 결과물 리턴. 요청한 URL의 출력물을 BufferedReader로 받는다.
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
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
        }
    }

    // id / pw -> db에서 체크해 login 하기
    class LoginDB extends AsyncTask<String, Void, String> {
        String errorString = null;
        String id, pw, verify;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            id = params[0];
            pw = params[1];
            verify = params[2];
            System.out.println(TAG+" Login verify : "+verify);
            String serverURL = "http://115.71.238.109/member_login.php";
            String postParameters = "id=" + id + "&pw=" + pw + "&verify=" + verify;

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
                System.out.println(TAG+" Login data1 : "+data);

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
            System.out.println(TAG+" data : "+data);

            if (data.equals("0")) {
                Log.w("RESULT", "비밀번호가 일치하지 않습니다.");

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("알림");
                builder.setMessage("비밀번호가 일치하지 않습니다.");
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Epw.setText("");
                    }
                });
                builder.show();
            } else {
                Log.w("RESULT", "성공적으로 처리되었습니다!");
                System.out.println(TAG+" Login data2 : "+ data);

                String[] dataArr = data.split("%");

                edit.putString("id", id); // 입력
                edit.putString("Scp", dataArr[0]);
                edit.putString("imgpath", dataArr[2]);
                edit.commit(); // 파일에 최종 반영함

                // 액티비티 이동
                Intent intent = new Intent(LoginActivity.this, FriendsListActivity.class);
                intent.putExtra("a", A);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Eid.setText("");
        Epw.setText("");

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "로그인이 성공했습니다.");

                // 로그인한 이름과 아이디 저장
                Sname = Profile.getCurrentProfile().getName();
                Sid = Profile.getCurrentProfile().getId();
                Spw = "0000";
                Scp = "01083025038";
                Simgpath = null;

                // 페이스북 아이디 중복 처리
                FaceBookidDBcheck faceBookidDBcheck = new FaceBookidDBcheck();
                faceBookidDBcheck.execute(Sid);

                // 사용 가능한 아이디일 경우 true 로서 DB에 insert 되어짐.
                if (possibleId == true) {
                    InsertData insertData = new InsertData();
                    insertData.execute(Sname, Sid, Spw, Scp, Simgpath);
                }

                LoginDB loginDB = new LoginDB();
                loginDB.execute(Sid, Spw, YorN);
            }

            @Override
            public void onCancel() {
                Log.d("Tag", "로그인 실패");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("Tag", "에러 : " + error.getLocalizedMessage());
            }
        });
    }

    public void onBackPressed() {
        new AlertDialog.Builder(this)
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