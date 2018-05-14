package com.example.cnwlc.testchatting.Chatting;

public class ChattingFriendListItem {
    private String Sname;
    private String Scp;
    private String Simagepath;

    public ChattingFriendListItem() {}

    public ChattingFriendListItem(String Sname, String Scp, String Simagepath) {
        this.Sname   = Sname;
        this.Scp   = Scp;
        this.Simagepath   = Simagepath;
    }

    public String getSname(){
        return Sname;
    }
    public String getScp(){
        return Scp;
    }
    public String getSimagepath() {
        return Simagepath;
    }
}