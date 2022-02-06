package com.ospn.core;

import com.ospn.data.*;

import java.util.List;

public interface IMDb {
    UserData readUserByID(String user);
    UserData readUserByName(String user);
    boolean deleteUser(String osnID);
    boolean updateUser(UserData userData, List<String> keys);
    GroupData readGroup(String groupID);
    MemberData readMember(String groupID, String userID);
    List<String> listGroup(String userID, boolean isAll);
    List<MemberData> listMember(String groupID);
    List<FriendData> listFriend(String userID);
    LitappData readLitapp(String osnID);
}
