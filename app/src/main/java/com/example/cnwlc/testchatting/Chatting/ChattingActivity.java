package com.example.cnwlc.testchatting.Chatting;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.cnwlc.testchatting.ChattingRoom.ChattingRoomActivity;
import com.example.cnwlc.testchatting.FriendInvite.FriendInviteActivity;
import com.example.cnwlc.testchatting.Main.FriendsPushActivity;
import com.example.cnwlc.testchatting.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.cnwlc.testchatting.Main.FriendsListActivity.clientThread_list;

public class ChattingActivity extends AppCompatActivity {
    private static String TAG = "ChattingActivity";

    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_GALLERY = 1;
    //    private static final int PICK_FROM_DRAW = 2;
//    private static final int PICK_FROM_GALLERY_MOVIE = 3;
    String selectedImagePath = null;

    ArrayList<ChattingListItem> listitem = new ArrayList<>();
    ChattingListAdater adapter;
    ListView listView;

    public static int a;
    int c = 0;
    EditText edit_input;
    public static Handler handler_ChattingActivity;
    // 개발자가 정의한 쓰레드는 절대 UI를 제어할 수 없음. 즉 메인쓰레드의 역할을 침범할 수 없음. 따라서 Handler를 통해 원하는 UI제어를 부탁하면 됨.

    // 시간 (년-월-일-요일) 받아오는 String
    long now = System.currentTimeMillis();
    Date date = new Date(now);
    String getYMD, getTime;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 E요일");
    SimpleDateFormat sdfTime = new SimpleDateFormat("a hh:mm");

    // 채팅 가져오기
    private static final String TAG_JSON = "chu";
    private static final String TAG_CONT = "chat_cont";
    private static final String TAG_TIME = "chat_date";
    private static final String TAG_ID = "member_id";
    private static final String TAG_ROOM = "chat_room";
    private static final String TAG_CNT = "cnt";
    private static final String TAG_NAME = "name";
    private static final String TAG_CP = "cp";
    private static final String TAG_IMG = "img_path";
    private static final String TAG_CHAT_IMG = "imgpath";
    int noOfmember;
    String data, mJsonString, SavingChattingName, SavingChattingImgpath, Savingcontent, Savingtime, Savingid, Savingroom, Savingname, Savingcp, Savingimgpath, myname, othername;
    String[] StrBaChattingName, StrBaChattingImgpath, StrBaContent, StrBaTime, StrBaId, StrBaRoom, dateTime, StrBaName, StrBaCp, StrBaImg;

    LinearLayout linearLayout;

    ImageButton imgBtn;

    public static String ScpMe, ScpOther, Simgpath, SroomNo;
    String msg, getCp = "";

    // 채팅방 안 메뉴
    LinearLayout menu, more;
    ArrayList<ChattingFriendListItem> chattingFriendListItemArrayList = new ArrayList<>();
    ChattingFriendListAdapter chattingFriendListAdapter;
    ListView friendList;
    Animation Showleft, Hideleft;
    ImageButton outimgbtn;

