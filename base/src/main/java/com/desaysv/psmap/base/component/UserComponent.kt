package com.desaysv.psmap.base.component

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.layer.model.BizAGroupBusinessInfo
import com.autonavi.gbl.layer.model.BizAGroupType
import com.autonavi.gbl.user.group.model.GroupMember
import com.autosdk.bussiness.account.AccountController
import com.autosdk.bussiness.layer.AGroupLayer
import com.autosdk.common.SdkApplicationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils
import timber.log.Timber

class UserComponent {
    //组队头像缓存
    private val mGroupBitmaps: MutableMap<String, Bitmap> = HashMap()
    private var mIndex: String = "-1"
    private val count: Int = 0

    fun containsKey(url: String): Boolean = mGroupBitmaps.containsKey(url)


    fun putBitmapByUrl(url: String, bitmap: Bitmap) {
        mGroupBitmaps[url] = bitmap
    }

    fun getBitmapByUrl(url: String): Bitmap? = mGroupBitmaps[url]

    fun clearGroupBitmap() {
        mGroupBitmaps.clear()
    }


    fun updateAGroupMember(successful: Boolean, url: String, bitmap: Bitmap, businessInfo: BizAGroupBusinessInfo?, aGroupLayer: AGroupLayer?) {
        if (successful) {
            mGroupBitmaps[url] = bitmap
        } else {
            mGroupBitmaps[DEFAULT_HEAD] = bitmap
        }
        aGroupLayer!!.updateAGroupMember(businessInfo)
    }

    interface LoadGroupBitmap {
        fun result(url: String?, businessInfo: BizAGroupBusinessInfo?)
    }

    fun setGroupFocus(aGroupLayer: AGroupLayer?, isFocus: Boolean, index: String) {
        aGroupLayer?.let {
            if (index != "-1") {
                mIndex = index
            }
            it.setFocus(BizAGroupType.BizAGroupTypeAGroup.toLong(), mIndex, !isFocus)
        }
    }

    fun updateAllGroupMember(membersList: ArrayList<GroupMember>?, aGroupLayer: AGroupLayer?, isUpdatePosition: Boolean): ArrayList<GroupMember> {
        if (!isUpdatePosition) {
            aGroupLayer?.clearAllItems()
        }
        membersList?.let {
            if (it.isEmpty()) {
                return ArrayList()
            }
            var mUid = ""
            try {
                mUid = AccountController.getInstance().accountInfo.uid
            } catch (e: NullPointerException) {
                Timber.e("获取用户ID失败 e:%s", e.message)
            }
            if (mUid.isEmpty() || it[0].uid == mUid) {
                it.removeAt(0)
            }
            val memberList = ArrayList<BizAGroupBusinessInfo>()
            for (i in it.indices) {
                val member = BizAGroupBusinessInfo()
                member.id = it[i].uid.toString()
                member.priority = i
                member.mPos3D.lon = it[i].locInfo.lon
                member.mPos3D.lat = it[i].locInfo.lat
                memberList.add(member)
            }
            for (i in it.indices) {
                val imageUrl = it[i].imgUrl
                val defaultHead = if (NightModeGlobal.isNightMode()) R.drawable.ic_default_avatar_night else R.drawable.ic_default_avatar_day
                Glide.with(SdkApplicationUtils.getApplication()).asBitmap()
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform().skipMemoryCache(false))
                    .placeholder(defaultHead)
                    .error(defaultHead)
                    .addListener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                            Timber.d("onLoadFailed")
                            updateAGroupMember(
                                false,
                                imageUrl,
                                BitmapFactory.decodeResource(SdkApplicationUtils.getApplication().resources, defaultHead),
                                memberList[i],
                                aGroupLayer
                            )
                            return false
                        }

                        override fun onResourceReady(
                            resource: Bitmap,
                            model: Any,
                            target: Target<Bitmap>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            updateAGroupMember(true, imageUrl, CommonUtils.imageZoom(resource), memberList[i], aGroupLayer)
                            return false
                        }
                    })
                    .preload()
            }
            aGroupLayer?.addAGroupMembers(memberList)
        }
        return membersList ?: ArrayList()
    }

    companion object {
        const val DEFAULT_HEAD: String = "global_image_team_default_head"
        const val DEFAULT_SIZE: Int = 200
        private var instance: UserComponent? = null

        fun getInstance(): UserComponent {
            return instance ?: synchronized(this) {
                instance ?: UserComponent().also {
                    instance = it
                }
            }
        }
    }
}
