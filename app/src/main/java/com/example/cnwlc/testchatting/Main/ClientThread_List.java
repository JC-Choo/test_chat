package com.example.cnwlc.testchatting.Main;

import android.os.Bundle;
import android.os.Message;

import com.example.cnwlc.testchatting.Chatting.ChattingActivity;
import com.example.cnwlc.testchatting.ChattingRoom.ChattingRoomActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;

/*
 * 메인쓰레드는 절대로 무한루프나 대기상태에 빠지거나 네트워크 작업을 해서는 안됨.
 * 또한 전송 버튼을 누르지 않더라도 서버측에서 보내는 메세지를 실시간으로 청취하려면 어느 누군가가 무한루프를 돌면서, 계속 입력을 감시해야함.
 * 그럼 어떻게 해야하느냐? 개발자 정의한 쓰레드가 해답임.
 */

public class ClientThread_List extends Thread {
    String TAG = "ClientThread_List";
    FriendsListActivity friendsListActivity;
    Socket client;

    BufferedReader buffread; /*들고*/
    BufferedWriter buffwrite; /*말함*/

    boolean stop= false;

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public ClientThread_List(FriendsListActivity activity, Socket client) {
        this.friendsListActivity = activity;
        this.client = client;
        try {
            buffread = new BufferedReader(new InputStreamReader(client.getInputStream(), "utf-8"));
            buffwrite = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*메세지 보냄. 현재 실행중인 프로그램에서 데이터가 나감 출력*/
    public void send(String msg) {
        try {
            buffwrite.write(msg + "\n"); /*반드시 반드시 줄바꿈 표시가 있어야함. 왜냐? 버퍼스트림의 문장의 끝임을 이해함*/
            buffwrite.flush(); /*퍼버처리된 출력문장의 버퍼를 싹 비워줘야함 비워줌.*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*실시간청취*/
    public void listen() {
        String msg = null;

        try {
            msg = buffread.readLine(); /*서버가 보낸 메세지 한줄을 읽어들이고*/
            System.out.println(TAG+" msg 1 : "+msg);

            StringTokenizer st = new StringTokenizer(msg, "]");
            String[] arr = new String[st.countTokens()];
            for (int i = 0; st.hasMoreTokens(); i++) {
                arr[i] = st.nextToken();
            }

            Message message = new Message();
            Bundle bundle = new Bundle();
            if(msg.startsWith("say]")) {
                msg = ""+arr[3];
                System.out.println(TAG+" msg.startsWith(say]) : "+msg);

                bundle.putString("name", arr[5]); //대화내용 넣어줌 풋
                bundle.putString("img", arr[6]); //대화내용 넣어줌 풋
                bundle.putString("msg", msg); //대화내용 넣어줌 풋
                message.setData(bundle);

                ChattingActivity.handler_ChattingActivity.sendMessage(message);
            } else if(msg.startsWith("enter]")){
                msg = ""+arr[1];
                System.out.println(TAG+" msg.startsWith(enter]) : "+msg);

                bundle.putString("name", null); //대화내용 넣어줌 풋
                bundle.putString("img", null); //대화내용 넣어줌 풋
                bundle.putString("msg", msg); //대화내용 넣어줌 풋
                message.setData(bundle);

                ChattingActivity.handler_ChattingActivity.sendMessage(message);
            } else if(msg.startsWith("re_msg]")){
                System.out.println(TAG+" msg.startsWith(re_msg]) : "+msg);
                String name = arr[1];
                msg = arr[2];
                String re_msg_count = arr[3];
                String room_title = arr[4];
                String enter_member = arr[5];
                String roomNumber = arr[6];

                System.out.println(TAG+" msg.startsWith(re_msg]) name : "+name);
                System.out.println(TAG+" msg.startsWith(re_msg]) msg : "+msg);
                System.out.println(TAG+" msg.startsWith(re_msg]) re_msg_count : "+re_msg_count);
                System.out.println(TAG+" msg.startsWith(re_msg]) room_title : "+room_title);
                System.out.println(TAG+" msg.startsWith(re_msg]) enter_member : "+enter_member);
                System.out.println(TAG+" msg.startsWith(re_msg]) roomNumber : "+roomNumber);

                bundle.putString("name", name);
                bundle.putString("msg", msg);
                bundle.putString("re_msg_count", re_msg_count);
                bundle.putString("room_title", room_title);
                bundle.putString("enter_member", enter_member);
                bundle.putString("roomNumber", roomNumber);
                message.setData(bundle);

                ChattingRoomActivity.handler_ChattingRoomActivity.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!stop) {
            listen();
        }
    }
}