    public static String _name, _img, content, my_n, my_i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        setDefine();
        setListView();
        setFriendListView();
    }
    @SuppressLint("HandlerLeak")
    void setDefine() {
        linearLayout = (LinearLayout) findViewById(R.id.layoutLinear);

        edit_input = (EditText) findViewById(R.id.contentEdit);
        imgBtn = (ImageButton) findViewById(R.id.plusImgBtn);
        imgBtn.setImageResource(R.drawable.plus);

        adapter = new ChattingListAdater(this, listitem);
        listView = (ListView) findViewById(R.id.contentListView);
        listView.setAdapter(adapter);

        Intent getidcp = getIntent();
        ScpMe = getidcp.getStringExtra("push_cellphone_Me");
        ScpOther = getidcp.getStringExtra("push_cellphone_Other");
        Simgpath = getidcp.getStringExtra("push_path");
        SroomNo = getidcp.getStringExtra("push_no");

        System.out.println(TAG+" ScpMe : "+ScpMe);
        System.out.println(TAG+" ScpOther : "+ScpOther);
        System.out.println(TAG+" SroomNo : "+SroomNo);

        // 로그인 정보 불러오기
        LoginDB loginDB = new LoginDB();
        loginDB.execute(ScpMe);

        // 채팅 내역 불러오기
        GetContent getContent = new GetContent();
        getContent.execute(ScpMe+"."+ScpOther, SroomNo);

        // 시간 구현
        getYMD = sdf.format(date);
        adapter.add(null, getYMD, null, null, null, 2);
        /**
         * shared 써서 getYMD 저장한다음에 저장한 값이랑 실제로 받아온 값이랑 thread로 비교해서 다르면 다시 저장하도록 설계
         */
        // 실시간 시간 구하기
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        getTime = sdfTime.format(new Date(System.currentTimeMillis()));
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        });
        thread.start();

        handler_ChattingActivity = new Handler() {
            @Override
            public void handleMessage(Message message) {
                /*메인액티비티의 EditText에 메세지 출력*/
                Bundle bundle = message.getData();
                content = bundle.getString("msg");

                // 대화방 초대 메시지 가져오기
                my_n = bundle.getString("name");
                my_i = bundle.getString("img");
                System.out.println("content : "+content);
                System.out.println("my_n : "+my_n);
                System.out.println("my_i : "+my_i);
                if( my_n == null ) {
                    System.out.println("msg == null: "+content);
                    refresh(null, content, null, null, null, 3);
                    return;
                }

                if (a == 1) { // 본인
                    System.out.println("content a=1: "+content);
                    refresh(my_n, content, getTime, getTime, null, 1);
                } else { // 상대방
                    System.out.println("content a=0: "+content);
                    refresh(my_n, content, getTime, getTime, my_i, 0);
                }
                a = 0;
            }
        };

        /* 액션바 정의 */
//        getSupportActionBar().setTitle("Memo");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFF000000));

        // 채팅 안에 사람 보기
        chattingFriendListAdapter = new ChattingFriendListAdapter(this, chattingFriendListItemArrayList);
        friendList = (ListView) findViewById(R.id.friendList);
        friendList.setAdapter(chattingFriendListAdapter);

        menu = (LinearLayout) findViewById(R.id.menuLinear);
        more = (LinearLayout) findViewById(R.id.moreLinear);

        Hideleft = AnimationUtils.loadAnimation(this, R.anim.left_out);
        Showleft = AnimationUtils.loadAnimation(this, R.anim.left_in);

        outimgbtn = (ImageButton) findViewById(R.id.outImgBtn);

        GetMember getMember = new GetMember();
        getMember.execute(ScpMe+"."+ScpOther);
    }
    void setListView() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

            }
        });
    }
    void setFriendListView() {
        ChattingFriendListItem smli = new ChattingFriendListItem("대화상대 초대", null, null);
        chattingFriendListItemArrayList.add(0, smli);
        chattingFriendListAdapter.notifyDataSetChanged();

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if(position == 0) {
                    for(int i=1; i<friendList.getCount(); i++) {
                        getCp += chattingFriendListItemArrayList.get(i).getScp() + ".";
                    }
                    String[] splitgetCp = getCp.split("[.]");
                    String getCp2="";
                    for(int i=0; i<splitgetCp.length; i++) {
                        if(i < splitgetCp.length-1) {
                            splitgetCp[i+1] = splitgetCp[i]+"."+splitgetCp[i+1];
                            getCp2 = splitgetCp[i+1];
                        }
                    }

                    String getImg = "";
                    for(int i=1; i<friendList.getCount(); i++) {
                        getImg += chattingFriendListItemArrayList.get(i).getSimagepath() + "%";
                    }
                    String[] splitgetImg = getImg.split("[%]");
                    String getImg2="";
                    for(int i=0; i<splitgetImg.length; i++) {
                        if(i < splitgetImg.length-1) {
                            splitgetImg[i+1] = splitgetImg[i]+"%"+splitgetImg[i+1];
                            getImg2 = splitgetImg[i+1];
                        }
                    }

                    Intent intent = new Intent(ChattingActivity.this, FriendInviteActivity.class);
                    intent.putExtra("room_cp", getCp2);
                    intent.putExtra("room_img", getImg2);
                    startActivity(intent);
                } else if (position == 1) {

                } else {
                    Intent intent = new Intent(ChattingActivity.this, FriendsPushActivity.class);
                    intent.putExtra("cellphoneMe", ScpMe);
                    intent.putExtra("cellphoneOther", chattingFriendListItemArrayList.get(position).getScp());
                    intent.putExtra("path", chattingFriendListItemArrayList.get(position).getSimagepath());
                    startActivity(intent);

                    clientThread_list.send("makeroom]" + ScpMe + "]" + chattingFriendListItemArrayList.get(position).getScp() + "]"
                            + Simgpath + "]" + chattingFriendListItemArrayList.get(position).getSimagepath() + "]" + null + "]with");
                }
            }
        });
    }



    // 핸들러 메모리 부족으로 인한 새 객체 생성
