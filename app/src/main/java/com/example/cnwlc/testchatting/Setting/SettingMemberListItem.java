package com.example.cnwlc.testchatting.Setting;

public class SettingMemberListItem {
    private String Sname;
    private String Simagepath;

    public SettingMemberListItem() {}

    public SettingMemberListItem(String Sname, String Simagepath) {
        this.Sname   = Sname;
        this.Simagepath   = Simagepath;
    }

    public String getSname(){
        return Sname;
    }
    public String getSimagepath() {
        return Simagepath;
    }
}