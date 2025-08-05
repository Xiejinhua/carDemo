package com.autosdk.bussiness.account.observer;

import com.autonavi.gbl.user.account.model.AccountCheckResult;
import com.autonavi.gbl.user.account.model.AccountLogoutResult;
import com.autonavi.gbl.user.account.model.AccountProfileResult;
import com.autonavi.gbl.user.account.model.AccountRegisterResult;
import com.autonavi.gbl.user.account.model.AccountUnRegisterResult;
import com.autonavi.gbl.user.account.model.AvatarResult;
import com.autonavi.gbl.user.account.model.BindMobileResult;
import com.autonavi.gbl.user.account.model.CarltdAuthInfoResult;
import com.autonavi.gbl.user.account.model.CarltdBindResult;
import com.autonavi.gbl.user.account.model.CarltdCheckBindResult;
import com.autonavi.gbl.user.account.model.CarltdCheckTokenResult;
import com.autonavi.gbl.user.account.model.CarltdLoginResult;
import com.autonavi.gbl.user.account.model.CarltdQLoginResult;
import com.autonavi.gbl.user.account.model.CarltdUnBindResult;
import com.autonavi.gbl.user.account.model.MobileLoginResult;
import com.autonavi.gbl.user.account.model.QRCodeLoginConfirmResult;
import com.autonavi.gbl.user.account.model.QRCodeLoginResult;
import com.autonavi.gbl.user.account.model.UnBindMobileResult;
import com.autonavi.gbl.user.account.model.VerificationCodeResult;
import com.autonavi.gbl.user.account.observer.IAccountServiceObserver;

/**
 * Created by AutoSdk on 2020/10/20.
 * @author AutoSDk
 **/
public class AccountServiceObserver implements IAccountServiceObserver {
    @Override
    public void notify(int errCode, int taskId, AccountCheckResult accountCheckResult) {

    }

    @Override
    public void notify(int errCode, int taskId, BindMobileResult bindMobileResult) {

    }

    @Override
    public void notify(int errCode, int taskId, UnBindMobileResult unBindMobileResult) {

    }

    @Override
    public void notify(int errCode, int taskId, VerificationCodeResult verificationCodeResult) {

    }

    /**
     * 通过手机验证码登录回调通知
     * @param errCode
     * @param taskId
     * @param mobileLoginResult
     */
    @Override
    public void notify(int errCode, int taskId, MobileLoginResult mobileLoginResult) {

    }

    /**
     * 获取扫码登录二维码回调通知
     * @param errCode
     * @param taskId
     * @param qrCodeLoginResult
     */
    @Override
    public void notify(int errCode, int taskId, QRCodeLoginResult qrCodeLoginResult) {

    }

    @Override
    public void notify(int errCode, int taskId, QRCodeLoginConfirmResult qrCodeLoginConfirmResult) {

    }

    /**
     * 注销登录回调通知
     * @param errCode
     * @param taskId
     * @param accountLogoutResult
     */
    @Override
    public void notify(int errCode, int taskId, AccountLogoutResult accountLogoutResult) {

    }

    @Override
    public void notify(int errCode, int taskId, AccountRegisterResult accountRegisterResult) {

    }

    @Override
    public void notify(int errCode, int taskId, AccountUnRegisterResult accountUnRegisterResult) {

    }

    @Override
    public void notify(int errCode, int taskId, AccountProfileResult accountProfileResult) {

    }

    @Override
    public void notify(int errCode, int taskId, AvatarResult avatarResult) {

    }

    @Override
    public void notify(int errCode, int taskId, CarltdCheckBindResult carltdCheckBindResult) {

    }

    @Override
    public void notify(int errCode, int taskId, CarltdCheckTokenResult carltdCheckTokenResult) {

    }

    @Override
    public void notify(int errCode, int taskId, CarltdBindResult carltdBindResult) {

    }

    @Override
    public void notify(int errCode, int taskId, CarltdLoginResult carltdLoginResult) {

    }

    @Override
    public void notify(int errCode, int taskId, CarltdQLoginResult carltdQLoginResult) {

    }

    @Override
    public void notify(int errCode, int taskId, CarltdUnBindResult carltdUnBindResult) {

    }

    @Override
    public void notify(int errCode, int taskId, CarltdAuthInfoResult result) {

    }
}
