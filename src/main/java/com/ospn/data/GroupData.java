package com.ospn.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupData extends CryptData{
    public String name;
    public String owner;
    public String portrait;
    public String aesKey;       // added by CESHI
    public int type; //Ⱥ������
    public int joinType; //��������
    public int passType; //��������
    public int mute; //ȫԱ����
    public int maxMember;
    public long createTime;
    public Set<String> userList = new HashSet<>();
    public Set<MemberData> members = new HashSet<>(); // add by CESHI
    //public Set<String> userSet = new HashSet<>();
    public Set<String> muteSet = new HashSet<>();

    public static int GroupType_Normal = 0;   //����Ⱥ����Ա���޸�Ⱥ�����֡�ͷ��
    public static int GroupType_Free = 1;     //����Ⱥ����Ա���޸�Ⱥ����
    public static int GroupType_Restrict = 2; //����Ⱥ����Ա�޷��޸�Ⱥ����

    public static int GroupJoinType_Free = 0;
    public static int GroupJoinType_Member = 1;
    public static int GroupJoinType_Admin = 2;
    public static int GroupJoinType_Owner = 3;

    public static int GroupPassType_Free = 0;
    public static int GropuPassType_Owner = 1;
    public static int GropuPassType_Admin = 2;


    public JSONObject toJson(){
        JSONObject json = new JSONObject();
        json.put("groupID", osnID);
        json.put("name", name);
        json.put("privateKey", "");
        json.put("owner", owner);
        json.put("type", type);
        json.put("mute", mute);
        json.put("joinType", joinType);
        json.put("passType", passType);
        json.put("portrait", portrait);
        json.put("memberCount", userList.size());
        JSONArray array = new JSONArray();
        //array.addAll(userList);
        json.put("userList", array);
        return json;
    }
    public void delMembers(List<String> members){
        members.forEach(userList::remove);
    }
    public void addMember(String userID){
        userList.add(userID);
    }
    public void delMember(String userID){
        userList.remove(userID);
    }
    public boolean hasMember(String osnID){
        return userList.contains(osnID);
    }
    public void addMute(String osnID){
        muteSet.add(osnID);
    }
    public void update(GroupData groupData, List<String> keys){
        for(String k : keys){
            if(k.equalsIgnoreCase("name"))
                name = groupData.name;
            else if(k.equalsIgnoreCase("portrait"))
                portrait = groupData.portrait;
            else if(k.equalsIgnoreCase("type"))
                type = groupData.type;
            else if(k.equalsIgnoreCase("joinType"))
                joinType = groupData.joinType;
            else if(k.equalsIgnoreCase("passType"))
                passType = groupData.passType;
            else if(k.equalsIgnoreCase("mute"))
                mute = groupData.mute;
        }
    }
    public void validType(){
        if(type > GroupType_Restrict)
            type = GroupType_Normal;
        if(joinType > GroupJoinType_Owner)
            joinType = GroupJoinType_Free;
        if(passType > GropuPassType_Admin)
            passType = GroupPassType_Free;
    }
    public boolean checkJoinRight(MemberData memberData){
        if(joinType == GropuPassType_Admin)
            return memberData.isAdmin();
        else if(joinType == GroupJoinType_Owner)
            return memberData.isOwner();
        return true;
    }
}
