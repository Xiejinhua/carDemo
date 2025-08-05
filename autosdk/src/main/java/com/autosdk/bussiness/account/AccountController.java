package com.autosdk.bussiness.account;

import android.content.Context;
import android.text.TextUtils;

import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.user.account.AccountService;
import com.autonavi.gbl.user.account.model.AccountCheckRequest;
import com.autonavi.gbl.user.account.model.AccountLogoutRequest;
import com.autonavi.gbl.user.account.model.AccountProfile;
import com.autonavi.gbl.user.account.model.AccountProfileRequest;
import com.autonavi.gbl.user.account.model.AccountRegisterRequest;
import com.autonavi.gbl.user.account.model.AccountRequestType;
import com.autonavi.gbl.user.account.model.AccountServiceParam;
import com.autonavi.gbl.user.account.model.AvatarRequest;
import com.autonavi.gbl.user.account.model.CarltdBindRequest;
import com.autonavi.gbl.user.account.model.CarltdLoginRequest;
import com.autonavi.gbl.user.account.model.CarltdUnBindRequest;
import com.autonavi.gbl.user.account.model.MobileLoginRequest;
import com.autonavi.gbl.user.account.model.ProfileMode;
import com.autonavi.gbl.user.account.model.QRCodeLoginConfirmRequest;
import com.autonavi.gbl.user.account.model.QRCodeLoginRequest;
import com.autonavi.gbl.user.account.model.VerificationCodeRequest;
import com.autonavi.gbl.user.account.model.VerificationCodeType;
import com.autonavi.gbl.user.account.model.VerificationTargetType;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.account.observer.AccountServiceObserver;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.common.storage.MapSharePreference;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 用户模块M层
 * 降低用户操作回调冗余
 * 在需要监听操作回调的地方添加AccountServiceObserver观察者
 * @author AutoSDk
 */
public class AccountController {
    private AccountService mAccountService;
    private MapSharePreference preference;

    private AccountController() {

    }

    private static class AccountControllerHolder {
        private static AccountController instance = new AccountController();
    }

    public static AccountController getInstance() {
        return AccountControllerHolder.instance;
    }

    public void setContext(Context context){
        preference = new MapSharePreference(context, MapSharePreference.SharePreferenceName.account);
    }

    /**
     * 初始化用户服务
     *
     * @param accountDir
     */
    public void initService(String accountDir) {
        if (mAccountService == null) {
            mAccountService = (AccountService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.AccountSingleServiceID);
            AccountServiceParam param = new AccountServiceParam();
            param.dataPath = accountDir;
            // 保证传入目录存在
            FileUtils.createDir(param.dataPath);
            // 服务初始化
            int res = mAccountService.init(param);
            Timber.d("initAccountService: init=" + res);
        }
    }

    private static List<AccountServiceObserver> accountServiceObservers = new ArrayList<>();

    public void addObserver(AccountServiceObserver accountServiceObserver) {
        if (null != mAccountService) {
            mAccountService.addObserver(accountServiceObserver);
            accountServiceObservers.add(accountServiceObserver);
        }
    }

    public void removeObserver(AccountServiceObserver accountServiceObserver) {
        if (null != mAccountService) {
            mAccountService.removeObserver(accountServiceObserver);
            accountServiceObservers.remove(accountServiceObserver);
        }
    }

    /**
     * 获取验证码
     *
     * @param mobile 登录手机号
     */
    public void getVerficationCode(String mobile,boolean isAccountExist) {
        // 请求获取验证码
        VerificationCodeRequest loginVerifyReq = new VerificationCodeRequest();
        loginVerifyReq.targetValue = mobile;
        loginVerifyReq.targetType = VerificationTargetType.VerificationTargetTypeSms;
        // 手机号登录
        loginVerifyReq.codeType = VerificationCodeType.VerificationCodeTypeMobileLogin;
        loginVerifyReq.bindMode = true;
        loginVerifyReq.skipNew = isAccountExist;
        if (mAccountService == null) {
            return;
        }
        mAccountService.executeRequest(loginVerifyReq);
    }

    /**
     * 账号验证码注册
     *
     * @param mobile 手机账号
     * @param code   验证码
     */
    public void accountRegist(String mobile, String code) {
        // 注册请求
        AccountRegisterRequest registerReq = new AccountRegisterRequest();
        registerReq.code = code;
        registerReq.mobileNum = mobile;
        if (mAccountService == null) {
            return;
        }
        mAccountService.executeRequest(registerReq);
    }

    /**
     * 手机验证码登录
     *
     * @param mobile
     * @param code
     */
    public void mobileLogin(String mobile, String code) {
        // 登录请求
        MobileLoginRequest loginReq = new MobileLoginRequest();
        // 验证码
        loginReq.code = code;
        // 手机号
        loginReq.mobileNum = mobile;
        if (mAccountService == null) {
            return;
        }
        mAccountService.executeRequest(loginReq);
    }

    /**
     * 二维码登录
     */
    public AccountResult requestQrCodeLogin() {
        AccountResult result = new AccountResult();
        QRCodeLoginRequest qrReq = new QRCodeLoginRequest();
        if (mAccountService == null) {
            return result;
        }
        result.result = mAccountService.executeRequest(qrReq);
        result.taskId = qrReq.taskId;
        return result;
    }
    /**
     * 长轮询二维码是否被登录
     */
    public AccountResult requestCodeConfirm(String qrCodeId) {
        // 长轮询是否扫码
        AccountResult result = new AccountResult();
        QRCodeLoginConfirmRequest req = new QRCodeLoginConfirmRequest();
        req.qrcodeId = qrCodeId;
        if (mAccountService != null) {
            result.result = mAccountService.executeRequest(req);;
            result.taskId = req.taskId;
            return result;
        }
        return result;
    }

