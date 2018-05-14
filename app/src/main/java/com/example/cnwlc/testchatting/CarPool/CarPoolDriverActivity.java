package com.example.cnwlc.testchatting.CarPool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cnwlc.testchatting.Login.LoginRegisterActivity;
import com.example.cnwlc.testchatting.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CarPoolDriverActivity extends Activity {
    private static final int PICK_FROM_CAMERA_V = 0;
    private static final int PICK_FROM_CAMERA_I = 1;
    private static final int PICK_FROM_GALLERY_V = 0;
    private static final int PICK_FROM_GALLERY_I = 1;
    String TAG = "CarPoolDriverActivity";
    String data, selectedImagePath_V, selectedImagePath_I;
    Bitmap photo;

    TextView car_numTv, car_modTv, car_relatTv, car_hostTv, license_numTv, license_kindTv;
    EditText car_numEtv, car_modEtv, car_relatEtv, car_hostEtv, license_numEtv, license_kindEtv;
    ImageButton licenseImgBtn, insuranceImgBtn;

    SharedPreferences pref;
    SharedPreferences.Editor edit;
    String Sid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carpool_driver);

        setDefine();
    }
    public void setDefine() {
        car_numTv = (TextView) findViewById(R.id.carNumTv);
        car_modTv = (TextView) findViewById(R.id.carModelTv);
        car_relatTv = (TextView) findViewById(R.id.carRelatTv);
        car_hostTv = (TextView) findViewById(R.id.carHostTv);
        license_numTv = (TextView) findViewById(R.id.licenseNumTv);
        license_kindTv = (TextView) findViewById(R.id.licenseKindTv);

        car_numEtv = (EditText) findViewById(R.id.carNumEtv);
        car_modEtv = (EditText) findViewById(R.id.carModelEtv);
        car_relatEtv = (EditText) findViewById(R.id.carRelatEtv);
        car_hostEtv = (EditText) findViewById(R.id.carHostEtv);
        license_numEtv = (EditText) findViewById(R.id.licenseNumEtv);
        license_kindEtv = (EditText) findViewById(R.id.licenseKindEtv);

        licenseImgBtn = (ImageButton) findViewById(R.id.vrcimgBtn);
        insuranceImgBtn = (ImageButton) findViewById(R.id.insuranceCardImgBtn);

        licenseImgBtn.setImageResource(R.drawable.plus);
        insuranceImgBtn.setImageResource(R.drawable.plus);

        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        Sid = pref.getString("id", "no id");
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveBtn :
                if(car_numEtv.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "차량번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    car_numEtv.requestFocus();
                    return;
                }
                if(car_modEtv.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "차량모델을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    car_modEtv.requestFocus();
                    return;
                }
                if(car_relatEtv.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "차량 소유관계를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    car_relatEtv.requestFocus();
                    return;
                }
                if(car_hostEtv.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "차량 소유자명을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    car_hostEtv.requestFocus();
                    return;
                }
                if( !isValidLicenseNumber(license_numEtv.getText().toString()) ) {
                    Toast.makeText(getApplicationContext(), "면허증 번호를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show();
                    license_numEtv.requestFocus();
                    return;
                }
                if(license_kindEtv.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "면허 종류를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    license_kindEtv.requestFocus();
                    return;
                }
                if(selectedImagePath_I == null) {
                    Toast.makeText(getApplicationContext(), "보험증 사진을 올려주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(selectedImagePath_V == null) {
                    Toast.makeText(getApplicationContext(), "운전면허증 사진을 올려주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(selectedImagePath_I != null && selectedImagePath_V != null) {
                    String car_num = car_numEtv.getText().toString();
                    String car_mod = car_modEtv.getText().toString();
                    String car_ral = car_relatEtv.getText().toString();
                    String car_host = car_hostEtv.getText().toString();
                    String license_num = license_numEtv.getText().toString();
                    String license_kin = license_kindEtv.getText().toString();

                    UploadFile uploadFile = new UploadFile(CarPoolDriverActivity.this);
                    uploadFile.setPath(selectedImagePath_V);
                    uploadFile.execute();

                    UploadFile uploadFile2 = new UploadFile(CarPoolDriverActivity.this);
                    uploadFile2.setPath(selectedImagePath_I);
                    uploadFile2.execute();

                    System.out.println(TAG+" selectedImagePath_I : "+selectedImagePath_I);
                    System.out.println(TAG+" selectedImagePath_V : "+selectedImagePath_V);

                    InsertData insertData = new InsertData();
                    insertData.execute(Sid, car_num, car_mod, car_ral, car_host, license_num, license_kin, selectedImagePath_V, selectedImagePath_I);
                }
                break;
            case R.id.cancelBtn :
                finish();
                break;

            /** 이미지 버튼 */
            case R.id.insuranceCardImgBtn :
                AlertDialog.Builder Alert_I = new AlertDialog.Builder(CarPoolDriverActivity.this);
                Alert_I.setTitle(" 사진등록 ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    // 안드로이드 카메라 가이드
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                                try {
                                    cameraIntent.putExtra("return-data", true);
                                    startActivityForResult(cameraIntent, PICK_FROM_CAMERA_I);
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
                                    startActivityForResult(Intent.createChooser(galleryIntent, "Complete action using"), PICK_FROM_GALLERY_I);
                                } catch (ActivityNotFoundException e) {
                                }
                            }
                        });
                AlertDialog AlD_I = Alert_I.create();
                AlD_I.setCanceledOnTouchOutside(true);
                AlD_I.show();
                break;
            case R.id.vrcimgBtn :
                AlertDialog.Builder Alert_V = new AlertDialog.Builder(CarPoolDriverActivity.this);
                Alert_V.setTitle(" 사진등록 ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("카메라", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    // 안드로이드 카메라 가이드
                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                                try {
                                    cameraIntent.putExtra("return-data", true);
                                    startActivityForResult(cameraIntent, PICK_FROM_CAMERA_V);
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
                                    startActivityForResult(Intent.createChooser(galleryIntent, "Complete action using"), PICK_FROM_GALLERY_V);
                                } catch (ActivityNotFoundException e) {
                                }
                            }
                        });
                AlertDialog AlD_V = Alert_V.create();
                AlD_V.setCanceledOnTouchOutside(true);
                AlD_V.show();
                break;
        }
    }

    // 운전면허증 자동 하이픈
    public static boolean isValidLicenseNumber(String licenseNum) {
        boolean returnValue = false;
        String regex = "^\\s*(\\d{2})(-|\\s)*(\\d{2})(-|\\s)*(\\d{6})(-|\\s)*(\\d{2})\\s*$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(licenseNum);
        if(m.matches()) returnValue = true;
        return returnValue;
    }

    // db에 데이터 넣기
    class InsertData extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String Sid = params[0];
            String car_num = params[1];
            String car_mod = params[2];
            String car_rel = params[3];
            String car_host = params[4];
            String license_num = params[5];
            String license_kin = params[6];
            String license_img = params[7];
            String insurance_img = params[8];

            String serverURL = "http://115.71.238.109/carpool_driver.php";
            String postParameters = "id=" + Sid + "&car_num=" + car_num + "&car_mod=" + car_mod + "&car_rel=" + car_rel + "&car_host=" + car_host
                    + "&license_num=" + license_num + "&license_kind=" + license_kin + "&license_img=" + license_img + "&insurance_img=" + insurance_img;

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

                AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolDriverActivity.this);
                builder.setTitle("알림");
                builder.setMessage("운전자로 등록이 완료되었습니다.");
                builder.setCancelable(true);
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            } else {
                Log.w("RESULT","에러 발생! ERRCODE = " + data);

                AlertDialog.Builder builder = new AlertDialog.Builder(CarPoolDriverActivity.this);
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

    // 카메라, 사진첩
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FROM_CAMERA_I) {
                if (data != null) {
                    photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null) {
                        insuranceImgBtn.setImageBitmap(photo);

                        Uri cameraUri = data.getData();
                        selectedImagePath_I = getPath(cameraUri);
                    }
                }
            } else if(requestCode == PICK_FROM_CAMERA_V) {
                if (data != null) {
                    photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null) {
                        licenseImgBtn.setImageBitmap(photo);

                        Uri cameraUri = data.getData();
                        selectedImagePath_V = getPath(cameraUri);
                    }
                }
            }
            if (requestCode == PICK_FROM_GALLERY_I) {
                Uri galleryUri = data.getData();
                selectedImagePath_I = getPath(galleryUri);

                photo = BitmapFactory.decodeFile(selectedImagePath_I);
                insuranceImgBtn.setImageBitmap(photo);
            } else if(requestCode == PICK_FROM_GALLERY_V) {
                Uri galleryUri = data.getData();
                selectedImagePath_V = getPath(galleryUri);

                photo = BitmapFactory.decodeFile(selectedImagePath_V);
                licenseImgBtn.setImageBitmap(photo);
            }
            System.out.println(TAG+" selectedImagePath_I : "+selectedImagePath_I);
            System.out.println(TAG+" selectedImagePath_V : "+selectedImagePath_V);
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
                                System.out.println("파일을 업로드했습니다.");
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
}
