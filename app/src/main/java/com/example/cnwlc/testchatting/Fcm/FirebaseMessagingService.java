package com.example.cnwlc.testchatting.Fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.cnwlc.testchatting.ChattingRoom.ChattingRoomActivity;
import com.example.cnwlc.testchatting.R;
import com.google.firebase.messaging.RemoteMessage;

import java.util.StringTokenizer;

import static com.example.cnwlc.testchatting.Chatting.ChattingActivity._name;

/**
 * FCM 메세지를 받을 때 onMessageReceived 콜백이 호출됩니다. Service 이므로 AndroidManifest.xml 에 등록이 필요합니다.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    SharedPreferences pref;
    SharedPreferences.Editor edit;
    String Scp;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        pref = getSharedPreferences("login_information", MODE_PRIVATE);
        edit = pref.edit();
        Scp = pref.getString("Scp", "no cp");

        System.out.println(TAG + " Scp : " + Scp);
        System.out.println(TAG + " _name : " + _name);

        String msg = remoteMessage.getData().get("message");
        System.out.println(TAG + " msg : " + msg);

        StringTokenizer st = new StringTokenizer(msg, "|");
        String[] arr = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            arr[i] = st.nextToken();
        }
        // arr[0] : 방에 포함된 유저들의 전화번호
        // arr[1] : 알림창에 뜬 이름
        // arr[2] : 보낸 메시지
        // arr[3] : 방 이름
        // arr[4] : 들어온 멤버 명단
        // Scp : 앱을 킨 아이디 전화번호
        // _name : 자기가 메세지를 보냈다면 자신의 이름, 안보냈고 받았다면 null

        String sending_name = arr[1];
        String sending_msg = arr[2];
        String room_title = arr[3];
        String enter_cp = arr[4];
        String room_number = arr[5];

        StringTokenizer st_room = new StringTokenizer(room_title, ".");
        String[] room_arr = new String[st_room.countTokens()];
        for (int i = 0; st_room.hasMoreTokens(); i++) {
            room_arr[i] = st_room.nextToken();
        }
        StringTokenizer st_enter = new StringTokenizer(enter_cp, ".");
        String[] cp_arr = new String[st_enter.countTokens()];
        for (int i = 0; st_enter.hasMoreTokens(); i++) {
            cp_arr[i] = st_enter.nextToken();
        }

        for (int i = 0; i < room_arr.length; i++) {
            boolean TorF2 = false;
            for (int j = 0; j < cp_arr.length; j++) {
                System.out.println(TAG + " room_arr[" + i + "] : " + room_arr[i]);
                System.out.println(TAG + " cp_arr[" + j + "] : " + cp_arr[j]);
                System.out.println(TAG + " TorF 1 : " + TorF2);
                System.out.println(TAG + " room_arr[i].equals(cp_arr[j]) : " + room_arr[i].equals(cp_arr[j]));
                TorF2 = TorF2 || room_arr[i].equals(cp_arr[j]);
                System.out.println(TAG + " TorF 2 : " + TorF2);
            }
            System.out.println(TAG + " TorF 3 : " + TorF2);
            System.out.println(TAG + " Scp.equals(arr[0]) : " + Scp.equals(arr[0]));
            System.out.println(TAG + " !sending_name.equals(_name) : " + !sending_name.equals(_name));
            System.out.println(TAG + " (TorF2 == false) : " + (TorF2 == false));
            if (Scp.equals(arr[0]) && !sending_name.equals(_name)) {
                if (TorF2 == false) {
                    System.out.println("success");
                    sendPushNotification(room_number+"|"+sending_name + " : " + sending_msg);
                }
            }
        }
    }

    // messageBody 를 핸드폰 상단 화면에 띄움
    public void sendPushNotification(String message) {
        System.out.println("received message : " + message);

        StringTokenizer st = new StringTokenizer(message, "|");
        String[] arr = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            arr[i] = st.nextToken();
            System.out.println("received message arr["+i+"] : "+arr[i]);
        }
        message = arr[1];
        System.out.println("received message : " + message);

        Intent intent = new Intent(this, ChattingRoomActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,0);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("채팅&카풀")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setLights(000000255, 500, 2000)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1, 1000});

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        wakelock.acquire(5000);

        notificationManager.notify(Integer.parseInt(arr[0]), notificationBuilder.build());
    }
}
