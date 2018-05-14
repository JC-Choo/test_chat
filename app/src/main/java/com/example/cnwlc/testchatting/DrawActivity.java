package com.example.cnwlc.testchatting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.cnwlc.testchatting.Login.LoginRegisterActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;

import static android.R.attr.name;

public class DrawActivity extends Activity implements View.OnClickListener {

    public static final boolean D = true; // 디버그 모드

    private signView sV; // 그려질 signView뷰영역
    private Button btn_save; // 저장 버튼
    private Button btn_cancle; // 취소 버튼

    Bitmap bit;
    String save;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        sV = (signView) findViewById(R.id.signView);
        btn_save = (Button) findViewById(R.id.btn_save);
        btn_cancle = (Button) findViewById(R.id.btn_cancle);

        // Button -> btn_save -> Activity -> implements OnClickListener -> this -> view
        btn_save.setOnClickListener(this);
        btn_cancle.setOnClickListener(this);
    }

    // OnClickListener-> this -> view
    public void onClick(View v) {
        // 모든 클릭이벤트 처리
        switch (v.getId()) {
            case R.id.btn_save: // 저장버튼
                AlertDialog.Builder ab = new AlertDialog.Builder(this);

                ab.setTitle("저장");
                ab.setMessage("저장하시겠습니까?");
                ab.setIcon(android.R.drawable.ic_dialog_alert);
                ab.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (sV != null) {
                            saveView(sV);
                        }
                        // handler 사용
                        mHandler.sendEmptyMessageDelayed(0, 10);

                        Intent Di = new Intent(DrawActivity.this, LoginRegisterActivity.class);
                        Di.putExtra("saveUri", save);
                        setResult(RESULT_OK, Di);
                        Toast.makeText(getApplicationContext(), "save 핸들러쪽 경로 : " + save, Toast.LENGTH_LONG).show();

                        finish();
                    }
                });
                ab.setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dia = ab.create();
                dia.show();
                break;
            case R.id.btn_cancle:
                finish();
                break;
        }
    }

    // Handler 사용
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                doUpload();
            }
        }
    };

    void doUpload() {
        // 약 2초의 지연시간뒤에 (20 * 100ms) 업로드 완료 toast 출력
        for (int i = 0; i < 20; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        Toast.makeText(this, "저장을 완료했습니다.", Toast.LENGTH_SHORT).show();
    }

    // 저장 루틴
    private void saveView(View view) {
        bit = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bit);
        view.draw(c);
        FileOutputStream fos = null;

        // 저장 경로 생성
        String folder = "/DCIM/Camera";
        try {
            File sdCardPath = Environment.getExternalStorageDirectory();
            File dirs = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), folder);
            if (!dirs.exists()) { // 원하는 경로에 폴더가 있는지 확인
                dirs.mkdirs(); // Test 폴더 생성
            }

            // 날짜 포맷
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss");
            Date date = new Date(System.currentTimeMillis());

            // 파일 이름 형식 지정
            String name = dateFormat.format(date) + ".jpg";

            // 저장위치는 /mnt/sdcard/Android/data/com.cnwlc.secondsubject/file
            save = sdCardPath.getPath() + "/" + folder + "/" + name;
            try {
                fos = new FileOutputStream(save);
                if (fos != null) {
                    bit.compress(Bitmap.CompressFormat.JPEG, 85, fos);// 압축률85%
                    fos.close();
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
            Log.e("Screen", "" + e.toString());
        }
        Toast.makeText(this, "저장완료:" + name, Toast.LENGTH_SHORT).show(); // 0: LENGTH_SHORT
    }
}