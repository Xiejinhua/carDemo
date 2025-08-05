package com.autosdk.bussiness.account.observer;

import com.autonavi.gbl.user.group.model.GroupResponseCreate;
import com.autonavi.gbl.user.group.model.GroupResponseDissolve;
import com.autonavi.gbl.user.group.model.GroupResponseFriendList;
import com.autonavi.gbl.user.group.model.GroupResponseInfo;
import com.autonavi.gbl.user.group.model.GroupResponseInvite;
import com.autonavi.gbl.user.group.model.GroupResponseInviteQRUrl;
import com.autonavi.gbl.user.group.model.GroupResponseJoin;
import com.autonavi.gbl.user.group.model.GroupResponseKick;
import com.autonavi.gbl.user.group.model.GroupResponseQuit;
import com.autonavi.gbl.user.group.model.GroupResponseSetNickName;
import com.autonavi.gbl.user.group.model.GroupResponseStatus;
import com.autonavi.gbl.user.group.model.GroupResponseUpdate;
import com.autonavi.gbl.user.group.model.GroupResponseUrlTranslate;
import com.autonavi.gbl.user.group.observer.IGroupServiceObserver;

/**
 * Created by AutoSdk on 2020/10/20.
 **/
public class GroupServiceObserver implements IGroupServiceObserver {
    private int businessType;//业务类型，比如组队消息弹条

    public int getBusinessType() {
        return businessType;
    }

    public void setBusinessType(int businessType) {
        this.businessType = businessType;
    }

    @Override
    public void onNotify(int i, long l, GroupResponseStatus groupResponseStatus) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseCreate groupResponseCreate) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseDissolve groupResponseDissolve) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseJoin groupResponseJoin) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseQuit groupResponseQuit) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseInvite groupResponseInvite) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseKick groupResponseKick) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseInfo groupResponseInfo) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseUpdate groupResponseUpdate) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseSetNickName groupResponseSetNickName) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseFriendList groupResponseFriendList) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseInviteQRUrl groupResponseInviteQRUrl) {

    }

    @Override
    public void onNotify(int i, long l, GroupResponseUrlTranslate groupResponseUrlTranslate) {

    }
}
