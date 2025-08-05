package com.autosdk.bussiness.account.observer;

import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem;
import com.autonavi.gbl.user.behavior.observer.IBehaviorServiceObserver;

/**
 * Created by AutoSdk on 2020/10/28.
 * @author AutoSDK
 **/
public interface BehaviorServiceObserver extends IBehaviorServiceObserver {
     void notifyFavorite(FavoriteBaseItem baseItem,boolean isDelete);
}