    public void abort(long taskid) {
        if (mAccountService != null) {
            mAccountService.abortRequest(taskid);
        }
    }

    /**
     * 获取登录账号信息
     */
    public int requestAccountProfile() {
        AccountProfileRequest profileReq = new AccountProfileRequest();
        //获取用户基本信息
        profileReq.mode = ProfileMode.ProfileModeBasic | ProfileMode.ProfileModeDefault | ProfileMode.ProfileModeCar | ProfileMode.ProfileModeContact | ProfileMode.ProfileModeProvider;
        if (mAccountService == null) {
            return -1;
        }
        return mAccountService.executeRequest(profileReq);
    }

    /**
     * 退出登录账号
     */
    public int requestAccountLogout() {
        // 退出登录请求
        AccountLogoutRequest logoutReq = new AccountLogoutRequest();
        if (mAccountService == null) {
            return -1;
        }
        return mAccountService.executeRequest(logoutReq);
    }

    /**
     * 验证账号是否存在
     */
    public void requestAccountCheck(String mobile) {
        AccountCheckRequest checkReq = new AccountCheckRequest();
        checkReq.mobileNum = mobile;
        if (mAccountService == null) {
            return;
        }
        mAccountService.executeRequest(checkReq);
    }

    /**
     * 获取用户头像
     *
     * @param avatar
     */
    public void requestAccountAvatar(String avatar) {
        AvatarRequest avatarReq = new AvatarRequest();
        avatarReq.url = avatar;
        if (mAccountService == null) {
            return;
        }
        mAccountService.executeRequest(avatarReq);
    }


    public AccountService getAccountService() {
        if (mAccountService != null) {
            return mAccountService;
        }
        return null;
    }

    /**
     * 获取用户信息
     */
    public AccountProfile getAccountInfo() {
        if (mAccountService == null) {
            return null;
        }
        return mAccountService.getUserData();
    }

    public int deleteAccountInfo() {
        if (mAccountService == null) {
            return Service.ErrorCodeFailed;
        }
        return mAccountService.deleteUserData();
    }

    public void uninit() {
        if (mAccountService != null) {
            //移除所有观察者
            for (AccountServiceObserver accountServiceObserver : accountServiceObservers) {
                mAccountService.removeObserver(accountServiceObserver);
            }
            accountServiceObservers.clear();
            mAccountService = null;
        }
    }

    /**
     * 是否登录账号
     * @return
     */
    public boolean isLogin(){
        String userName = preference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.userName, "");
        boolean isLoginAccount = (getAccountInfo() != null || !TextUtils.isEmpty(userName));
        Timber.d("isLogin isLoginAccount:%s", isLoginAccount);
        return isLoginAccount;
    }

    /**
     * 保存用户信息
     */
    public void saveUserData(AccountProfile userData) {
        if (null != mAccountService) {
            mAccountService.saveUserData(userData);
        }
    }

    /**
     * 车企账号绑定
     */
    public int requestAccountBind(String sourceId, String authId, String deviceCode) {
        if (null != mAccountService) {
            CarltdBindRequest carltdBindRequest = new CarltdBindRequest();
            carltdBindRequest.reqType = AccountRequestType.AccountTypeCarltdBind;
            carltdBindRequest.sourceId = sourceId; //高德分配网络请求源ID, 必传
            carltdBindRequest.authId = authId; // 车企账号ID, 必传
            carltdBindRequest.deviceCode = deviceCode; // 唯一设备ID
            int res = mAccountService.executeRequest(carltdBindRequest);
            Timber.i("call requestAccountBind result: %s", res);
            return res;
        } else {
            return -1;
        }
    }

    /**
     * 车企账号解绑
     */
    public int requestUnBindAccount(String sourceId, String authId, String deviceCode) {
        if (null != mAccountService) {
            CarltdUnBindRequest carltdUnBindRequest = new CarltdUnBindRequest();
            carltdUnBindRequest.reqType = AccountRequestType.AccountTypeCarltdUnBind;
            carltdUnBindRequest.sourceId = sourceId; //高德分配网络请求源ID, 必传
            carltdUnBindRequest.authId = authId; // 车企账号ID, 必传
            carltdUnBindRequest.deviceCode = deviceCode; // 车机设备唯一标识
            int res = mAccountService.executeRequest(carltdUnBindRequest);
            Timber.i("call requestUnBindAccount result: %s", res);
            return res;
        } else {
            return -1;
        }

    }

    /**
     * 车企账号快速登录
     */
    public int requestQuickLogin(String sourceId, String authId, String userId) {
        if (null != mAccountService) {
            CarltdLoginRequest carltdLoginRequest = new CarltdLoginRequest();
            carltdLoginRequest.reqType = AccountRequestType.AccountTypeCarltdLogin;
            carltdLoginRequest.sourceId = sourceId; //高德分配网络请求源ID, 必传
            carltdLoginRequest.authId = authId; // 车企账号ID, 必传
            carltdLoginRequest.userId = userId; // 高德账号ID, 必传
            int res = mAccountService.executeRequest(carltdLoginRequest);
            Timber.i("call requestQuickLogin result: " + res + " authId: " + authId);
            return res;
        } else {
            return -1;
        }
    }

    public static class AccountResult{
        public int result = Integer.MIN_VALUE;
        public long taskId = -1;
    }
}
