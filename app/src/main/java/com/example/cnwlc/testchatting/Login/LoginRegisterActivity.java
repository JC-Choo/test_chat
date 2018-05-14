package com.example.cnwlc.testchatting.Login;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.cnwlc.testchatting.R;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginRegisterActivity extends Activity {
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_GALLERY = 1;
    private static final int PICK_FROM_DRAW = 2;
    private static String TAG = "chatting_LoginRegisterActivity";

    private String selectedImagePath = null;

    Animation animSlide_Down;
    ImageView imgview;
    Bitmap photo;
    EditText editname, editid, editpw, editpwconfirm, editcellphone, editcertification;
    String strNumber, strCerNum, data;

    boolean possibleId = false;
    boolean possibleCertification = false;
    boolean possibleCellphone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        Define();
        TextLineColor();
    }

    private void Define() {
        imgview = (ImageView) findViewById(R.id.pictureImgView);

        animSlide_Down = AnimationUtils.loadAnimation(this, R.anim.anim_slide_down);

        editname = (EditText) findViewById(R.id.nameEdit);
        editid = (EditText) findViewById(R.id.idEdit);
        editpw = (EditText) findViewById(R.id.pwEdit);
        editpwconfirm = (EditText) findViewById(R.id.pwEdit2);
        editcellphone = (EditText) findViewById(R.id.cellphoneEdit);
        editcertification = (EditText) findViewById(R.id.certificationEdit);

        int colorOnCreate = Color.parseColor("#000000");
        editname.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editid.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editpw.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editpwconfirm.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editcellphone.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editcertification.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
    }

    // 회원 가입 시 맞지 않으면 빨간줄 나타내기
    private void TextLineColor() {
        editname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = editname.getText().toString();

                if ( name.length() >= 2 ) {
                    int color = Color.parseColor("#000000");
                    editname.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                } else {
                    int color = Color.parseColor("#ff0000");
                    editname.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        editid.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 유효성 검사 통과 시 초록식줄
                if ( android.util.Patterns.EMAIL_ADDRESS.matcher(editid.getText().toString()).matches() ) {
                    int color = Color.parseColor("#000000");
                    editid.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                } else {
                    int color = Color.parseColor("#ff0000");
                    editid.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        editpwconfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = editpw.getText().toString();
                String confirm = editpwconfirm.getText().toString();

                if ( (password.equals(confirm)) && checkPassword(password) ) {
                    int color = Color.parseColor("#000000");
                    editpw.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    editpwconfirm.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                } else {
                    int color = Color.parseColor("#ff0000");
                    editpw.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    editpwconfirm.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        editcellphone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String cellphone = editcellphone.getText().toString();

                if ( cellphone.length() >= 11 ) {
                    int color = Color.parseColor("#000000");
                    editcellphone.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                } else {
                    int color = Color.parseColor("#ff0000");
                    editcellphone.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        editcertification.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String certification = editcertification.getText().toString();

                if ( certification.equals(strCerNum) ) {
                    int color = Color.parseColor("#000000");
                    editcertification.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                } else {
                    int color = Color.parseColor("#ff0000");
                    editcertification.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    // 유효성 검사 및 스페이스(공간) 검사
    public boolean checkPassword(String password) {
        boolean isOk;

        Pattern pattern = Pattern.compile("([a-zA-Z0-9].*[!,@,#,$,%,^,&,*,?,~])|([!,@,#,$,%,^,&,*,?,~].*[a-zA-Z0-9])");
        Matcher matcher = pattern.matcher(password);

        if (!matcher.find()) {
            isOk = false;
        } else if (!spaceCheck(password)) {
            isOk = false;
        } else if (password.length() < 8 || password.length() > 15) {
            isOk = false;
        } else {
            isOk = true;
        }

        return isOk;
    }
    public boolean spaceCheck(String string) {
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == ' ') {
                return false;
            }
        }
        return true;
    }

    // 버튼 클릭
    public void onClick(View view) {
        switch (view.getId()) {
            // 카메라 버튼
            case R.id.pictureBtn :
                AlertDialog.Builder AlD_B = new AlertDialog.Builder(LoginRegisterActivity.this);
                AlD_B.setTitle(" 사진등록 ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    // 안드로이드 카메라 가이드
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                                try {
                                    cameraIntent.putExtra("return-data", true);
                                    startActivityForResult(cameraIntent, PICK_FROM_CAMERA);
                                } catch (ActivityNotFoundException e) {
                                    // Do nothing for now
                                }
                            }
                        })
                        .setNegativeButton("갤러리", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent galleryIntent = new Intent();
                                // Gallery 호출
                                galleryIntent.setType("image/*");
                                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                                try {
                                    galleryIntent.putExtra("return-data", true);
                                    startActivityForResult(Intent.createChooser(galleryIntent, "Complete action using"), PICK_FROM_GALLERY);
                                } catch (ActivityNotFoundException e) {
                                }
                            }
                        });
                AlertDialog AlD = AlD_B.create();
                AlD.setCanceledOnTouchOutside(true);
                AlD.show();
                break;
            // 아이디 체크 버튼
            case R.id.idcheckBtn:
                if (editid.getText().toString().length() == 0) {
                    Toast.makeText(LoginRegisterActivity.this, "id를 입력하세요!", Toast.LENGTH_SHORT).show();
                    editid.requestFocus();
                    return;
                }
                // 이메일 유효성 검사
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(editid.getText().toString()).matches()) {
                    Toast.makeText(getApplicationContext(), "이메일 형식이 아닙니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                idDBcheck check = new idDBcheck();
                check.execute(editid.getText().toString());
                break;
            // 완료 버튼
            case R.id.okBtn:
                if (editname.getText().toString().length() < 2) {
                    Toast.makeText(LoginRegisterActivity.this, "이름을 입력하세요!", Toast.LENGTH_SHORT).show();
                    editname.requestFocus();
                    return;
                }
                if (editid.getText().toString().length() == 0) {
                    Toast.makeText(LoginRegisterActivity.this, "id를 입력하세요!", Toast.LENGTH_SHORT).show();
                    editid.requestFocus();
                    return;
                }
                if (editpw.getText().toString().length() < 4) {
                    Toast.makeText(LoginRegisterActivity.this, "비밀번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    editpw.requestFocus();
                    return;
                }
                if (editpwconfirm.getText().toString().length() < 4) {
                    Toast.makeText(LoginRegisterActivity.this, "비밀번호 확인을 입력하세요!", Toast.LENGTH_SHORT).show();
                    editpwconfirm.requestFocus();
                    return;
                }
                if (editcellphone.getText().toString().length() < 10) {
                    Toast.makeText(LoginRegisterActivity.this, "전화번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    editcellphone.requestFocus();
                    return;
                }
                if (editcertification.getText().toString().length() == 0) {
                    Toast.makeText(LoginRegisterActivity.this, "인증번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    editcertification.requestFocus();
                    return;
                }

                if ( !editpw.getText().toString().equals(editpwconfirm.getText().toString()) ) {
                    Toast.makeText(LoginRegisterActivity.this, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show();
                    editpw.setText("");
                    editpwconfirm.setText("");
                    editpw.requestFocus();
                    return;
                }
                // 중복 체크 안할 경우
                if (possibleId != true) {
                    Toast.makeText(LoginRegisterActivity.this, "아이디 중복 체크를 진행하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 인증번호를 안 받을 경우
                if (possibleCellphone != true) {
                    Toast.makeText(LoginRegisterActivity.this, "인증번호를 받으세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 인증번호 체크 안할 경우
                if (possibleCertification != true) {
                    Toast.makeText(LoginRegisterActivity.this, "인증번호 체크를 진행하세요!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // 중복 체크 및 인증번호 성공
                if (possibleId == true && possibleCertification == true) {
                    String sname = editname.getText().toString();
                    String sid = editid.getText().toString();
                    String spw = editpw.getText().toString();
                    String scp = editcellphone.getText().toString();

                    if( selectedImagePath == null ) {
                        String imagepath = "http://115.71.238.109/upload/basic.jpg";
                        InsertData task = new InsertData();
                        task.execute(sname, sid, spw, scp, imagepath);
                    } else {
                        String[] imgpath = selectedImagePath.split("/");
                        String imagepath = "http://115.71.238.109/upload/"+imgpath[6];

                        InsertData task = new InsertData();
                        task.execute(sname, sid, spw, scp, imagepath);

                        UploadFile uploadFile = new UploadFile(LoginRegisterActivity.this);
                        uploadFile.setPath(selectedImagePath);
                        uploadFile.execute();
                    }
                }
                break;
            // 취소 버튼
            case R.id.noBtn:
                finish();
                // finish()를 쓸 경우 onDestroy 를 출력하게 된다.
                break;
            // 핸드폰 번호 인증 버튼
            case R.id.cellphoneBtn:
                strNumber = editcellphone.getText().toString();
                if(strNumber.equals("")) {
                    Toast.makeText(getApplicationContext(), "전화번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    int certificationNumber = (int) (Math.random() * 10000);
                    strCerNum = String.valueOf(certificationNumber);
                    Toast.makeText(getApplicationContext(), "인증 번호 : "+strCerNum, Toast.LENGTH_LONG).show();
                    possibleCellphone = true;
//                    sendSMS(strNumber, strCerNum);
                }
                break;
            // 핸드폰 인증 버튼
            case R.id.certificationBtn:
                if (editcertification.getText().toString().length() == 0) {
                    Toast.makeText(LoginRegisterActivity.this, "인증번호를 입력하세요!", Toast.LENGTH_SHORT).show();
                    editcertification.requestFocus();
                    return;
                }
                if (editcertification.getText().toString().equals(strCerNum)) {
                    Toast.makeText(getApplicationContext(), "인증에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                    possibleCertification = true;
                } else {
                    Toast.makeText(getApplicationContext(), "인증에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    possibleCertification = false;
                }
                break;
        }
    }

    // db에 데이터 넣기
    class InsertData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                    System.out.println("HTTP_OK 값 : "+HttpURLConnection.HTTP_OK);
                    Log.d(TAG, "inputStream : "+inputStream);
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                    Log.d(TAG, "inputStream : "+inputStream);
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
            if(data.equals("0")) {
                Log.w("RESULT","성공적으로 처리되었습니다!");

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginRegisterActivity.this);
                builder.setTitle("알림");
                builder.setMessage("회원가입이 완료되었습니다.");
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                                finish();
                    }
                });
                builder.show();
            } else {
                Log.w("RESULT","에러 발생! ERRCODE = " + data);

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginRegisterActivity.this);
                builder.setTitle("알림");
                builder.setMessage("등록중 에러가 발생했습니다! errcode : "+ data);
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            }
        }
    }

    // id / pw -> db에서 체크
    private class idDBcheck extends AsyncTask<String, Void, String> {
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

                while ( (line = bufferedReader.readLine()) != null ) {
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

            if(data.equals("1")) {
                Log.w("RESULT", "사용 가능한 아이디");
                Toast.makeText(getApplicationContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                possibleId = true;
            } else if(data.equals("0")) {
                Log.w("RESULT", "이미 사용중인 아이디");
                Toast.makeText(getApplicationContext(), "이미 사용중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                possibleId = false;
            } else {
                Log.e("RESULT","에러 발생! ERRCODE = " + data);

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginRegisterActivity.this);
                builder.setTitle("알림");
                builder.setMessage("등록중 에러가 발생했습니다! errcode : "+ data);
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

    // 카메라, 사진첩, 그림그리기
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA) {
                if (data != null) {
                    photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null) {
                        imgview.setImageBitmap(photo);
                        imgview.startAnimation(animSlide_Down);

                        Uri cameraUri = data.getData();
                        selectedImagePath = getPath(cameraUri);
                    }
                }
            }
            if (requestCode == PICK_FROM_GALLERY) {
                Uri galleryUri = data.getData();
                selectedImagePath = getPath(galleryUri);

                photo = BitmapFactory.decodeFile(selectedImagePath);
                imgview.setImageBitmap(photo);
                imgview.startAnimation(animSlide_Down);
            }
            if (requestCode == PICK_FROM_DRAW) {
                selectedImagePath = data.getStringExtra("saveUri");

                photo = BitmapFactory.decodeFile(selectedImagePath);
                imgview.setImageBitmap(photo);
                imgview.startAnimation(animSlide_Down);
            }
            Log.e("selectedImagePath : ",selectedImagePath);
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

    // 사진 서버로 업로드
    public class UploadFile extends AsyncTask<String, String, String> {
        Context context; // 생성자 호출
        String fileName; // 파일 위치

        HttpURLConnection conn = null; // 네트워크 연결 객체
        DataOutputStream dos = null; // 서버 전송 시 데이터 작성한 뒤 전송

        String lineEnd = "\r\n"; // 구분자
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024;
        File sourceFile;
        int serverResponseCode;
        String TAG = "FileUpload";

        // UploadFile 생성자를 통해 업로드를 시킬 액티비티의 Context 객체를 전달해 초기화
        public UploadFile(Context context) {
            this.context = context;
        }
        // setPath 란 함수를 생성, 업로드할 파일의 경로를 전달
        public void setPath(String uploadFilePath) {
            this.fileName = uploadFilePath;
            this.sourceFile = new File(uploadFilePath);
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if(!sourceFile.isFile()) {
                Log.e(TAG, "sourceFile("+fileName+") is Not A File");
                return null;
            } else {
                String success = "Success";
                Log.i(TAG, "sourceFile("+fileName+") is A File");
                try{
                    FileInputStream fileInputStream = new FileInputStream(sourceFile);
                    URL url = new URL("http://115.71.238.109/upload.php");
                    Log.i("sourceFile : ", "sourceFile is "+sourceFile);
                    Log.i("fileInputStream : ", "fileInputStream is "+fileInputStream);

                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST"); // 전송 방식
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); // boundary 기준으로 인자를 구분함
                    conn.setRequestProperty("uploaded_file", fileName);
                    Log.i(TAG, "fileName : "+fileName);

                    // dataoutput은 outputstream이란 클래스를 가져오며, outputstream은 fileoutputstream의 하위 클래스
                    // output은 쓰기, input은 읽기, 데이터를 전송할 내용을 적는 것으로 이해할 것
                    dos = new DataOutputStream(conn.getOutputStream());

                    // ㅏㅅ용자 이름으로 폴더를 생성하기 위해 사용자 이름을 서버로 전송한다. 하나의 인자 전달 data1 = newImage
//                    dos.writeBytes(twoHyphens + boundary + lineEnd);
//                    dos.writeBytes("Content-Disposition : form-data; name=\"data1\"" + lineEnd); // name은 \ \ 안 인자가 php의 key
//                    dos.writeBytes(lineEnd);
//                    dos.writeBytes("newImage");
//                    dos.writeBytes(lineEnd);

                    // 이미지 전송, 데이터 전달 uploadded_file 라는 php key 값에 저장되는 내용은 fileName
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    // create a buffer of maximum size
                    bytesAvailable = fileInputStream.available();

                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    // read file and write it into form...
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                    Log.e("Test", "image byte is " + bytesRead);

                    while(bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    // send miltipart form data necessary after file data..., 마지막에 two~~ lineEnd로 마무리(인제 나열이 끝났음을 알림)
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
                    serverResponseCode = conn.getResponseCode();
                    String serverResponseMessage = conn.getResponseMessage();

                    Log.i(TAG, "[UploadImageToserver] Http Response is : "+serverResponseMessage + ": " + serverResponseCode);

                    if(serverResponseCode == 200) {
                        runOnUiThread(new Runnable() {
                            public void run() {
//                                Toast.makeText(getApplicationContext(), " 파일을 업로드했습니다", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    //결과 확인
                    BufferedReader rd = null;

                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    String line = null;
                    while( (line = rd.readLine()) != null) {
                        Log.i("Upload State", line);
                    }

                    // close the streams
                    fileInputStream.close();
                    dos.flush();

                    // get response
                    int ch;
                    InputStream is = conn.getInputStream();
                    StringBuffer b =new StringBuffer();
                    while( ( ch = is.read() ) != -1 ){
                        b.append( (char)ch );
                    }
                    String s=b.toString();
                    Log.e("Test", "result = " + s);

                    dos.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG + "Error", e.toString());
                }
                return success;
            }
        }
    }

    // sms 보내기 -> 돈듬...
    private void sendSMS(String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
        String strMessage = "인증번호 : " + message;

        // 각각 위에서부터 문자 전송, 문자 수신에 관련하여 sendTextMessage()에 넘겨줄 값들입니다
        PendingIntent senTPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "SMS 전송", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getApplicationContext(), "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getApplicationContext(), "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getApplicationContext(), "Null PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getApplicationContext(), "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(getApplicationContext(), "SMS 전송 완료", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getApplicationContext(), "SMS 전송 실패", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, strMessage, senTPI, deliveredPI);
    }

    @Override
    protected void onStop() {
        super.onStop();

        int colorOnCreate = Color.parseColor("#000000");
        editname.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editid.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editpw.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editpwconfirm.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editcellphone.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
        editcertification.getBackground().setColorFilter(colorOnCreate, PorterDuff.Mode.SRC_IN);
    }
}