//    private final MyHandler mHandler = new MyHandler(this);
//
//    // 핸들러 객체 만들기 Handler static inner class
//    public static class MyHandler extends Handler {
//        private final WeakReference<ChattingActivity> mActivity;
//
//        public MyHandler(ChattingActivity activity) {
//            mActivity = new WeakReference<ChattingActivity>(activity);
//        }
//        @Override
//        public void handleMessage(Message msg) {
//            ChattingActivity activity = mActivity.get();
//            if (activity != null) {
//                activity.handleMessage(msg);
//            }
//        }
//    }
//    // Handler 에서 호출하는 함수
//    private void handleMessage(Message message) {
//        /*메인액티비티의 EditText에 메세지 출력*/
//        Bundle bundle = message.getData();
//        String msg = bundle.getString("msg");
//
//        // 대화방 초대 메시지 가져오기
//        String my_n = bundle.getString("name");
//        String my_i = bundle.getString("img");
//        System.out.println("msg : "+msg);
//        System.out.println("my_n : "+my_n);
//        System.out.println("my_i : "+my_i);
//        Toast.makeText(getApplicationContext(), "my_n : "+my_n, Toast.LENGTH_SHORT).show();
//        if( my_n == null ) {
//            System.out.println("msg == null: "+msg);
//            refresh(null, msg, null, null, null, 3);
//            return;
//        }
//
//        if (a == 1) { // 본인
//            System.out.println("msg a=1: "+msg);
//            refresh(my_n, msg, getTime, getTime, null, 1);
//        } else { // 상대방
//            System.out.println("msg a=0: "+msg);
//            refresh(my_n, msg, getTime, getTime, my_i, 0);
//        }
//        a = 0;
//    }



    //액션바
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chatting, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_more:
                menu.setVisibility(View.VISIBLE);
                menu.startAnimation(Showleft);
                more.setVisibility(View.VISIBLE);

                more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menu.startAnimation(Hideleft);
                        menu.setVisibility(View.GONE);
                        more.setVisibility(View.GONE);
                    }
                });
                outimgbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "채팅방 나가기 버튼", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    //액션바 끝

    // 채팅내용, 시간, type(내건지 아닌지) 넣기
    private void refresh(String inputName, String inputContent, String inputTime, String inputDate, String inputImgpath, int _str) {
        adapter.add(inputName, inputContent, inputTime, inputDate, inputImgpath, _str);
        adapter.notifyDataSetChanged();
    }

    // 버튼
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendBtn:
                if(SroomNo == null) {
                    msg = "say]" + ScpMe + "]" + ScpOther + "]" + edit_input.getText().toString() + "]" + null + "]" + _name + "]" + _img + "]with";
                } else {
                    msg = "say]" + ScpMe + "]" + ScpOther + "]" + edit_input.getText().toString() + "]" + SroomNo + "]" + _name + "]" + _img + "]with";
                }
                clientThread_list.send(msg);
                edit_input.setText("");
                a = 1;
                break;
            case R.id.plusImgBtn:
                if (c == 0) {
                    linearLayout.setVisibility(View.GONE);
                    c = 1;
                } else {
                    linearLayout.setVisibility(View.VISIBLE);
                    c = 0;
                }
                break;
            case R.id.cameraBtn:
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    // 안드로이드 카메라 가이드
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                try {
                    cameraIntent.putExtra("return-data", true);
                    startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }
                linearLayout.setVisibility(View.GONE);
                break;
            case R.id.galleryBtn:
                Intent galleryIntent = new Intent();
                // Gallery 호출
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                try {
                    galleryIntent.putExtra("return-data", true);
                    startActivityForResult(Intent.createChooser(galleryIntent, "Complete action using"), PICK_FROM_GALLERY);
                } catch (ActivityNotFoundException e) {
                }
                linearLayout.setVisibility(View.GONE);
                break;
        }
    }

    // cp -> db에서 체크해 이름/사진 가져오기
    class LoginDB extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = "http://115.71.238.109/chattingA_refresh_member.php";
            String postParameters = "cp=" + params[0];

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

                return sb.toString().trim();
            } catch (Exception e) {
                Log.d(TAG, "LoginDB: Error ", e);
                errorString = e.toString();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "response - " + result);

            String[] dataArr = result.split("%");
            for(int i=0; i<dataArr.length; i++) {
                System.out.println(TAG+" loginDB "+i+" : "+dataArr[i]);
            }

            _name = dataArr[0];
            _img = dataArr[1];
        }
    }

    // db 에서 대화방 명단 불러오기
    private class GetMember extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            String scp = params[0];
            String serverURL = "http://115.71.238.109/chatting_room_member.php";
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
                Log.d("GetMember Data : ", data);

                return data;
            } catch (Exception e) {
                Log.d(TAG, "GetMember: Error ", e);
                errorString = e.toString();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "response  - " + result);
            mJsonString = result;
            showMember();
        }
    }
    private void showMember() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            System.out.println("ChattingA jsonObject : "+jsonObject);
            System.out.println("ChattingA jsonArray : "+jsonArray);

            JSONObject jsonNo = jsonArray.getJSONObject(0);
            System.out.println("ChattingA jsonNo : "+jsonNo);
            String Sno = jsonNo.getString(TAG_CNT);
            noOfmember = Integer.parseInt(Sno);
            System.out.println("ChattingA noOfmember : "+noOfmember);

            StrBaName = new String[noOfmember];
            StrBaCp = new String[noOfmember];
            StrBaImg = new String[noOfmember];

            System.out.println("ChattingA jsonArray.length() : "+jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                Savingname = item.getString(TAG_NAME);
                Savingcp = item.getString(TAG_CP);
                Savingimgpath = item.getString(TAG_IMG);

                StrBaName[i] = Savingname;
                StrBaCp[i] = Savingcp;
                StrBaImg[i] = Savingimgpath;

                System.out.println("GetMember StrBaName["+ i +"] : "+StrBaName[i]);
                System.out.println("GetMember StrBaCp["+ i +"] : "+StrBaCp[i]);
                System.out.println("GetMember StrBaImg["+ i +"] : "+StrBaImg[i]);
            }
            for (int i = 0; i < jsonArray.length(); i++) {
//                if(Savingid != null) {
                    ChattingFriendListItem smli = new ChattingFriendListItem(StrBaName[i], StrBaCp[i], StrBaImg[i]);
                    System.out.println("GetMember smli 2 : " + smli);
                    chattingFriendListItemArrayList.add(smli);
                    chattingFriendListAdapter.notifyDataSetChanged();
//                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "showResult : ", e);
        }
    }

    // db 에서 내용 불러오기
    private class GetContent extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            System.out.println(TAG+" GetContent params[0] : "+params[0]);
            System.out.println(TAG+" GetContent params[1] : "+params[1]);

            String serverURL = "http://115.71.238.109/chatting_content.php";
            String postParameters = "chat_room=" + params[0]+"&room_no="+params[1];

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
                Log.d("GetContent data : ", data);

                return data;
            } catch (Exception e) {
                Log.d(TAG, "GetContent: Error ", e);
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
            StrBaChattingName = new String[noOfmember];
            StrBaChattingImgpath = new String[noOfmember];
            StrBaContent = new String[noOfmember];
            StrBaTime = new String[noOfmember];
            StrBaId = new String[noOfmember];
            StrBaRoom = new String[noOfmember];

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);

                SavingChattingName = item.getString(TAG_NAME);
                SavingChattingImgpath = item.getString(TAG_CHAT_IMG);
                Savingcontent = item.getString(TAG_CONT);
                Savingtime = item.getString(TAG_TIME);
                Savingid = item.getString(TAG_ID);
                Savingroom = item.getString(TAG_ROOM);
                dateTime = Savingtime.split("[.]");

                StrBaChattingName[i] = SavingChattingName;
                StrBaChattingImgpath[i] = SavingChattingImgpath;
                StrBaContent[i] = Savingcontent;
                StrBaTime[i] = dateTime[1];
                StrBaId[i] = Savingid;
                StrBaRoom[i] = Savingroom;
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                if(StrBaContent[i].startsWith("enter]")){
                    String[] cont = StrBaContent[i].split("[]]");

                    adapter.add(null, cont[1], null, null, null, 3);
                    adapter.notifyDataSetChanged();
                    continue;
                }
                if (StrBaId[i].equals(ScpMe) && !StrBaContent[i].startsWith("enter]")) {
                    myname = StrBaChattingName[i];
                    adapter.add(StrBaChattingName[i], StrBaContent[i], StrBaTime[i], StrBaTime[i], null, 1);
                    adapter.notifyDataSetChanged();
                } else {
                    othername = StrBaChattingName[i];
                    adapter.add(StrBaChattingName[i], StrBaContent[i], StrBaTime[i], StrBaTime[i], StrBaChattingImgpath[i], 0);
                    adapter.notifyDataSetChanged();
                }
            }
            /**
             * 날짜 오후 11:59 -> 오전 00:00으로 바뀔 경우 날짜 변환
             */
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
            String name = params[0];
            String message = params[1];
            String cp = params[2];

            String serverURL = "http://115.71.238.109/fcm_push_notification.php";
            String postParameters = "name=" + name + "&message=" + message + "&cp=" + cp;

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

    // 사진찍고 저장 및 불러오기, 갤러리에서 사진 불러오기 정의, bit맵 저장
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA) {
                if (data != null) {
                    Uri cameraUri = data.getData();
                    selectedImagePath = getPath(cameraUri);
                }
            }
            if (requestCode == PICK_FROM_GALLERY) {
                Uri galleryUri = data.getData();
                selectedImagePath = getPath(galleryUri);
            }
            Log.e("selectedImagePath : ", selectedImagePath);
            System.out.println("selectedImagePath sysout : " + selectedImagePath);
        }
    }
    public String getPath(Uri uri) {
        // uri가 null일경우 null반환
        if (uri == null) {
            return null;
        }
        // 미디어스토어에서 유저가 선택한 사진의 URI를 받아온다.
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // URI경로를 반환한다.
        return uri.getPath();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if( SroomNo == null ) {
            clientThread_list.send("exitroom]" + ScpMe + "]" + ScpOther + "]" + null + "]with");
        } else {
            clientThread_list.send("exitroom]" + ScpMe + "]" + ScpOther + "]" + SroomNo + "]with");
        }
    }

    // 뒤로가기 종료
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ChattingRoomActivity.class);
        startActivity(intent);
    }
}

/**
 * http://recipes4dev.tistory.com/46, 리스트뷰 xml 설명
 */