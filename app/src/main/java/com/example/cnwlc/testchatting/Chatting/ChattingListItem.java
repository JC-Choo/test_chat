package com.example.cnwlc.testchatting.Chatting;

public class ChattingListItem {
    private String name;
    private String content;
    private String time;
    private String date;
    private String imgpath;
//    private String sendimgpath;
    int type;

    public ChattingListItem() {}

    //Parcelable를 생성하기 위한 생성자 //임의 생성
    public ChattingListItem(String name, String content, String time, String date, String _imgpath, int _type) {
        this.name = name;
        this.content = content;
        this.time = time;
        this.date = date;
        this.type = _type;
        this.imgpath = _imgpath;
//        this.sendimgpath = _sendimgpath;
    }

    public String getSname(){
        return name;
    }
    public String getScontent(){
        return content;
    }
    public String getStime(){
        return time;
    }
    public String getSdate(){
        return date;
    }
    public String getSimagepath(){
        return imgpath;
    }
//    public String getSsendimagepath(){
//        return sendimgpath;
//    }
}
