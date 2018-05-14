package com.example.cnwlc.testchatting.Main;

import android.os.Parcel;
import android.os.Parcelable;

public class FriendsListItem implements Parcelable {
    private String Sname;
    private String Scellphone;
    private String Sno;
    private String Simagepath;

    public FriendsListItem() {
    }

    //Parcelable를 생성하기 위한 생성자 //임의 생성
    public FriendsListItem(String Sname, String Scellphone, String Sno, String Simagepath) {
        this.Sname = Sname;
        this.Scellphone = Scellphone;
        this.Sno = Sno;
        this.Simagepath = Simagepath;
    }

    //생성자와 순서가 같아야함 -> 틀릴 경우 다르게 데이터가 전달된다.

    //Parcelable를 생성하기 위한 생성자 Parcel를 파라메타로 넘겨 받음
    public FriendsListItem(Parcel in) {
        Sname = in.readString();
        Scellphone = in.readString();
        Sno = in.readString();
        Simagepath = in.readString();
    }

    //Parcelable의 write를 구현하기 위한 Method
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Sname);
        dest.writeString(Scellphone);
        dest.writeString(Sno);
        dest.writeString(Simagepath);
    }

    //Parcelable을 상속 받으면 필수 Method
    @Override
    public int describeContents() {
        return 0;
    }

    //Parcelable 객체로 구현하기 위한 Parcelable Method ArrayList구현 등..
    public static final Creator<FriendsListItem> CREATOR = new Creator<FriendsListItem>() {
        @Override
        public FriendsListItem createFromParcel(Parcel in) {
            return new FriendsListItem(in);
        }

        @Override
        public FriendsListItem[] newArray(int size) {
            return new FriendsListItem[size];
        }
    };

    public String getSname() {
        return Sname;
    }

    public String getScellphone() {
        return Scellphone;
    }

    public String getSno() {
        return Sno;
    }

    public String getSimagepath() {
        return Simagepath;
    }
}