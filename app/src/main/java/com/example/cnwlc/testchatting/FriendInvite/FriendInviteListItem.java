package com.example.cnwlc.testchatting.FriendInvite;

public class FriendInviteListItem {
    private String Sname;
    private String Scp;
    private String Simagepath;

    public FriendInviteListItem() {

    }

    public FriendInviteListItem(String Sname, String Scp, String Simagepath) {
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
