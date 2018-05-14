package com.example.cnwlc.testchatting.ChattingRoom;

public class ChattingRoomListItem {
    private String Sno;
    private String Sname;
    private String Scp;
    private String Sdate;
    private String Scontent;
    private String Simagepath;
    private String Sre_msg;

    public ChattingRoomListItem() {

    }

    public ChattingRoomListItem(String Sno, String Sname, String Scp, String Sdate, String Scontent, String Simagepath, String Sre_msg) {
        this.Sno = Sno;
        this.Sname = Sname;
        this.Scp = Scp;
        this.Sdate = Sdate;
        this.Scontent = Scontent;
        this.Simagepath = Simagepath;
        this.Sre_msg = Sre_msg;
    }

    public String getSno() {
        return Sno;
    }
    public String getSname() {
        return Sname;
    }
    public String getScp() {
        return Scp;
    }
    public String getSdate() {
        return Sdate;
    }
    public String getScontent() {
        return Scontent;
    }
    public String getSimagepath() {
        return Simagepath;
    }
    public String getSre_msg() {
        return Sre_msg;
    }
}