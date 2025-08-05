package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.method.ArrowKeyMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.autonavi.auto.skin.view.SkinImageView
import com.autonavi.auto.skin.view.SkinProgressBar
import com.autonavi.auto.skin.view.SkinRadioButton
import com.autonavi.auto.skin.view.SkinTextView
import com.autonavi.gbl.data.model.CityDownLoadItem
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_CHECKED
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_CHECKING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DONE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_ERR
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_MAX
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPED
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.autonavi.gbl.data.model.Voice
import com.autonavi.gbl.search.model.DeepinfoPoi
import com.autonavi.gbl.search.model.SearchSuggestMark
import com.autonavi.gbl.user.behavior.model.FavoriteBaseItem
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.autonavi.gbl.user.msgpush.model.AimPushMsg
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.autonavi.gbl.util.model.BinaryStream
import com.autosdk.bussiness.account.BehaviorController
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.data.MapDataController
import com.autosdk.common.SdkApplicationUtils
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.model.bean.MineGuideList
import com.desaysv.psmap.model.bean.TankCollectItem
import com.desaysv.psmap.model.bean.TankMarkers
import com.desaysv.psmap.model.utils.EditTextUtil
import com.desaysv.psmap.model.utils.QrUtils
import com.desaysv.psmap.model.utils.SizeLabel
import com.desaysv.psmap.model.view.RoundProgressBar
import com.desaysv.psmap.model.view.ShadowLayout
import com.example.aha_api_sdkd01.manger.models.LineListModel
import com.sy.swbt.SettingSwitchView
import timber.log.Timber
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


/**
 * author: uidq0964
 * email: Ronghua.Deng@desaysv.com
 * create on: 2023/6/20 21:44
 * description: DataBinding的静态方法，在xml中做数据绑定时使用
 */
object BindingAdapters {
    /**
     * 控件自定义属性 visibleGone
     * 显示隐藏View
     *
     * @param view 需要操作的View
     * @param show 是否显示
     */
    @JvmStatic
    @BindingAdapter("visibleGone")
    fun showHide(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.GONE
    }


    @JvmStatic
    @BindingAdapter("invisible")
    fun invisibleOrNot(view: View, show: Boolean) {
        view.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    @JvmStatic
    @BindingAdapter("showInvisible", "showGone", requireAll = false)
    fun showHideOrGone(view: View, showInvisible: Boolean, showGone: Boolean) {
        view.visibility = if (showGone) View.GONE else if (showInvisible) View.INVISIBLE else View.VISIBLE
    }

    /**
     * 控件自定义属性 isDisable
     * 使控件失效，无法点击
     *
     * @param view    需要操作的View
     * @param disable 是否使控件失效，无法点击
     */
    @JvmStatic
    @BindingAdapter("isDisable")
    fun bindIsDisable(view: View, disable: Boolean) {
        if (disable) {
            view.alpha = 0.3f
            view.isEnabled = false
        } else {
            view.alpha = 1f
            view.isEnabled = true
        }
    }

    @JvmStatic
    @BindingAdapter("viewEnable")
    fun bindEnable(view: View, disable: Boolean) {
        view.isEnabled = disable
    }

    @JvmStatic
    @BindingAdapter("movementMethod")
    fun bindMovementMethod(view: TextView, horizontal: Boolean) {
        if (horizontal) {
            view.movementMethod = ArrowKeyMovementMethod.getInstance()
        } else {
            view.movementMethod = ScrollingMovementMethod.getInstance()
        }
    }

    /**
     * 序号排列
     */
    @JvmStatic
    @BindingAdapter("index")
    fun bindIndex(textView: TextView, position: Int) {
        if (position < 0) {
            textView.text = "0"
        } else if (position < 9) {
            textView.text = "0${position + 1}"
        } else {
            textView.text = (position + 1).toString()
        }
    }

    /**
     * 根据类型设置文字
     */
    @JvmStatic
    @BindingAdapter("poiType")
    fun bindPoiType(textView: TextView, poiType: Int) {
        val text = when (poiType) {
            3 -> "终"
            else -> "经"
        }
        textView.text = text
    }

    /**
     * TextView 设置途经点，终点按钮文字颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setTextColor"], requireAll = false)
    fun setTextColor(view: TextView, state: Boolean) {
        if (NightModeGlobal.isNightMode()) {
            view.setTextColor(
                if (state) SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerNight)
                else SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerPressNight)
            )
        } else {
            view.setTextColor(
                if (state) SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerDay)
                else SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerPressDay)
            )
        }
    }

    /**
     * View 按钮设置背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setViewBackground"], requireAll = false)
    fun setViewBackground(view: View, state: Boolean) {
        if (NightModeGlobal.isNightMode()) {
            view.setBackgroundResource(if (state) R.drawable.shape_btn_search_confirm_night else R.drawable.shape_btn_search_confirm_press_night)
        } else {
            view.setBackgroundResource(if (state) R.drawable.shape_btn_search_confirm_day else R.drawable.shape_btn_search_confirm_press_day)
        }
    }

    /**
     * 设置服务条款背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setTermsServiceBackground"], requireAll = false)
    fun setTermsServiceBackground(view: View, type: Int) {
        view.setBackgroundResource(
            if (NightModeGlobal.isNightMode()) R.color.black else R.color.white
        )
    }

    /**
     * 设置服务条款返回按钮
     */
    @JvmStatic
    @BindingAdapter(value = ["setTermsServiceBackIcon"], requireAll = false)
    fun setTermsServiceBackIcon(view: ImageView, type: Int) {
        view.setImageResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_close_night else R.drawable.ic_close_day)
    }

    /**
     * 设置服务条款文字颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setTermsServiceTitleColor"], requireAll = false)
    fun setTermsServiceTitleColor(view: TextView, type: Int) {
        view.setTextColor(
            if (NightModeGlobal.isNightMode()) view.resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryNight) else view.resources.getColor(
                com.desaysv.psmap.model.R.color.customColorTitleDay
            )
        )
    }

    /**
     * 设置服务条款重试按钮
     */
    @JvmStatic
    @BindingAdapter(value = ["setTermsServiceRetry"], requireAll = false)
    fun setTermsServiceRetry(view: View, type: Int) {
        view.setBackgroundResource(
            if (NightModeGlobal.isNightMode()) R.drawable.ic_refresh_night else R.drawable.ic_refresh_day
        )
    }

    /**
     * 设置loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateLargeDrawable"], requireAll = false)
    fun setIndeterminateLargeDrawable(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_100),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_100)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawable"], requireAll = false)
    fun setIndeterminateDrawable(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_96),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_96)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawableSmall"], requireAll = false)
    fun setIndeterminateDrawableSmall(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_60),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_60)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setActiveIndeterminateDrawable"], requireAll = false)
    fun setActiveIndeterminateDrawable(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_active_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_active_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_80),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_80)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawableMoreSmall"], requireAll = false)
    fun setIndeterminateDrawableMoreSmall(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_56),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_56)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawableFiftyFour"], requireAll = false)
    fun setIndeterminateDrawableFiftyFour(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_54),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_54)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置收藏夹loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawableFavorite"], requireAll = false)
    fun setIndeterminateDrawableFavorite(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_favorite_loading_night) else view.context.getDrawable(
                R.drawable.rotate_favorite_loading_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_56),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_56)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置搜索loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawableSearch"], requireAll = false)
    fun setIndeterminateDrawableSearch(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_64),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置行程分享loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawableTripShare"], requireAll = false)
    fun setIndeterminateDrawableTripShare(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_login_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_login_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_180),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_180)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * 设置登录loading样式
     */
    @JvmStatic
    @BindingAdapter(value = ["setIndeterminateDrawableLogin"], requireAll = false)
    fun setIndeterminateDrawableLogin(view: ProgressBar, night: Boolean) {
        val drawable =
            if (night) view.context.getDrawable(R.drawable.rotate_loading_login_view_night) else view.context.getDrawable(
                R.drawable.rotate_loading_login_view_day
            )
        drawable!!.setBounds(
            0,
            0,
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_180),
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_180)
        ) //这个四参数指的是drawable将在被绘制在canvas的哪个矩形区域内
        view.indeterminateDrawable = drawable
    }

    /**
     * Kotlin-Coil 加载图片url
     * @param imageView   显示图片的ImageView
     * @param url         图片url
     * @param bitmap      Bitmap 需要设置url为空
     * @param filePath    图片文件路径
     * @param resId       图片ResId
     * @param ph          占位图片资源
     * @param radius      圆角: NULL(没有圆角)；==0(圆形);>0(圆角值)
     */
    @JvmStatic
    @BindingAdapter(
        value = ["imageUrl", "bitmap", "qrCodeBinaryStream", "filePath", "resId", "placeHolder", "radius", "qrUrl", "qrWidthPix", "qrHeightPix"],
        requireAll = false
    )
    fun bindImageUrl(
        imageView: ImageView,
        url: String?,
        bitmap: Bitmap?,
        qrCodeImg: BinaryStream?,
        filePath: String?,
        resId: Int?,
        ph: Drawable?,
        radius: Int?,
        qrUrl: String?,
        qrWidthPix: Int?,
        qrHeightPix: Int?
    ) {
        val builder: ImageRequest.Builder.() -> Unit = {
            crossfade(true) //淡入淡出动画
            if (radius != null) {
                if (radius > 0) {
                    transformations(RoundedCornersTransformation(radius.toFloat())) //圆角图
                } else {
                    transformations(CircleCropTransformation()) //圆形图
                }
            } else {
                transformations(RoundedCornersTransformation()) //无圆角
            }
            if (ph != null) {
                placeholder(ph) //加载占位图
                error(ph) //加载错误图
            }
        }

        //二维码加载
        val finalBitmap = if (!qrUrl.isNullOrEmpty()) {
            QrUtils.createQRImage(qrUrl, qrWidthPix ?: 100, qrHeightPix ?: 100)
        } else if (qrCodeImg != null) {
            BitmapFactory.decodeByteArray(qrCodeImg.buffer, 0, qrCodeImg.buffer.size)
        } else {
            bitmap
        }
        if (!url.isNullOrEmpty()) {
            //URL网络链接加载
            imageView.load(url, builder = builder)
        } else if (finalBitmap != null) {
            //Bitmap加载
            imageView.load(finalBitmap, builder = builder)
        } else if (!filePath.isNullOrEmpty()) {
            //文件路径加载
            val file = File(filePath)
            if (file.exists()) {
                imageView.load(file, builder = builder)
            } else {
                if (ph != null) {
                    imageView.setImageDrawable(ph)
                } else {
                    Timber.e("bindImageUrl failed; params is null")
                }
            }
        } else if (resId != null) {
            //本地资源加载
            imageView.load(resId, builder = builder)
        } else {
            //兜底方式
            if (ph != null) {
                imageView.setImageDrawable(ph)
            } else {
                Timber.e("bindImageUrl failed; params is null")
            }
        }
    }

    /**
     * 设置车标朝向图标
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setCarModelBtnBackground", "setCarModelBtnNightModel"],
        requireAll = false
    )
    fun setCarModelBtnBackground(view: View, @MapModeType mapMode: Int, isNight: Boolean) {
        view.setBackgroundResource(
            when (mapMode) {

                MapModeType.VISUALMODE_2D_CAR -> {
                    if (isNight) R.drawable.selector_ic_car_mode_2d_night else R.drawable.selector_ic_car_mode_2d_day
                }

                MapModeType.VISUALMODE_2D_NORTH -> {
                    if (isNight) R.drawable.selector_ic_car_mode_north_night else R.drawable.selector_ic_car_mode_north_day
                }

                MapModeType.VISUALMODE_3D_CAR -> {
                    if (isNight) R.drawable.selector_ic_car_mode_3d_night else R.drawable.selector_ic_car_mode_3d_day
                }

                else -> {
                    Timber.w("setCarModelBtnBackground VISUALMODE_UNKNOW")
                    if (isNight) R.drawable.selector_ic_car_mode_2d_night else R.drawable.selector_ic_car_mode_2d_day
                }
            }
        )
    }

    /**
     * View设置item选中效果
     */
    @JvmStatic
    @BindingAdapter(value = ["setSelectView"], requireAll = false)
    fun setSelectView(view: View, select: Boolean) {
        view.isSelected = select
    }

    /**
     * RadioButton设置选中效果
     */
    @JvmStatic
    @BindingAdapter(value = ["setRadioButtonChecked"], requireAll = false)
    fun setRadioButtonChecked(view: RadioButton, isChecked: Boolean) {
        view.isChecked = isChecked
    }

    /**
     * 播放队列标题
     */
    @JvmStatic
    @BindingAdapter("htmlTitle")
    fun bindHtmlTitle(view: TextView, title: String?) {
        if (!TextUtils.isEmpty(title)) {
            view.text = Html.fromHtml(title, null, SizeLabel(24))
        }
    }

    /**
     * 设置Switch样式
     */
    @JvmStatic
    @BindingAdapter("switchSetDrawable", "switchSetChecked")
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun switchSetDrawable(switch: Switch, isNight: Boolean, isChecked: Boolean) {
        if (isChecked) {
            switch.thumbDrawable = if (isNight) ContextCompat.getDrawable(
                switch.context, R.drawable.switch_thumb_checked_night
            ) else ContextCompat.getDrawable(switch.context, R.drawable.switch_thumb_checked_day)
            switch.trackDrawable = if (isNight) ContextCompat.getDrawable(
                switch.context, R.drawable.switch_track_checked_night
            ) else ContextCompat.getDrawable(switch.context, R.drawable.switch_track_checked_day)
        } else {
            switch.thumbDrawable =
                if (isNight) ContextCompat.getDrawable(
                    switch.context,
                    R.drawable.switch_thumb_night
                ) else ContextCompat.getDrawable(
                    switch.context, R.drawable.switch_thumb_day
                )
            switch.trackDrawable =
                if (isNight) ContextCompat.getDrawable(
                    switch.context,
                    R.drawable.switch_track_night
                ) else ContextCompat.getDrawable(
                    switch.context, R.drawable.switch_track_day
                )
        }
        switch.isChecked = isChecked
    }

    /**
     * 设置Switch样式
     */
    @JvmStatic
    @BindingAdapter("switchViewSetDrawable", "switchViewSetChecked")
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    fun switchSetDrawable(switch: SettingSwitchView, isNight: Boolean, isChecked: Boolean) {
        switch.thumbDrawable = if (isNight) ContextCompat.getDrawable(
            switch.context, R.drawable.bg_switch_thumb_night
        ) else ContextCompat.getDrawable(switch.context, R.drawable.bg_switch_thumb_day)
        switch.backDrawable = if (isNight) ContextCompat.getDrawable(
            switch.context, R.drawable.bg_switch_track_night
        ) else ContextCompat.getDrawable(switch.context, R.drawable.bg_switch_track_day)
        switch.isChecked = isChecked
    }

    /**
     * 设置view HorizontalMargin
     */
    @JvmStatic
    @BindingAdapter(value = ["marginHorizontalMargin"], requireAll = false)
    fun marginHorizontalMargin(view: View, length: Int) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.rightMargin = length
        params.leftMargin = length
        view.layoutParams = params
    }

    /**
     * 设置收藏图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setViewFavorite"], requireAll = false)
    fun setViewFavorite(view: SkinImageView, isFavorite: Boolean) {
        if (view is TextView) {
            view.setText(if (isFavorite) com.desaysv.psmap.base.R.string.sv_common_favorited else com.desaysv.psmap.base.R.string.sv_common_favorite)
        } else {
            val backgroundResId = if (isFavorite) {
                view.setImageResource(
                    R.drawable.selector_ic_favorite_day,
                    R.drawable.selector_ic_favorite_night
                )
                if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_favorite_night else R.drawable.selector_ic_favorite_day
            } else {
                view.setImageResource(
                    R.drawable.selector_ic_un_favorite_day,
                    R.drawable.selector_ic_un_favorite_night
                )
                if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_un_favorite_night else R.drawable.selector_ic_un_favorite_day
            }
            view.setBackgroundResource(backgroundResId)
        }
    }

    /**
     * 设置收藏图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setViewIsFavorite", "setViewFavoriteIsNight"], requireAll = false)
    fun setViewFavoriteDayNight(view: View, isFavorite: Boolean, isNight: Boolean) {
        if (view is TextView) {
            view.setText(if (isFavorite) com.desaysv.psmap.base.R.string.sv_common_favorited else com.desaysv.psmap.base.R.string.sv_common_favorite)
        } else {
            val backgroundResId = if (isFavorite) {
                if (isNight) R.drawable.selector_ic_favorite_night else R.drawable.selector_ic_favorite_day
            } else {
                if (isNight) R.drawable.selector_ic_un_favorite_night else R.drawable.selector_ic_un_favorite_day
            }
            view.setBackgroundResource(backgroundResId)
        }
    }

    /**
     * 微信发送位置
     */
    private val WX_SEND_STEP_NIGHT = intArrayOf(
        R.drawable.ic_bg_wechat_tip_open_night,
        R.drawable.ic_bg_wechat_tip_send_night,
        R.drawable.ic_bg_wechat_tip_has_send_night
    )
    private val WX_SEND_STEP = intArrayOf(
        R.drawable.ic_bg_wechat_tip_open_day,
        R.drawable.ic_bg_wechat_tip_send_day,
        R.drawable.ic_bg_wechat_tip_has_send_day
    )

    @JvmStatic
    @BindingAdapter("wxStep")
    fun bindWxStep(view: ImageView, index: Int) {
        if (index in 1..3) {
            if (NightModeGlobal.isNightMode()) {
                view.setImageResource(WX_SEND_STEP_NIGHT[index - 1])
            } else {
                view.setImageResource(WX_SEND_STEP[index - 1])
            }
        }
    }

    /**
     * recyclerView设置setLayoutManager
     */
    @JvmStatic
    @BindingAdapter(value = ["setLayoutManager"], requireAll = false)
    fun setLayoutManager(recyclerView: RecyclerView, isVertical: Boolean) {
        val linearLayoutManager = LinearLayoutManager(recyclerView.context)
        linearLayoutManager.orientation =
            if (isVertical) RecyclerView.VERTICAL else RecyclerView.HORIZONTAL
        recyclerView.layoutManager = linearLayoutManager
    }

    /**
     * recyclerView设置setGridLayoutManager
     */
    @JvmStatic
    @BindingAdapter(value = ["setGridOrientation", "setGridSpanCount"], requireAll = false)
    fun setGridLayoutManager(recyclerView: RecyclerView, isVertical: Boolean, spanCount: Int) {
        recyclerView.itemAnimator?.run {
            changeDuration = 0
            addDuration = 0
            moveDuration = 0
            removeDuration = 0
            (this as SimpleItemAnimator).supportsChangeAnimations = false
        }
        recyclerView.layoutManager =
            GridLayoutManager(
                recyclerView.context,
                spanCount,
                if (isVertical) GridLayoutManager.VERTICAL else GridLayoutManager.HORIZONTAL,
                false
            )
    }

    private val trafficEventIconsDay = mapOf(
        11010 to R.drawable.ic_image_auto_traffic_11010_day,
        11011 to R.drawable.ic_image_auto_traffic_11011_day,
        11012 to R.drawable.ic_image_auto_traffic_11012_day,
        11021 to R.drawable.ic_image_auto_traffic_11021_day,
        11031 to R.drawable.ic_image_auto_traffic_11031_day,
        11040 to R.drawable.ic_image_auto_traffic_11040_day,
        11050 to R.drawable.ic_image_auto_traffic_11050_day,
        11060 to R.drawable.ic_image_auto_traffic_11060_day,
        11070 to R.drawable.ic_image_auto_traffic_11070_day,
        11071 to R.drawable.ic_image_auto_traffic_11071_day,
        11072 to R.drawable.ic_image_auto_traffic_11060_day,
        11100 to R.drawable.ic_image_auto_traffic_11100_day
    )

    private val trafficEventIconsNight = mapOf(
        11010 to R.drawable.ic_image_auto_traffic_11010_night,
        11011 to R.drawable.ic_image_auto_traffic_11011_night,
        11012 to R.drawable.ic_image_auto_traffic_11012_night,
        11021 to R.drawable.ic_image_auto_traffic_11021_night,
        11031 to R.drawable.ic_image_auto_traffic_11031_night,
        11040 to R.drawable.ic_image_auto_traffic_11040_night,
        11050 to R.drawable.ic_image_auto_traffic_11050_night,
        11060 to R.drawable.ic_image_auto_traffic_11060_night,
        11070 to R.drawable.ic_image_auto_traffic_11070_night,
        11071 to R.drawable.ic_image_auto_traffic_11071_night,
        11072 to R.drawable.ic_image_auto_traffic_11060_night,
        11100 to R.drawable.ic_image_auto_traffic_11100_night
    )

    /**
     * 设置交通详情卡片类型图标
     */
    @JvmStatic
    @BindingAdapter(value = ["bindTrafficCardIcon"], requireAll = false)
    fun bindTrafficCardIcon(view: ImageView, layerTag: Int) {
        if (NightModeGlobal.isNightMode()) {
            view.setImageResource(
                trafficEventIconsNight.getOrDefault(
                    layerTag,
                    R.drawable.ic_image_auto_traffic_11060_night
                )
            )
        } else {
            view.setImageResource(
                trafficEventIconsDay.getOrDefault(
                    layerTag,
                    R.drawable.ic_image_auto_traffic_11060_day
                )
            )
        }
    }

    private val trafficEventTextList = mapOf(
        11010 to R.string.sv_map_traffic_event_11010,
        11011 to R.string.sv_map_traffic_event_11011,
        11012 to R.string.sv_map_traffic_event_11012,
        11021 to R.string.sv_map_traffic_event_11021,
        11031 to R.string.sv_map_traffic_event_11031,
        11040 to R.string.sv_map_traffic_event_11040,
        11050 to R.string.sv_map_traffic_event_11050,
        11060 to R.string.sv_map_traffic_event_11060,
        11070 to R.string.sv_map_traffic_event_11070,
        11071 to R.string.sv_map_traffic_event_11071,
        11072 to R.string.sv_map_traffic_event_11060,
        11100 to R.string.sv_map_traffic_event_11100
    )

    /**
     * 设置交通详情卡片标题
     */
    @JvmStatic
    @BindingAdapter(value = ["bindTrafficCardHeadTitle"], requireAll = false)
    fun bindTrafficCardHeadTitle(view: TextView, layerTag: Int) {
        view.setText(
            trafficEventTextList.getOrDefault(
                layerTag,
                R.string.sv_map_traffic_event_11060
            )
        )
    }

    /**
     * View 清除消息按钮背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setMessageClearBackground"], requireAll = false)
    fun setMessageClearBackground(view: View, state: Boolean) {
        if (NightModeGlobal.isNightMode()) {
            view.setBackgroundResource(if (state) R.drawable.selector_bg_cancel_day else R.drawable.shape_bg_unenable_clear_night)
        } else {
            view.setBackgroundResource(if (state) R.drawable.selector_bg_cancel_night else R.drawable.shape_bg_unenable_clear_day)
        }
        view.isSoundEffectsEnabled = state
    }

    /**
     * TextView 设置清除消息字体颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setMessageClearColor"], requireAll = false)
    fun setMessageClearColor(view: TextView, state: Boolean) {
        val context = view.context
        val textColor = if (NightModeGlobal.isNightMode()) {
            if (state) {
                ContextCompat.getColor(
                    context,
                    com.desaysv.psmap.model.R.color.onPrimaryNight
                )
            } else {
                ContextCompat.getColor(context, com.desaysv.psmap.model.R.color.onSecondaryNight)
            }
        } else {
            if (state) {
                ContextCompat.getColor(context, com.desaysv.psmap.model.R.color.primaryContainerDay)
            } else {
                ContextCompat.getColor(context, com.desaysv.psmap.model.R.color.customColorTitleDay)
            }
        }
        view.setTextColor(textColor)
        view.isSoundEffectsEnabled = state
    }

    /**
     * ImageView 消息列表设置图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setMsgIconType"], requireAll = false)
    fun setImageIcon(imageView: ImageView, type: Int) {
        val nightMode = NightModeGlobal.isNightMode()
        val resId = when (type) {
            1, 4 -> if (nightMode) R.drawable.ic_message_poi_night else R.drawable.ic_message_poi_day
            2 -> if (nightMode) R.drawable.ic_message_route_night else R.drawable.ic_message_route_day
            3 -> if (nightMode) R.drawable.ic_message_team_night else R.drawable.ic_message_team_day
            else -> 0
        }
        if (resId != 0) {
            imageView.setImageResource(resId)
        }
    }

    /**
     * TextView 设置消息红点显示
     */
    @JvmStatic
    @BindingAdapter(value = ["setAimPushMsgRed", "setTeamPushMsgRed"], requireAll = false)
    fun setMsgRed(view: ImageView, aimPushMsg: AimPushMsg?, teamPushMsg: TeamPushMsg?) {
        val isRead = if (aimPushMsg != null) {
            if (isPoiMsg(aimPushMsg)) {
                aimPushMsg.aimPoiMsg?.isReaded
            } else if (isRouteMsg(aimPushMsg)) {
                aimPushMsg.aimRouteMsg?.isReaded
            } else {
                false
            }
        } else teamPushMsg?.isReaded ?: false
        view.visibility = if (isRead == true) View.GONE else View.VISIBLE
    }

    private fun isPoiMsg(aimPushMsg: AimPushMsg?): Boolean {
        return aimPushMsg?.aimPoiMsg != null && !TextUtils.isEmpty(aimPushMsg.aimPoiMsg.createTime)
    }

    private fun isRouteMsg(aimPushMsg: AimPushMsg?): Boolean {
        return aimPushMsg?.aimRouteMsg != null && !TextUtils.isEmpty(aimPushMsg.aimRouteMsg.createTime)
    }

    /**
     * TextView 设置消息标题
     */
    @JvmStatic
    @BindingAdapter(value = ["setAimPushMsgTitle", "setTeamPushMsgTitle"], requireAll = false)
    fun setMsgTitle(textView: TextView, aimPushMsg: AimPushMsg?, teamPushMsg: TeamPushMsg?) {
        val name = if (aimPushMsg != null) {
            if (isPoiMsg(aimPushMsg)) {
                "来自手机发送的位置"
            } else if (isRouteMsg(aimPushMsg)) {
                "来自手机发送的路线"
            } else {
                ""
            }
        } else if (teamPushMsg != null) {
            if (TextUtils.equals(teamPushMsg.content?.type, "INVITE") || TextUtils.equals(
                    teamPushMsg.content?.type,
                    "KICK"
                )
            ) {
                teamPushMsg.title
            } else if (TextUtils.equals(teamPushMsg.content?.type, "DISMISS")) {
                "队长已解散队伍~"
            } else {
                "${teamPushMsg.title}, ${teamPushMsg.text}"
            }
        } else {
            ""
        }
        textView.text = name
    }

    /**
     * TextView 设置消息日期title
     */
    @RequiresApi(Build.VERSION_CODES.O)
    @JvmStatic
    @BindingAdapter(value = ["setDateTitle"], requireAll = false)
    fun setDateTitle(textView: TextView, title: String?) {
        textView.text = CommonUtils.descriptiveData(title)
    }

    /**
     * TextView 设置消息名称
     */
    @JvmStatic
    @BindingAdapter(value = ["setAimPushMsgName", "setTeamPushMsgName"], requireAll = false)
    fun setMsgName(textView: TextView, aimPushMsg: AimPushMsg?, teamPushMsg: TeamPushMsg?) {
        val name = if (aimPushMsg != null) {
            if (isPoiMsg(aimPushMsg)) {
                aimPushMsg.aimPoiMsg?.content?.name
            } else if (isRouteMsg(aimPushMsg)) {
                aimPushMsg.aimRouteMsg?.content?.routeParam?.destination?.name
            } else {
                ""
            }
        } else if (teamPushMsg != null) {
            if (TextUtils.equals(teamPushMsg.content?.type, "INVITE") || TextUtils.equals(
                    teamPushMsg.content?.type,
                    "KICK"
                )
            ) {
                teamPushMsg.text
            } else if (TextUtils.equals(teamPushMsg.content?.type, "DISMISS")) {
                "下次我们再结伴出去耍吧~"
            } else {
                "${teamPushMsg.title}, ${teamPushMsg.text}"
            }
        } else {
            ""
        }
        textView.text = name
    }

    /**
     * TextView 设置消息时间
     */
    @JvmStatic
    @BindingAdapter(value = ["setAimPushMsgTime", "setTeamPushMsgTime"], requireAll = false)
    fun setMsgTime(textView: TextView, aimPushMsg: AimPushMsg?, teamPushMsg: TeamPushMsg?) {
        val date = if (aimPushMsg != null) {
            if (isPoiMsg(aimPushMsg)) {
                CommonUtil.switchDate(aimPushMsg.aimPoiMsg.createTime)
            } else if (isRouteMsg(aimPushMsg)) {
                CommonUtil.switchDate(aimPushMsg.aimRouteMsg.createTime)
            } else {
                ""
            }
        } else if (teamPushMsg != null) {
            CommonUtil.switchDate(teamPushMsg.createTime)
        } else {
            ""
        }
        textView.text = date
    }

    /**
     * TextView 设置快充显隐
     */
    @JvmStatic
    @BindingAdapter(value = ["chargeFastPoiShow", "chargeFastDeepinShow"], requireAll = false)
    fun setupChargeFastLayoutShow(view: View, poi: POI?, deepinfoPoi: DeepinfoPoi?) {
        if (poi != null && poi.chargeStationInfo != null && !TextUtils.isEmpty(poi.chargeStationInfo.num_fast) && poi.chargeStationInfo.num_fast.toInt() > 0) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    /**
     * TextView 设置快充数量
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["chargeFastPoiContent", "chargeFastDeepinContent"], requireAll = false)
    fun setupChargeFastContent(textView: TextView, poi: POI?, deepinfoPoi: DeepinfoPoi?) {
        if (poi != null && poi.chargeStationInfo != null && !TextUtils.isEmpty(poi.chargeStationInfo.num_fast)) textView.setText(
            poi.chargeStationInfo.num_fast + "个"
        ) else if (deepinfoPoi != null && deepinfoPoi.chargeData != null && deepinfoPoi.chargeData.size > 0 && deepinfoPoi.chargeData[0].num_fast >= 0) textView.text =
            deepinfoPoi.chargeData[0].num_fast.toString() + "个"
    }

    /**
     * TextView 设置慢充显隐
     */
    @JvmStatic
    @BindingAdapter(value = ["chargeSlowPoiShow", "chargeSlowDeepinShow"], requireAll = false)
    fun setupChargeSlowLayoutShow(view: View, poi: POI?, deepinfoPoi: DeepinfoPoi?) {
        if (poi != null && poi.chargeStationInfo != null && !TextUtils.isEmpty(poi.chargeStationInfo.num_slow) && poi.chargeStationInfo.num_slow.toInt() > 0) {
            view.visibility = View.VISIBLE
        } else if (deepinfoPoi != null && deepinfoPoi.chargeData != null && deepinfoPoi.chargeData.size > 0 && deepinfoPoi.chargeData[0].num_slow > 0) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    /**
     * TextView 设置慢充数量
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["chargeSlowPoiContent", "chargeSlowDeepinContent"], requireAll = false)
    fun setupChargeSlowContent(textView: TextView, poi: POI?, deepinfoPoi: DeepinfoPoi?) {
        if (poi != null && poi.chargeStationInfo != null && !TextUtils.isEmpty(poi.chargeStationInfo.num_slow)) textView.setText(
            poi.chargeStationInfo.num_slow + "个"
        ) else if (deepinfoPoi != null && deepinfoPoi.chargeData != null && deepinfoPoi.chargeData.size > 0 && deepinfoPoi.chargeData[0].num_slow >= 0) textView.text =
            deepinfoPoi.chargeData[0].num_slow.toString() + "个"
    }

    /**
     * TextView 设置车位数量
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["setupParkSpaces"], requireAll = false)
    fun setupParkSpaces(textView: TextView, deepinfoPoi: DeepinfoPoi?) {
        if (deepinfoPoi == null) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.parkinfo == null) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.parkinfo.sumSpace <= 0) {
            textView.visibility = View.GONE
        } else {
            textView.text = "车位：总共${deepinfoPoi.parkinfo.sumSpace}个"
            textView.visibility = View.VISIBLE
        }
    }

    /**
     * TextView 设置停车价格
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["setupChargeParkPrice"], requireAll = false)
    fun setupChargeParkPrice(textView: TextView, deepinfoPoi: DeepinfoPoi?) {
        if (deepinfoPoi == null) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData.isEmpty()) {
            textView.visibility = View.GONE
        } else if (TextUtils.isEmpty(deepinfoPoi.chargeData[0].price_parking)) {
            textView.visibility = View.GONE
        } else {
            textView.text = deepinfoPoi.chargeData[0].price_parking
            textView.visibility = View.VISIBLE
        }
    }

    /**
     * TextView 设置停车价格title
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["setupChargeParkTitle"], requireAll = false)
    fun setupChargeParkTitle(textView: TextView, deepinfoPoi: DeepinfoPoi?) {
        if (deepinfoPoi == null) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData.isEmpty()) {
            textView.visibility = View.GONE
        } else if (TextUtils.isEmpty(deepinfoPoi.chargeData[0].price_parking)) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
        }
    }

    /**
     * TextView 设置充电价格
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["setupChargePrice"], requireAll = false)
    fun setupChargePrice(textView: TextView, deepinfoPoi: DeepinfoPoi?) {
        if (deepinfoPoi == null) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData.isEmpty()) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData[0].chargingPrice.isEmpty()) {
            textView.visibility = View.GONE
        } else if (TextUtils.isEmpty(deepinfoPoi.chargeData[0].chargingPrice[0].ele_price)) {
            textView.visibility = View.GONE
        } else {
            textView.text =
                "充电价格  " + deepinfoPoi.chargeData[0].chargingPrice[0].ele_price + "元/度"
            textView.visibility = View.VISIBLE
        }
    }

    /**
     * TextView 设置服务价格
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["setupChargeSerPrice"], requireAll = false)
    fun setupChargeSerPrice(textView: TextView, deepinfoPoi: DeepinfoPoi?) {
        if (deepinfoPoi == null) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData.isEmpty()) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData[0].chargingPrice.isEmpty()) {
            textView.visibility = View.GONE
        } else if (TextUtils.isEmpty(deepinfoPoi.chargeData[0].chargingPrice[0].ser_price)) {
            textView.visibility = View.GONE
        } else {
            textView.text =
                "服务价格  " + deepinfoPoi.chargeData[0].chargingPrice[0].ser_price + "元/度"
            textView.visibility = View.VISIBLE
        }
    }

    /**
     * TextView 设置充电提示
     */
    @JvmStatic
    @SuppressLint("SetTextI18n")
    @BindingAdapter(value = ["setupChargeTip"], requireAll = false)
    fun setupChargeTip(textView: TextView, deepinfoPoi: DeepinfoPoi?) {
        if (deepinfoPoi == null) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData.isEmpty()) {
            textView.visibility = View.GONE
        } else if (deepinfoPoi.chargeData[0].chargingPrice.isEmpty() || TextUtils.isEmpty(
                deepinfoPoi.chargeData[0].chargingPrice[0].ser_price
            ) || TextUtils.isEmpty(
                deepinfoPoi.chargeData[0].chargingPrice[0].ele_price
            )
        ) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
        }
    }

    /**
     * TextView 设置电话号码
     */
    @JvmStatic
    @BindingAdapter(value = ["setPhoneText"], requireAll = false)
    fun setPhoneText(textView: TextView, poi: POI?) {
        if (poi == null) {
            textView.visibility = View.GONE
        } else {
            if (TextUtils.isEmpty(poi.phone)) {
                textView.visibility = View.GONE
            } else {
                textView.visibility = View.VISIBLE
                textView.text =
                    poi.phone.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            }
        }
    }


    /**
     * RoundProgressBar 设置圆环进度的颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setRoundProgressColor"], requireAll = false)
    fun setRoundProgressColor(progressBar: RoundProgressBar, isNight: Boolean) {
        progressBar.setCircleProgressColor(isNight)
    }

    /**
     * 离线数据 RoundProgressBar进度显示及显隐
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["setRoundProgressBar", "setProgressWorkingQueueList"],
        requireAll = false
    )
    fun setRoundProgressBar(
        progressBar: RoundProgressBar,
        cityDownLoadItem: CityDownLoadItem?,
        workingQueueAdCodeList: ArrayList<Int>?
    ) {
        if (cityDownLoadItem != null) {
            when (cityDownLoadItem.taskState) {
                TASK_STATUS_CODE_ERR -> {
                    progressBar.visibility = View.GONE
                }

                TASK_STATUS_CODE_READY -> {
                    progressBar.visibility = View.GONE
                    progressBar.setProgress(100)
                }

                TASK_STATUS_CODE_WAITING -> if (workingQueueAdCodeList!!.size == 1) {
                    //当前操作的下载不显示等待中
                } else {
                    progressBar.visibility = View.VISIBLE
                    progressBar.setProgress(cityDownLoadItem.percent.toInt())
                }

                TASK_STATUS_CODE_DOING -> {
                    progressBar.visibility = View.VISIBLE
                    progressBar.setProgress(cityDownLoadItem.percent.toInt())
                }

                TASK_STATUS_CODE_PAUSE -> {
                    val percent = cityDownLoadItem.percent.toInt()
                    if (percent > 0) {
                        progressBar.visibility = View.VISIBLE
                        progressBar.setProgress(percent)
                    } else {
                        progressBar.visibility = View.GONE
                    }
                }

                TASK_STATUS_CODE_CHECKING -> {
                    progressBar.visibility = View.VISIBLE
                    progressBar.setProgress(100)
                }

                TASK_STATUS_CODE_UNZIPPING -> {
                    progressBar.visibility = View.VISIBLE
                    progressBar.setProgress(cityDownLoadItem.percent.toInt())
                }

                TASK_STATUS_CODE_UNZIPPED -> {
                    progressBar.visibility = View.VISIBLE
                    progressBar.setProgress(100)
                }

                TASK_STATUS_CODE_SUCCESS -> {
                    progressBar.visibility = View.GONE
                }

                else -> {
                    Timber.d("setRoundProgressBar cityDownLoadItem.taskState: ${cityDownLoadItem.taskState}")
                }
            }
        }
    }

    /**
     * 离线数据 X个城市下载更新中...
     */
    @JvmStatic
    @BindingAdapter(value = ["setOfflineDownloadNum"], requireAll = false)
    fun setOfflineDownloadNum(textView: TextView, num: Int) {
        textView.text = textView.resources.getString(R.string.sv_custom_offline_data_download_num, num)
        textView.visibility = if (num <= 0) View.GONE else View.VISIBLE
    }

    /**
     * 离线数据 城市大小
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["setOfflineCitySize", "setWorkingQueueAdCodeList", "provinceFullZipSize", "isAllProvince"],
        requireAll = false
    )
    fun setOfflineCitySize(
        textView: TextView,
        cityDownLoadItem: CityDownLoadItem?,
        workingQueueAdCodeList: ArrayList<Int>?,
        provinceFullZipSize: Long = 0L,
        isAllProvince: Boolean = false,
    ) {
        if (isAllProvince) {
            textView.text = CustomFileUtils.formetFileSize(provinceFullZipSize)
        } else {
            if (cityDownLoadItem != null) {
                when (cityDownLoadItem.taskState) {
                    TASK_STATUS_CODE_ERR, TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_PAUSE,
                    TASK_STATUS_CODE_CHECKING, TASK_STATUS_CODE_UNZIPPING, TASK_STATUS_CODE_UNZIPPED, TASK_STATUS_CODE_SUCCESS -> {
                        textView.text =
                            CustomFileUtils.formetFileSize(cityDownLoadItem.nFullZipSize.toLong())
                    }

                    TASK_STATUS_CODE_READY -> {
                        textView.text =
                            if (cityDownLoadItem.bUpdate) {
                                CustomFileUtils.formetFileSize(cityDownLoadItem.nZipSize.toLong())
                            } else {
                                CustomFileUtils.formetFileSize(cityDownLoadItem.nFullZipSize.toLong())
                            }
                    }

                    TASK_STATUS_CODE_WAITING ->
                        if (workingQueueAdCodeList!!.size == 1) {
                            //当前操作的下载不显示等待中
                        } else {
                            textView.text =
                                CustomFileUtils.formetFileSize(cityDownLoadItem.nFullZipSize.toLong())
                        }

                    else -> {
                        Timber.d("setOfflineCitySize cityDownLoadItem.taskState: ${cityDownLoadItem.taskState}")
                    }
                }
            }
        }
    }

    /**
     * 离线数据 城市下载进度txt
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["setOfflineCityProgressTxt", "setProgressTxtWorkingQueueList"],
        requireAll = false
    )
    fun setOfflineCityProgressTxt(
        progress: SkinProgressBar,
        cityDownLoadItem: CityDownLoadItem?,
        workingQueueAdCodeList: ArrayList<Int>?
    ) {
        if (cityDownLoadItem != null) {
            when (cityDownLoadItem.taskState) {
                TASK_STATUS_CODE_ERR -> {
                    progress.progress = 0
                }

                TASK_STATUS_CODE_READY -> {
                    progress.progress = 0
                }

                TASK_STATUS_CODE_WAITING -> if (workingQueueAdCodeList!!.size == 1) {
                    progress.progress = 0
                } else {
                    progress.progress = cityDownLoadItem.percent.toInt()
                }

                TASK_STATUS_CODE_DOING -> {
                    progress.progress = cityDownLoadItem.percent.toInt()
                }

                TASK_STATUS_CODE_PAUSE -> {
                    val percent = cityDownLoadItem.percent.toInt()
                    if (percent > 0) {
                        progress.progress = percent
                    } else {
                        progress.progress = 0
                    }
                }

                TASK_STATUS_CODE_CHECKING -> {
                    progress.progress = cityDownLoadItem.percent.toInt()
                }

                TASK_STATUS_CODE_UNZIPPING -> {
                    progress.progress = cityDownLoadItem.percent.toInt()
                }

                TASK_STATUS_CODE_UNZIPPED -> {
                    progress.progress = 0
                }

                TASK_STATUS_CODE_SUCCESS -> {
                    progress.progress = 0
                }

                else -> {
                    Timber.d("setOfflineCityProgressTxt cityDownLoadItem.taskState: ${cityDownLoadItem.taskState}")
                }
            }
        }
    }

    /**
     * 离线数据 城市操作按钮
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["setOfflineCityBtn", "setBtnWorkingQueueList", "setOfflineCityBtnTextNight"],
        requireAll = false
    )
    fun setOfflineCityBtn(
        textView: SkinTextView,
        cityDownLoadItem: CityDownLoadItem?,
        workingQueueAdCodeList: ArrayList<Int>?,
        isNight: Boolean
    ) {
        if (cityDownLoadItem != null) {
            when (cityDownLoadItem.taskState) {
                TASK_STATUS_CODE_ERR -> {
                    Timber.i("setOfflineCityBtn TASK_STATUS_CODE_ERR")
                    textView.text = "继续"
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_READY -> {
                    if (cityDownLoadItem.bUpdate) {
                        textView.text = "更新"
                    } else {
                        textView.text = "下载"
                    }
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_WAITING -> if (workingQueueAdCodeList!!.size == 1) {
                    //当前操作的下载不显示等待中
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                } else {
                    textView.text = "等待中"
                    val percent = cityDownLoadItem.percent.toInt()
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_DOING -> { //执行中
                    val percent = cityDownLoadItem.percent.toInt()
                    textView.text = cityDownLoadItem.percent.toInt().toString() + "%"
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_PAUSE -> {
                    val percent = cityDownLoadItem.percent.toInt()
                    textView.text = "继续"
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_CHECKING -> { //校验中
                    val percent = cityDownLoadItem.percent.toInt()
                    textView.text = cityDownLoadItem.percent.toInt().toString() + "%"
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_UNZIPPING -> { //解压中
                    val percent = cityDownLoadItem.percent.toInt()
                    textView.text = cityDownLoadItem.percent.toInt().toString() + "%"
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_UNZIPPED -> {
                    textView.text = "解压完成"
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                TASK_STATUS_CODE_SUCCESS -> {
                    textView.text = "已下载"
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                }

                else -> {
                    Timber.d("setOfflineCityBtn cityDownLoadItem.taskState: ${cityDownLoadItem.taskState}")
                }
            }
        }
    }

    /**
     * 离线数据 附近城市推荐操作按钮
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setOfflineNearCityBtn", "setOfflineNearCityBtnNightState"],
        requireAll = false
    )
    fun setOfflineNearCityBtn(imageView: ImageView, cityDownLoadState: Int, nightMode: Boolean) {
        Timber.d("setOfflineNearCityBtn cityDownLoadState: $cityDownLoadState, nightMode: $nightMode")
        when (cityDownLoadState) {
            BaseConstant.OFFLINE_STATE_UPDATE -> { //更新状态
                imageView.setImageResource(if (nightMode) R.drawable.selector_ic_data_update_button_night else R.drawable.selector_ic_data_update_button_day)
            }

            BaseConstant.OFFLINE_STATE_T0_DOWNLOAD -> { //全新未下载状态
                imageView.setImageResource(if (nightMode) R.drawable.selector_ic_data_not_download_night else R.drawable.selector_ic_data_not_download_day)
            }

            BaseConstant.OFFLINE_STATE_DOWNLOAD -> { //下载状态
                imageView.setImageResource(if (nightMode) R.drawable.selector_ic_data_pause_night else R.drawable.selector_ic_data_pause_day)
            }

            BaseConstant.OFFLINE_STATE_PAUSE -> { //暂停状态
                imageView.setImageResource(if (nightMode) R.drawable.selector_ic_data_download_night else R.drawable.selector_ic_data_download_day)
            }

            BaseConstant.OFFLINE_STATE_COMPLETE -> { //完成状态
                imageView.setImageResource(if (nightMode) R.drawable.selector_ic_data_complete_night else R.drawable.selector_ic_data_complete_day)
            }

            else -> {
                Timber.d("setOfflineNearCityBtn cityDownLoadState: $cityDownLoadState")
            }
        }
    }

    /**
     * 离线数据 列表item城市大小
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["setOfflineItemCitySize", "setItemWorkingQueueList"],
        requireAll = false
    )
    fun setOfflineItemCitySize(
        textView: TextView,
        cityDownLoadItem: CityDownLoadItem?,
        workingQueueAdCodeList: ArrayList<Int>?
    ) {
        if (cityDownLoadItem != null) {
            when (cityDownLoadItem.taskState) {
                TASK_STATUS_CODE_ERR, TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_PAUSE,
                TASK_STATUS_CODE_CHECKING, TASK_STATUS_CODE_UNZIPPING, TASK_STATUS_CODE_UNZIPPED, TASK_STATUS_CODE_SUCCESS -> {
                    textView.text =
                        CustomFileUtils.formetFileSize(cityDownLoadItem.nFullZipSize.toLong())
                }

                TASK_STATUS_CODE_READY -> {
                    textView.text =
                        if (cityDownLoadItem.bUpdate) {
                            CustomFileUtils.formetFileSize(cityDownLoadItem.nZipSize.toLong())
                        } else {
                            CustomFileUtils.formetFileSize(cityDownLoadItem.nFullZipSize.toLong())
                        }
                }

                TASK_STATUS_CODE_WAITING ->
                    when (workingQueueAdCodeList?.size) {
                        0 -> {
                            textView.text =
                                CustomFileUtils.formetFileSize(cityDownLoadItem.nFullZipSize.toLong())
                        }

                        1 -> {
                            //当前操作的下载不显示等待中
                        }

                        else -> {
                            textView.text =
                                CustomFileUtils.formetFileSize(cityDownLoadItem.nFullZipSize.toLong())
                        }
                    }

                else -> {
                    Timber.d("setOfflineItemCitySize cityDownLoadItem.taskState: ${cityDownLoadItem.taskState}")
                }
            }
        }
    }

    /**
     * 离线数据 列表item省份大小
     */
    @JvmStatic
    @BindingAdapter(value = ["setOfflineItemProvinceSize"], requireAll = false)
    fun setOfflineItemProvinceSize(textView: TextView, cityItemInfoList: ArrayList<CityItemInfo>) {
        var valueTotalSize: Long = 0
        for (cityItemInfo in cityItemInfoList) {
            val downLoadItem = MapDataController.getInstance()
                .getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, cityItemInfo.cityAdcode)
            if (downLoadItem != null) valueTotalSize += downLoadItem.nFullZipSize.toLong()
        }
        textView.text = CustomFileUtils.formetFileSize(valueTotalSize)
    }

    /**
     * 离线数据 列表item城市名称
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["setOfflineItemCityName", "setOfflineItemCurrentCityAdCode"],
        requireAll = false
    )
    fun setOfflineItemCityName(
        textView: TextView,
        cityItemInfo: CityItemInfo,
        currentCityAdCode: Int
    ) {
        if (cityItemInfo.cityAdcode == currentCityAdCode) {
            textView.text = "${cityItemInfo.cityName}（当前城市）"
        } else {
            textView.text = cityItemInfo.cityName
        }
    }

    /**
     * 离线数据 列表对应省份是否展开
     */
    @JvmStatic
    @BindingAdapter(value = ["setOfflineItemExpand"], requireAll = false)
    fun setOfflineItemExpand(view: View, isExpanded: Boolean) {
        if (isExpanded) {
            view.rotation = 180F
        } else {
            view.rotation = 0F
        }
    }

    /**
     * View 按钮设置背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setOfflineBtnBackground", "setOfflineBtnNight"], requireAll = false)
    fun setOfflineBtnBackground(view: View, state: Boolean, night: Boolean) {
        if (night) {
            view.setBackgroundResource(if (state) com.desaysv.psmap.model.R.color.primaryContainerNight else com.desaysv.psmap.model.R.color.primaryContainerPressNight)
        } else {
            view.setBackgroundResource(if (state) com.desaysv.psmap.model.R.color.primaryContainerDay else com.desaysv.psmap.model.R.color.primaryContainerPressDay)
        }
    }

    /**
     * 进阶动作图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setTurnIcon"], requireAll = false)
    fun setTurnIcon(imageView: ImageView, bitmap: Bitmap?) {
        if (bitmap != null) {
            imageView.visibility = View.VISIBLE
            imageView.setImageBitmap(bitmap)
        } else {
            imageView.visibility = View.INVISIBLE
        }
    }

    /**
     * 更新(接近)进阶动作图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setNearThumTurnIcon"], requireAll = false)
    fun setNearThumTurnIcon(imageView: ImageView, bitmap: Bitmap?) {
        if (bitmap != null) {
//            imageView.visibility = View.VISIBLE
            imageView.setImageBitmap(bitmap)
        } else {
//            imageView.visibility = View.INVISIBLE
        }
    }

    /**
     * 组队模块队名人数或者设置名称
     */
    @JvmStatic
    @BindingAdapter(value = ["groupTeamName", "groupTeamSetting", "groupJoinCall"], requireAll = false)
    fun groupTeamName(textview: TextView, number: String, setting: String, joinCall: String) {
        if (TextUtils.isEmpty(number) && TextUtils.isEmpty(joinCall)) {
            textview.text = setting
        } else if (TextUtils.equals(number, "0")) {
            textview.text = ResUtil.getString(R.string.sv_group_main_text_number_default)
        } else if (TextUtils.isEmpty(number)) {
            textview.text = ResUtil.getString(R.string.sv_group_main_text_join_call_number, joinCall)
        } else {
            textview.text = ResUtil.getString(R.string.sv_group_main_text_number, number)
        }
    }

    /**
     * 队友昵称设置
     */
    @JvmStatic
    @BindingAdapter(value = ["nickName", "remark"], requireAll = false)
    fun setNickName(textview: TextView, nickName: String?, remark: String?) {
        textview.text = when {
            !remark.isNullOrEmpty() -> remark
            !nickName.isNullOrEmpty() -> nickName
            else -> ""
        }
    }

    /**
     * TextView 组队设置队员昵称字体颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setTextNickColor"], requireAll = false)
    fun setTextNickColor(textView: TextView, onLine: Boolean) {
        val dayNight: Boolean = NightModeGlobal.isNightMode()
        if (onLine) {
            textView.setTextColor(
                if (dayNight) ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                ) else ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onPrimaryContainerDay
                )
            )
        } else {
            textView.setTextColor(
                if (dayNight) ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onSecondaryContainerNight
                ) else ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onSecondaryContainerDay
                )
            )
        }
    }

    /**
     *  屏蔽editText弹出工具条
     */
    @JvmStatic
    @BindingAdapter(value = ["setCustomSelectionActionModeCallback"], requireAll = false)
    fun setCustomSelectionActionModeCallback(textview: EditText, state: Boolean) {
        EditTextUtil.setCustomSelectionActionModeCallback(textview)
    }

    /**
     *  激活按钮样式设置
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setActivateManualBtn", "setActivateManualBtnNight"],
        requireAll = false
    )
    fun setActivateManualBtn(view: View, isOk: Boolean, isNightMode: Boolean) {
        if (isOk) {
            view.isEnabled = true
            view.alpha = 1.0f
        } else {
            view.isEnabled = false
            view.alpha = 0.4f
        }
    }

    /**
     *  激活按钮样式设置
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setMobileLoginBtn", "setMobileLoginNight"],
        requireAll = false
    )
    fun setMobileLoginBtn(view: View, isOk: Boolean, isNightMode: Boolean) {
        if (isOk) {
            view.setBackgroundResource(if (isNightMode) R.drawable.selector_bg_report_btn_night else R.drawable.selector_bg_report_btn_day)
        } else {
            view.setBackgroundResource(if (isNightMode) R.drawable.selector_bg_report_btn_unclick_night else R.drawable.selector_bg_report_btn_unclick_day)
        }
    }

    /**
     *  主辅路切换按钮
     */
    @JvmStatic
    @BindingAdapter(value = ["setParallelRoadImage", "setParallelRoadImageNight"], requireAll = false)
    fun setParallelRoadImage(imageView: ImageView, state: Int, isNightMode: Boolean) {
        when (state) {
            1 -> {
                // 显示 "切到辅路" 按钮
                imageView.setImageResource(if (isNightMode) R.drawable.navi_btn_parallelroad1_night else R.drawable.navi_btn_parallelroad1)
            }

            2 -> {
                // 显示 "切到主路" 按钮
                imageView.setImageResource(if (isNightMode) R.drawable.navi_btn_parallelroad2_night else R.drawable.navi_btn_parallelroad2)
            }

            else -> {
                Timber.d("setParallelRoadImage is gone")
            }
        }
    }

    /**
     *  桥上桥下切换按钮
     */
    @JvmStatic
    @BindingAdapter(value = ["setParallelBridgeImage", "setParallelBridgeImageNight"], requireAll = false)
    fun setParallelBridgeImage(imageView: ImageView, state: Int, isNightMode: Boolean) {
        when (state) {
            1 -> {
                // 显示 "切到桥下" 按钮，在线才显示
                imageView.setImageResource(if (isNightMode) R.drawable.navi_btn_parallelroad3_night else R.drawable.navi_btn_parallelroad3)
            }

            2 -> {
                // 显示 "切到桥上" 按钮，在线才显示
                imageView.setImageResource(if (isNightMode) R.drawable.navi_btn_parallelroad4_night else R.drawable.navi_btn_parallelroad4)
            }

            else -> {
                Timber.d("setParallelBridgeImage is gone")
            }
        }
    }


    /**
     * 区间平均速度
     */
    @JvmStatic
    @BindingAdapter(value = ["setAverageSpeed", "setLimitSpeed"], requireAll = false)
    fun setAverageSpeed(textview: TextView, averageSpeed: String?, limitSpeed: String?) {
        if (!averageSpeed.isNullOrEmpty() && !limitSpeed.isNullOrEmpty()) {
//            val originalString = "${averageSpeed}km/h"
//            val spannableString = SpannableString(originalString)
//            var color = textview.resources.getColor(
//                if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight
//                else com.desaysv.psmap.model.R.color.onPrimaryDay
//            ) // 变色的颜色
//            if (averageSpeed.toLong() > limitSpeed.toLong()) {
//                color = textview.resources.getColor(com.desaysv.psmap.model.R.color.onErrorDay)
//            }
//            spannableString.setSpan(
//                ForegroundColorSpan(color),
//                4,
//                originalString.lastIndexOf("km"),
//                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//            )
//            // 如果需要在 TextView 中显示，可以这样设置
//            textview.text = spannableString
            val averageSpeedString = "${averageSpeed}km/h"
            val spannableString = SpannableString(averageSpeedString)
            var color = textview.resources.getColor(
                if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight
                else com.desaysv.psmap.model.R.color.onPrimaryDay
            ) // 变色的颜色
            if (averageSpeed.toLong() > limitSpeed.toLong()) {
                color = textview.resources.getColor(com.desaysv.psmap.model.R.color.onErrorDay)
            }
            val start = 0 // "80" 的起始位置
            val end = averageSpeedString.lastIndexOf("km")   // "80" 的结束位置
            spannableString.setSpan(
                AbsoluteSizeSpan(textview.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_56)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 设置加粗
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD), // 加粗样式
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 设置文本颜色
            spannableString.setSpan(
                ForegroundColorSpan(color), // 设置颜色为红色
                0,
                averageSpeedString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textview.text = spannableString
        }
    }

    /**
     * 区间剩余距离
     */
    @JvmStatic
    @BindingAdapter(value = ["setRemainDist"], requireAll = false)
    fun setRemainDist(textview: TextView, remainDist: String?) {
        if (!remainDist.isNullOrEmpty()) {
            val remainDistString =
                NavigationUtil.meterToStrEnglish(
                    SdkApplicationUtils.getApplication(),
                    remainDist.toLong()
                )
            val spannableString = SpannableString(remainDistString)
            val start = 0 // "80" 的起始位置
            val end =
                remainDistString.lastIndexOf(if (remainDist.toLong() >= 1000) "km" else "m")   // "80" 的结束位置
            spannableString.setSpan(
                AbsoluteSizeSpan(textview.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_56)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 设置加粗
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD), // 加粗样式
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textview.text = spannableString
        }
    }

    /**
     *  超速背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setSpeedingAverageSpeed", "setSpeedingLimitSpeed"], requireAll = false)
    fun setSpeedingImage(imageView: ImageView, averageSpeed: String?, limitSpeed: String?) {
        if (averageSpeed.isNullOrEmpty() || limitSpeed.isNullOrEmpty()) {
            imageView.visibility = View.GONE
        } else if (averageSpeed.toLong() > limitSpeed.toLong()) {
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.GONE
        }
    }

    /**
     * 登录账号协议样式
     */
    @JvmStatic
    @BindingAdapter(value = ["accountBindTextStyle"], requireAll = false)
    fun accountBindTextStyle(textview: TextView, textResource: Int) {
        val style = SpannableStringBuilder(textview.resources.getString(textResource))
        style.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    textview.context,
                    com.desaysv.psmap.model.R.color.primaryContainerNight
                )
            ),
            7,
            17,
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
        textview.text = style
    }

    /**
     * 手机号码登录，验证码输入框状态
     */
    @JvmStatic
    @BindingAdapter(value = ["verificationEditTextState"], requireAll = false)
    fun verificationEditTextState(verification: EditText, number: String) {
        if (TextUtils.isEmpty(number))
            return
        val phone = CommonUtils.phoneNoSpace(number)
        if (phone.length == BaseConstant.PHONE_NUMBER_LEN) {
            verification.setEnabled(true)
            verification.requestFocus()
            verification.setFocusableInTouchMode(true)
        } else {
            verification.setEnabled(false)
        }
    }

    /**
     * editText设置 setSelection
     */
    @JvmStatic
    @BindingAdapter(value = ["editTextSetSelection"], requireAll = false)
    fun editTextSetSelection(editText: EditText, text: String) {
        if (!TextUtils.isEmpty(text) && editText.text.isNotEmpty()) {
            editText.setSelection(text.length.coerceAtMost(editText.text.length))
        }
    }

    /**
     * 当前车速
     */
    @JvmStatic
    @BindingAdapter(value = ["setCurrentSpeed"], requireAll = false)
    fun setCurrentSpeed(textview: TextView, currentSpeed: String?) {
        if (!currentSpeed.isNullOrEmpty()) {
            val currentSpeedString = "$currentSpeed\nkm/h"
            val spannableString = SpannableString(currentSpeedString)
            val start = 0 // "80" 的起始位置
            val end = currentSpeedString.lastIndexOf("km")   // "80" 的结束位置
            spannableString.setSpan(
                AbsoluteSizeSpan(textview.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_28)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 设置加粗
            spannableString.setSpan(
                StyleSpan(Typeface.BOLD), // 加粗样式
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            textview.text = spannableString
        }
    }

    /**
     * 区间平均速度
     */
    @JvmStatic
    @BindingAdapter(value = ["setTrafficEventCardTimeText"], requireAll = false)
    fun setTrafficEventCardTimeText(textview: TextView, text: String?) {
        if (!text.isNullOrEmpty()) {
            textview.visibility = View.VISIBLE
            val color = textview.context.getColor(
                if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight
                else com.desaysv.psmap.model.R.color.onPrimaryDay
            )

            val spannableText = SpannableString(text)

            if (text.contains("具体时间：")) {
                val a0 = text.indexOf("具体时间：")
                var b0 = text.indexOf("\n")
                b0 = if (b0 >= 0) b0 else text.length
                if (a0 >= 0 && b0 > a0) {
                    spannableText.setSpan(
                        ForegroundColorSpan(color),
                        a0 + 5,
                        b0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
//                    spannableText.setSpan(
//                        AbsoluteSizeSpan(
//                            textview.resources.getDimensionPixelSize(
//                                com.desaysv.psmap
//                                    .base.R.dimen.sv_dimen_24
//                            ), true
//                        ), a0 + 5, b0, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                    spannableText.setSpan(
//                        StyleSpan(Typeface.NORMAL),
//                        a0 + 5,
//                        b0,
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
                }
            }
            if (text.contains("开始时间：")) {
                val a0 = text.indexOf("开始时间：")
                var b0 = text.indexOf("\n", a0)
                b0 = if (b0 >= 0) b0 else text.length
                if (a0 >= 0 && b0 > a0) {
                    spannableText.setSpan(
                        ForegroundColorSpan(color),
                        a0 + 5,
                        b0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
//                    spannableText.setSpan(
//                        AbsoluteSizeSpan(
//                            textview.resources.getDimensionPixelSize(
//                                com.desaysv.psmap
//                                    .base.R.dimen.sv_dimen_24
//                            ), true
//                        ), a0 + 5, b0, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                    spannableText.setSpan(
//                        StyleSpan(Typeface.NORMAL),
//                        a0 + 5,
//                        b0,
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
                }
            }

            if (text.contains("结束时间：")) {
                val a0 = text.indexOf("结束时间：")
                if (a0 >= 0) {
                    spannableText.setSpan(
                        ForegroundColorSpan(color),
                        a0 + 5,
                        text.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
//                    spannableText.setSpan(
//                        AbsoluteSizeSpan(
//                            textview.resources.getDimensionPixelSize(
//                                com.desaysv.psmap
//                                    .base.R.dimen.sv_dimen_24
//                            ), true
//                        ), a0 + 5, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
//                    spannableText.setSpan(
//                        StyleSpan(Typeface.NORMAL),
//                        a0 + 5,
//                        text.length,
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                    )
                }
            }
            textview.text = spannableText
        } else {
            textview.visibility = View.GONE
        }
    }

    /**
     * 错误上报页面title
     */
    @JvmStatic
    @BindingAdapter(value = ["errorReportTitle"], requireAll = false)
    fun errorReportTitle(textView: TextView, type: Int) {
        when (type) {
            BaseConstant.TYPE_PAGE_ISSUE -> textView.text =
                textView.resources.getString(R.string.sv_setting_issue_feedback)

            BaseConstant.TYPE_PAGE_ISSUE_POS -> textView.text =
                textView.resources.getString(R.string.sv_setting_issue_feedback_pos_title)

            BaseConstant.TYPE_PAGE_ISSUE_INTERNET -> textView.text =
                textView.resources.getString(R.string.sv_setting_issue_feedback_internet_title)

            BaseConstant.TYPE_PAGE_ISSUE_DATA_DOWNLOAD -> textView.text =
                textView.resources.getString(R.string.sv_setting_issue_feedback_data_download_title)

            BaseConstant.TYPE_PAGE_ISSUE_BROADCAST -> textView.text =
                textView.resources.getString(R.string.sv_setting_issue_feedback_broadcast_title)

            BaseConstant.TYPE_PAGE_ISSUE_OTHER -> textView.text =
                textView.resources.getString(R.string.sv_setting_issue_feedback_other_title)

            BaseConstant.TYPE_PAGE_ISSUE_PHONE, BaseConstant.TYPE_PAGE_ISSUE_EDIT_DEC -> textView.text =
                ""

            else -> {}
        }
    }


    /**
     * 错误上报--描述框高度
     */
    @JvmStatic
    @BindingAdapter(value = ["errorReportInputHeight"], requireAll = false)
    fun errorReportInputHeight(view: View, type: Int) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.height =
            if (BaseConstant.TYPE_PAGE_ISSUE_OTHER == type) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_265) else view.resources.getDimensionPixelSize(
                com.desaysv.psmap.base.R.dimen.sv_dimen_353
            )
        view.layoutParams = params
    }

    /**
     * 错误上报类别头像
     */
    @JvmStatic
    @BindingAdapter(value = ["errorReportTypeImage"], requireAll = false)
    fun errorReportTypeImage(imageView: ImageView, type: Int) {
        when (type) {
            BaseConstant.TYPE_ISSUE_POS -> imageView.setBackgroundResource((if (NightModeGlobal.isNightMode()) R.drawable.ic_issue_pos_night else R.drawable.ic_issue_pos_day))
            BaseConstant.TYPE_ISSUE_INTERNET -> imageView.setBackgroundResource((if (NightModeGlobal.isNightMode()) R.drawable.ic_issue_internet_night else R.drawable.ic_issue_internet_day))
            BaseConstant.TYPE_ISSUE_DATA_DOWNLOAD -> imageView.setBackgroundResource((if (NightModeGlobal.isNightMode()) R.drawable.ic_issue_data_download_night else R.drawable.ic_issue_data_download_day))
            BaseConstant.TYPE_ISSUE_BROADCAST -> imageView.setBackgroundResource((if (NightModeGlobal.isNightMode()) R.drawable.ic_issue_broadcast_night else R.drawable.ic_issue_broadcast_day))
            BaseConstant.TYPE_ISSUE_OTHER -> imageView.setBackgroundResource((if (NightModeGlobal.isNightMode()) R.drawable.ic_issue_other_night else R.drawable.ic_issue_other_day))
            else -> {}
        }
    }

    /**
     * 限行按钮
     */
    @JvmStatic
    @BindingAdapter(value = ["btSetUpTip"], requireAll = false)
    fun btSetUpTip(textView: TextView, type: Int) {
        when (type) {
            0 -> textView.text =
                textView.resources.getString(R.string.sv_route_navi_restrict_set_up)

            1 -> textView.text = textView.resources.getString(R.string.sv_route_navi_restrict_open)
            else -> textView.text =
                textView.resources.getString(R.string.sv_route_navi_restrict_view)
        }
    }

    /**
     * 高德协议二维码
     */
    @JvmStatic
    @BindingAdapter(value = ["agreementImage"], requireAll = false)
    fun agreementImage(view: View, type: Int) {
        val dp300 =
            view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_300)
        view.background =
            BitmapDrawable(
                view.resources,
                QrUtils.create2DCode(
                    if (type == 0) BaseConstant.TERMS_LINK else if (type == 1) BaseConstant.POLICY_LINK else BaseConstant.ACCOUNT_LINK,
                    dp300,
                    dp300,
                    0
                )
            )
    }

    /**
     *  路线偏好按钮样式设置
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setStrategyBtn", "setStrategyType", "setStrategyEnabled", "setStrategyBtnNight"],
        requireAll = false
    )
    fun setStrategyBtn(view: ConstraintLayout, isSelect: Boolean, type: Int, isEnabled: Boolean, isNightMode: Boolean) {
        val backgroundResource = when (type) {
            1 -> if (isSelect) {
                if (isNightMode) R.drawable.selector_route_remommend_press_night else R.drawable.selector_route_remommend_press_day
            } else {
                if (isNightMode) R.drawable.selector_route_remommend_night else R.drawable.selector_route_remommend_day
            }

            2 -> if (isSelect) {
                if (isNightMode) R.drawable.selector_route_strategy_other_bg_press_night else R.drawable.selector_route_strategy_other_bg_press_day
            } else {
                if (isNightMode) R.drawable.selector_route_strategy_other_bg_night else R.drawable.selector_route_strategy_other_bg_day
            }

            else -> {
                throw IllegalArgumentException("Invalid type: $type")
            }
        }

        view.setBackgroundResource(backgroundResource)
        view.isEnabled = isEnabled
    }

    /**
     *  路线偏好Icon样式设置
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setStrategyIcon", "setStrategyIconType", "setStrategyIconNight"],
        requireAll = false
    )
    fun setStrategyIcon(view: View, isSelect: Boolean, type: Int, isNightMode: Boolean) {
        val backgroundResource = when (type) {
            //高德推荐
            1 -> if (isNightMode) R.drawable.selector_route_remommend_icon_night else
                if (isSelect) R.drawable.selector_route_remommend_icon_select_day else R.drawable.selector_route_remommend_icon_day
            //避免拥堵
            2 -> if (isNightMode) R.drawable.selector_route_avoid_congestion_night else
                if (isSelect) R.drawable.selector_route_avoid_congestion_select_day else R.drawable.selector_route_avoid_congestion_day
            //高速优先
            3 -> if (isNightMode) R.drawable.selector_route_high_speed_night else
                if (isSelect) R.drawable.selector_route_high_speed_select_day else R.drawable.selector_route_high_speed_day
            //不走高速
            4 -> if (isNightMode) R.drawable.selector_route_nohigh_speed_night else
                if (isSelect) R.drawable.selector_route_nohigh_speed_select_day else R.drawable.selector_route_nohigh_speed_day
            //少收费
            5 -> if (isNightMode) R.drawable.selector_route_avoid_charging_night else
                if (isSelect) R.drawable.selector_route_avoid_charging_select_day else R.drawable.selector_route_avoid_charging_day
            //大路优先
            6 -> if (isNightMode) R.drawable.selector_route_width_first_night else
                if (isSelect) R.drawable.selector_route_width_first_select_day else R.drawable.selector_route_width_first_day
            //速度最快
            7 -> if (isNightMode) R.drawable.selector_route_speed_first_night else
                if (isSelect) R.drawable.selector_route_speed_first_select_day else R.drawable.selector_route_speed_first_day


            else -> throw IllegalArgumentException("Invalid type: $type")
        }

        view.setBackgroundResource(backgroundResource)
    }

    /**
     *  路线偏好Text颜色设置
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setStrategyText", "setStrategyTextNight"],
        requireAll = false
    )
    fun setStrategyText(view: TextView, isSelect: Boolean, isNightMode: Boolean) {
        val textColor =
            if (isSelect) {
                if (isNightMode) R.color.color_route_strategy_night else R.color.color_route_strategy_select_day
            } else {
                if (isNightMode) R.color.color_route_strategy_night else R.color.color_route_strategy_day
            }

        view.setTextColor(ContextCompat.getColorStateList(view.context, textColor))
    }

    /**
     * 搜索记录按钮宽度修改
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchHistoryType", "setSearchHistoryisFavorite"], requireAll = false)
    fun setSearchHistoryType(linearLayout: LinearLayout, twoView: Boolean, isFavorite: Boolean) {
        val width: Int =
            if (twoView)
                if (isFavorite)
                    linearLayout.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_340)
                else
                    linearLayout.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_280)
            else
                linearLayout.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_140)

        val layoutParams = LinearLayout.LayoutParams(
            width,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        linearLayout.layoutParams = layoutParams
    }

    /**
     * 收藏按钮宽度修改
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchFavoriteWidth"], requireAll = false)
    fun setSearchFavoriteWidth(textView: TextView, isFavorite: Boolean) {
        val width: Int =
            if (isFavorite)
                textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_200)
            else
                textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_140)

        val layoutParams = textView.layoutParams
        layoutParams.width = width
        textView.layoutParams = layoutParams
    }

    /**
     * 导航语音包数据头像
     */
    @JvmStatic
    @BindingAdapter(value = ["voiceImage", "voiceImageTopMargin"], requireAll = false)
    fun voiceImage(imageView: ImageView, imageFilePath: String, showTitle: Boolean) {
        if (TextUtils.isEmpty(imageFilePath)) {
            imageView.setImageResource(R.drawable.ic_default_woman_head)
        } else {
            imageView.setImageBitmap(BitmapFactory.decodeFile(imageFilePath))
        }

        val params = imageView.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin =
            if (showTitle) imageView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_20) else imageView.resources.getDimensionPixelSize(
                com.desaysv.psmap.base.R.dimen.sv_dimen_28
            )
        imageView.layoutParams = params
    }

    /**
     * 导航语音包数据大小或者显示默认
     */
    @JvmStatic
    @BindingAdapter(value = ["voiceSizeDefault"], requireAll = false)
    fun voiceSizeDefault(textView: TextView, voice: Voice) {
        if (voice.id == -1) {
            textView.text = textView.resources.getString(R.string.sv_setting_system_default_voice)
        } else {
            textView.text = CustomFileUtils.formetFileSize(voice.zipDataSize.toLong())
        }
    }

    /**
     * 导航语音包数据大小下载百分比
     */
    @JvmStatic
    @BindingAdapter(value = ["voicePercent"], requireAll = false)
    fun voicePercent(progress: SkinProgressBar, voice: Voice) {
        if (voice.id == -1) {
            progress.progress = 0
        } else {
            if (voice.taskState == TASK_STATUS_CODE_SUCCESS) {
                progress.progress = 0
            } else {
                progress.progress = voice.percent.toInt()
            }
            when (voice.taskState) {
                TASK_STATUS_CODE_ERR, TASK_STATUS_CODE_MAX -> {
                    progress.progress = 0
                }

                TASK_STATUS_CODE_READY -> {
                    progress.progress = 0
                }

                TASK_STATUS_CODE_WAITING -> {
                    progress.progress = voice.percent.toInt()
                }

                TASK_STATUS_CODE_DOING -> {
                    progress.progress = voice.percent.toInt()
                }

                TASK_STATUS_CODE_PAUSE -> {
                    val percent = voice.percent.toInt()
                    if (percent > 0) {
                        progress.progress = percent
                    } else {
                        progress.progress = 0
                    }
                }

                TASK_STATUS_CODE_CHECKING -> {
                    progress.progress = voice.percent.toInt()
                }

                TASK_STATUS_CODE_UNZIPPING -> {
                    progress.progress = voice.percent.toInt()
                }

                TASK_STATUS_CODE_UNZIPPED -> {
                    progress.progress = 0
                }

                TASK_STATUS_CODE_SUCCESS -> {
                    progress.progress = 0
                }

                else -> {
                    Timber.d("setOfflineCityProgressTxt cityDownLoadItem.taskState: ${voice.taskState}")
                }
            }
        }
    }

    /**
     * 导航语音包数据下载状态文字描述
     */
    @JvmStatic
    @BindingAdapter(value = ["voiceDownloadText", "voiceDownloadTextIsNight"], requireAll = false)
    fun voiceDownloadText(textView: TextView, voice: Voice, isNight: Boolean) {
        if (voice.id == -1) {
            textView.text = "使用" // 状态是下载完成，显示icon+使用
            textView.setTextColor(
                if (isNight) ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onPrimaryNight
                ) else ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onPrimaryDay
                )
            )
        } else {
            textView.text = when (voice.taskState) {
                TASK_STATUS_CODE_READY -> {
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )

                    if (voice.isUpdate) {
                        if (voice.percent.toInt() <= 0) {
                            "下载" //下载，显示更新
                        } else {
                            "更新" // 更新，显示下载
                        }
                    } else {
                        "下载" // 下载，显示下载
                    }
                }

                TASK_STATUS_CODE_WAITING -> {
                    val percent = voice.percent.toInt()
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    "等待"
                } // 等待中，显示等待

                TASK_STATUS_CODE_PAUSE -> {
                    val percent = voice.percent.toInt()
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    "继续"
                } //状态是暂停，显示icon+继续

                TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_DONE -> {
                    val percent = voice.percent.toInt()
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    voice.percent.toInt().toString() + "%"
                } //状态是下载中,更新中,显示icon+下载百分比

                TASK_STATUS_CODE_CHECKING -> {
                    val percent = voice.percent.toInt()
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    "校验"
                } // 校验中，显示校验

                TASK_STATUS_CODE_CHECKED -> {
                    val percent = voice.percent.toInt()
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    "校验完成"
                } // 校验完成，显示校验完成

                TASK_STATUS_CODE_UNZIPPING -> {
                    val percent = voice.percent.toInt()
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            if (percent >= 44) {
                                com.desaysv.psmap.model.R.color.onPrimaryContainerNight
                            } else {
                                com.desaysv.psmap.model.R.color.onPrimaryNight
                            }
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    voice.percent.toInt().toString() + "%"
                } // 状态是解压中,显示icon+下载百分比

                TASK_STATUS_CODE_UNZIPPED -> {
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    "解压完成"
                } // 解压完成，显示解压完成

                TASK_STATUS_CODE_SUCCESS -> {
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    "使用"
                } // 状态是下载完成，显示icon+使用

                TASK_STATUS_CODE_ERR, TASK_STATUS_CODE_MAX -> {
                    Timber.i("voiceDownloadText taskState:${voice.taskState}")
                    textView.setTextColor(
                        if (isNight) ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryNight
                        ) else ContextCompat.getColor(
                            textView.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                    )
                    "继续"
                } //状态是下载失败,显示icon+重试

                else -> ""
            }
        }
    }

    /**
     * 导航语音包数据下载状态ICON
     */
    @JvmStatic
    @BindingAdapter(value = ["voiceDownloadIcon", "voiceDownloadIconVisi"], requireAll = false)
    fun voiceDownloadIcon(imageView: ImageView, voice: Voice, isNoUseVoice: Boolean) {
        val iconState = if (voice.id == -1) {
            0
        } else {
            when (voice.taskState) { //-1：没有图标， 0：下载图标 1：暂停图标
                TASK_STATUS_CODE_READY -> if (voice.isUpdate) {
                    -1 //更新，显示更新
                } else {
                    -1 // 下载，显示下载
                }

                TASK_STATUS_CODE_WAITING -> -1 // 等待中，显示等待
                TASK_STATUS_CODE_PAUSE -> 0 //状态是暂停，显示icon+继续
                TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_DONE -> {
                    1 //状态是下载中,更新中,显示icon+下载百分比
                }

                TASK_STATUS_CODE_CHECKING -> -1 // 校验中，显示校验
                TASK_STATUS_CODE_CHECKED -> -1 // 校验完成，显示校验完成
                TASK_STATUS_CODE_UNZIPPING -> 1 // 状态是解压中,显示icon+下载百分比
                TASK_STATUS_CODE_UNZIPPED -> -1 // 解压完成，显示解压完成
                TASK_STATUS_CODE_SUCCESS -> 0 // 状态是下载完成，显示icon+使用
                TASK_STATUS_CODE_ERR, TASK_STATUS_CODE_MAX -> 0 //状态是下载失败,显示icon+重试
                else -> {
                    -1
                }
            }
        }
        imageView.visibility =
            if (iconState == -1) View.GONE else if (isNoUseVoice) View.VISIBLE else View.GONE
        imageView.setImageResource(if (iconState == 0) R.drawable.selector_ic_map_data_download_night else R.drawable.selector_ic_map_data_pause_night)
    }

    /**
     *  模拟导航速度选择按钮样式设置
     */
    @JvmStatic
    @BindingAdapter(value = ["setSpeedBtn", "setSpeedBtnNight"], requireAll = false)
    fun setSpeedBtn(view: SkinTextView, type: Int, isNightMode: Boolean) {
        val backgroundResource = when (type) {
            0 -> if (isNightMode) R.drawable.selector_ic_route_navi_speed_180_night else R.drawable.selector_ic_route_navi_speed_180_day

            1 -> if (isNightMode) R.drawable.selector_ic_route_navi_speed_480_night else R.drawable.selector_ic_route_navi_speed_480_day

            2 -> if (isNightMode) R.drawable.selector_ic_route_navi_speed_680_night else R.drawable.selector_ic_route_navi_speed_680_day

            else -> {
                if (isNightMode) R.drawable.selector_ic_route_navi_speed_180_night else R.drawable.selector_ic_route_navi_speed_180_day
            }
        }
        view.setBackgroundResource(backgroundResource)
    }

    /**
     * 个人中心首页未登录--我的行程提示
     */
    @JvmStatic
    @BindingAdapter(value = ["setAccountMainTripTip"], requireAll = false)
    fun setAccountMainTripTip(textView: TextView, isNight: Boolean) {
        val color =
            textView.context.getColor(
                if (isNight) com.desaysv.psmap.model.R.color.onTertiaryContainerNight else com.desaysv.psmap.model.R.color.customColorRbSelectedBgDay
            )
        val text = textView.resources.getString(R.string.sv_setting_to_see_trip)
        val style = SpannableStringBuilder(text)
        // 确保索引在字符串长度范围内
        val start = 8
        val end = 13
        if (text.length >= end) {
            style.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
        }
        textView.text = style
    }

    /**
     * 个人中心&设置底部边距
     */
    @JvmStatic
    @BindingAdapter(value = ["accountSettingLayout"], requireAll = false)
    fun accountSettingLayout(view: View, isMapSetting: Boolean) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.bottomMargin =
            if (isMapSetting) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_8) else view.resources.getDimensionPixelSize(
                com.desaysv.psmap.base.R.dimen.sv_dimen_8
            )
        view.layoutParams = params
    }

    /**
     * 控件设置alpha
     */
    @JvmStatic
    @BindingAdapter(value = ["setViewAlpha"], requireAll = false)
    fun setViewAlpha(view: View, enabled: Boolean) {
        view.alpha = if (enabled) 1.0f else 0.5f
    }

    /**
     * 控件设置alpha
     */
    @JvmStatic
    @BindingAdapter(value = ["setViewAlpha4"], requireAll = false)
    fun setViewAlpha4(view: View, enabled: Boolean) {
        view.alpha = if (enabled) 1.0f else 0.4f
    }

    /**
     * 手机登录协议设置文本颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setMobileLoginServicePolicy"], requireAll = false)
    fun setMobileLoginServicePolicy(textView: TextView, isNight: Boolean) {
        val fullText = textView.resources.getString(R.string.sv_setting_agree_policy)
        val color =
            if (isNight) textView.resources.getColor(com.desaysv.psmap.model.R.color.onTertiaryContainerNight) else textView.resources.getColor(
                com.desaysv.psmap.model.R.color.customColorRbSelectedBgDay
            )

        // 创建 SpannableString
        val spannableString = SpannableString(fullText)

        // 设置《高德服务条款》的颜色为白色
        val service = textView.resources.getString(R.string.sv_agreement_terms_service)
        val start1 = fullText.indexOf(service)
        val end1 = start1 + service.length

        spannableString.setSpan(
            ForegroundColorSpan(color),
            start1,
            end1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 设置《高德隐私权政策》的颜色为白色
        val policy = textView.resources.getString(R.string.sv_agreement_privacy_policy)
        val start2 = fullText.indexOf(policy)
        val end2 = start2 + policy.length
        spannableString.setSpan(
            ForegroundColorSpan(color),
            start2,
            end2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 设置《高德账号服务个人信息处理规则》的颜色为白色
        val account = textView.resources.getString(R.string.sv_agreement_account)
        val start3 = fullText.indexOf(account)
        val end3 = start3 + account.length
        spannableString.setSpan(
            ForegroundColorSpan(color),
            start3,
            end3,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // 设置 TextView 的文本
        textView.text = spannableString
    }

    /**
     * 设置个人中心-手车互联入口图标状态
     */
    @JvmStatic
    @BindingAdapter(
        value = ["setMobileIconNight", "setMobileIconNetwork", "setMobileIconLogin"],
        requireAll = false
    )
    fun setMobileIcon(
        view: TextView,
        isNight: Boolean,
        isNetworkConnected: Boolean,
        loginState: Boolean
    ) {
        val size = view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_49)
        val drawable = if (isNight) {
            if (!isNetworkConnected) {
                ContextCompat.getDrawable(
                    view.context,
                    R.drawable.selector_ic_setting_connect_fail_night
                )
            } else if (loginState) {
                ContextCompat.getDrawable(
                    view.context,
                    R.drawable.selector_ic_setting_connect_night
                )
            } else {
                ContextCompat.getDrawable(
                    view.context,
                    R.drawable.selector_ic_setting_un_connect_night
                )
            }
        } else {
            if (!isNetworkConnected) {
                ContextCompat.getDrawable(
                    view.context,
                    R.drawable.selector_ic_setting_connect_fail_night
                )
            } else if (loginState) {
                ContextCompat.getDrawable(view.context, R.drawable.selector_ic_setting_connect_day)
            } else {
                ContextCompat.getDrawable(
                    view.context,
                    R.drawable.selector_ic_setting_un_connect_day
                )
            }
        }
        drawable?.setBounds(0, 0, size, size)
        view.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
    }

    /**
     * 收藏夹家和公司icon设置
     */
    @JvmStatic
    @BindingAdapter(value = ["setCompanyHomeFavorite"], requireAll = false)
    fun setCompanyHomeFavorite(imageView: ImageView, type: Int) {
        when (type) {
            FavoriteType.FavoriteTypeHome -> {
                imageView.setImageResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_favorite_home_night else R.drawable.ic_favorite_home_day)
            }

            FavoriteType.FavoriteTypeCompany -> {
                imageView.setImageResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_favorite_company_night else R.drawable.ic_favorite_company_day)
            }
        }
    }

    /**
     * 收藏夹家和公司名称设置
     */
    @JvmStatic
    @BindingAdapter(value = ["setCompanyHomeFavoriteTitle"], requireAll = false)
    fun setCompanyHomeFavoriteTitle(textView: TextView, type: Int) {
        when (type) {
            FavoriteType.FavoriteTypeHome -> {
                textView.text =
                    textView.resources.getString(com.desaysv.psmap.base.R.string.sv_common_home)
            }

            FavoriteType.FavoriteTypeCompany -> {
                textView.text =
                    textView.resources.getString(com.desaysv.psmap.base.R.string.sv_common_company)
            }
        }
    }

    /**
     * 收藏夹收藏点地址
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(value = ["setFavoriteAddress"], requireAll = false)
    fun setFavoriteAddress(textView: TextView, simpleFavoriteItem: SimpleFavoriteItem) {
        val favoriteBaseItem = FavoriteBaseItem()
        favoriteBaseItem.item_id = simpleFavoriteItem.item_id
        val favoriteItem = BehaviorController.getInstance().getFavorite(favoriteBaseItem) ?: null
        if (TextUtils.isEmpty(simpleFavoriteItem.address)) {
            if (favoriteItem == null || TextUtils.isEmpty(favoriteItem.address)) {
                textView.text = simpleFavoriteItem.name + "附近"
            } else {
                textView.text = favoriteItem.address
            }
        } else if (TextUtils.equals(simpleFavoriteItem.address, "附近")) {
            if (favoriteItem == null || TextUtils.isEmpty(favoriteItem.address)) {
                textView.text = simpleFavoriteItem.name + "附近"
            } else {
                textView.text = favoriteItem.address
            }
        } else {
            textView.text = simpleFavoriteItem.address
        }
    }

    /**
     *  停车场P1 背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setP1", "setP1NightMode"], requireAll = false)
    fun setP1(view: TextView, isSelect: Boolean, isNight: Boolean) {
        val backgroundResource = if (isSelect) {
            if (isNight) R.drawable.selector_btn_confirm_night else R.drawable.selector_btn_confirm_day
        } else {
            if (isNight) R.drawable.selector_bg_confirm_night else R.drawable.selector_bg_confirm_day
        }
        val colorResource = if (isSelect) {
            if (isNight)
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerNight)
            else
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerDay)
        } else {
            if (isNight)
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.customColorWhite80Night)
            else
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.customColorRbBgDay)
        }
        view.setBackgroundResource(backgroundResource)
        view.setTextColor(colorResource)
    }

    /**
     *  停车场P2 背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setP2", "setP2NightMode"], requireAll = false)
    fun setP2(view: TextView, isSelect: Boolean, isNight: Boolean) {
        val backgroundResource = if (isSelect) {
            if (isNight) R.drawable.selector_btn_confirm_night else R.drawable.selector_btn_confirm_day
        } else {
            if (isNight) R.drawable.selector_bg_confirm_night else R.drawable.selector_bg_confirm_day
        }
        val colorResource = if (isSelect) {
            if (isNight)
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerNight)
            else
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerDay)
        } else {
            if (isNight)
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.customColorWhite80Night)
            else
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.customColorRbBgDay)
        }
        view.setBackgroundResource(backgroundResource)
        view.setTextColor(colorResource)
    }

    /**
     *  停车场P3 背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setP3", "setP3NightMode"], requireAll = false)
    fun setP3(view: TextView, isSelect: Boolean, isNight: Boolean) {
        val backgroundResource = if (isSelect) {
            if (isNight) R.drawable.selector_btn_confirm_night else R.drawable.selector_btn_confirm_day
        } else {
            if (isNight) R.drawable.selector_bg_confirm_night else R.drawable.selector_bg_confirm_day
        }
        val colorResource = if (isSelect) {
            if (isNight)
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerNight)
            else
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryContainerDay)
        } else {
            if (isNight)
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.customColorWhite80Night)
            else
                SdkApplicationUtils.getApplication().resources.getColor(com.desaysv.psmap.model.R.color.customColorRbBgDay)
        }
        view.setBackgroundResource(backgroundResource)
        view.setTextColor(colorResource)
    }

    /**
     * 最后一公里弹条宽度
     */
    @JvmStatic
    @BindingAdapter(value = ["sendToPhoneWidth"], requireAll = false)
    fun sendToPhoneWidth(view: View, screenStatus: Boolean) { //true 为2/3屛， false 为全屏
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.width =
            if (screenStatus) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_652) else view.resources.getDimensionPixelSize(
                com.desaysv.psmap.base.R.dimen.sv_dimen_950
            )
        view.layoutParams = params
    }

    /**
     * 或者离线数据下载数量
     */
    @JvmStatic
    @BindingAdapter(value = ["setOfflineDaraDownloadingNum", "setOfflineDaraDownloadingNumNight"], requireAll = false)
    fun setOfflineDaraDownloadingNum(textView: TextView, num: Int, isNight: Boolean) {
        if (num == 0) {
            textView.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            textView.text = "$num"
            if (num < 10) {
                textView.setBackgroundResource(if (isNight) R.drawable.selector_bg_map_data_num_night else R.drawable.selector_bg_map_data_num_day)
            } else {
                textView.setBackgroundResource(if (isNight) R.drawable.selector_bg_map_data_num_large_night else R.drawable.selector_bg_map_data_num_large_day)
            }
        }
    }

    /**
     * 设置背景阴影颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setShadowColor"], requireAll = false)
    fun setShadowColor(shadowLayout: ShadowLayout, isNight: Boolean) {
        shadowLayout.setShadowColor(
            if (isNight) shadowLayout.resources.getColor(com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight) else shadowLayout.resources.getColor(
                com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay
            )
        )
    }


    /**
     * 设置背景阴影颜色--白天有阴影
     */
    @JvmStatic
    @BindingAdapter(value = ["setHasShadowColor"], requireAll = false)
    fun setHasShadowColor(shadowLayout: ShadowLayout, isNight: Boolean) {
        shadowLayout.setShadowColor(
            if (isNight) shadowLayout.resources.getColor(com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight) else shadowLayout.resources.getColor(
                com.desaysv.psmap.model.R.color.customColorShadowColorDay
            )
        )
    }

    /**
     * 设置背景阴影颜色--黑夜有阴影
     */
    @JvmStatic
    @BindingAdapter(value = ["setShadowNightColor"], requireAll = false)
    fun setShadowNightColor(shadowLayout: ShadowLayout, isNight: Boolean) {
        shadowLayout.setShadowColor(
            if (isNight) shadowLayout.resources.getColor(com.desaysv.psmap.model.R.color.customColorBlack52ShadowNight) else shadowLayout.resources.getColor(
                com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay
            )
        )
    }

    /**
     * 设置右上角背景阴影颜色
     */
    @JvmStatic
    @BindingAdapter(value = ["setLongCardShadowColor"], requireAll = false)
    fun setLongCardShadowColor(shadowLayout: ShadowLayout, isNight: Boolean) {
        shadowLayout.setShadowColor(
            if (isNight) shadowLayout.resources.getColor(com.desaysv.psmap.model.R.color.customColorWXUnBindShadowColorNight) else shadowLayout.resources.getColor(
                com.desaysv.psmap.model.R.color.customColorWXUnBindShadowColorDay
            )
        )
    }

    /**
     * 设置CustomDialog高度
     */
    @JvmStatic
    @BindingAdapter(value = ["customDialogHeight"], requireAll = false)
    fun customDialogHeight(view: View, isBig: Boolean) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.height =
            if (isBig) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_421) else
                view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_415)
        view.layoutParams = params
    }

    /**
     * editText设置光标
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    @JvmStatic
    @BindingAdapter(value = ["setTextCursorDrawable"], requireAll = false)
    fun setTextCursorDrawable(editText: EditText, isNight: Boolean) {
        editText.textCursorDrawable =
            ContextCompat.getDrawable(
                editText.context,
                if (isNight) R.drawable.shape_bg_cursor_night else R.drawable.shape_bg_cursor_day
            )
    }

    /**
     * 全局切屏按钮
     */
    @SuppressLint("ResourceType")
    @JvmStatic
    @BindingAdapter(
        value = ["setScreenExtendNight", "setScreenExtendActivateAgreement"],
        requireAll = false
    )
    fun setScreenExtend(view: View, isNight: Boolean, showActivateAgreement: Boolean) {
        if (showActivateAgreement) {
            view.setBackgroundResource(Color.TRANSPARENT)
        } else {
            view.setBackgroundResource(if (isNight) R.drawable.ic_main_bg_night else R.drawable.ic_main_bg_day)
        }
    }

    /**
     *  历史结果左侧图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setHistoryLeftImage"], requireAll = false)
    fun setHistoryLeftImage(view: SkinImageView, type: Int) {
        val resource = if (type == 2) {
            view.setImageResource(
                R.drawable.ic_search_history_search_day,
                R.drawable.ic_search_history_search_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.ic_search_history_search_night else R.drawable.ic_search_history_search_day
        } else {
            view.setImageResource(
                R.drawable.ic_search_history_periphery_day,
                R.drawable.ic_search_history_periphery_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.ic_search_history_periphery_night else R.drawable.ic_search_history_periphery_day
        }
        view.setImageResource(resource)
    }

    /**
     *  历史结果左侧图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setHistoryRightImage"], requireAll = false)
    fun setHistoryRightImage(view: SkinImageView, type: Int) {
        val resource = if (type == 6) {
            view.setImageResource(
                R.drawable.ic_search_alongway_suggestion_right_day,
                R.drawable.ic_search_alongway_suggestion_right_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.ic_search_alongway_suggestion_right_night else R.drawable.ic_search_alongway_suggestion_right_day
        } else {
            view.setImageResource(
                R.drawable.ic_search_history_navigation_day,
                R.drawable.ic_search_history_navigation_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.ic_search_history_navigation_night else R.drawable.ic_search_history_navigation_day
        }
        view.setImageResource(resource)
    }

    /**
     *  预搜索结果左侧图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setSuggestionRightImage"], requireAll = false)
    fun setSuggestionRightImage(view: SkinImageView, type: Int) {
        val resource = if (type == 5) {
            view.setImageResource(
                R.drawable.ic_search_alongway_suggestion_right_day,
                R.drawable.ic_search_alongway_suggestion_right_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.ic_search_alongway_suggestion_right_night else R.drawable.ic_search_alongway_suggestion_right_day
        } else {
            view.setImageResource(
                R.drawable.ic_search_suggestion_day,
                R.drawable.ic_search_suggestion_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.ic_search_suggestion_night else R.drawable.ic_search_suggestion_day
        }
        view.setImageResource(resource)
    }

    /**
     *  搜索结果选中与不选中背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchResultBg"], requireAll = false)
    fun setSearchResultBg(view: SkinConstraintLayout, isSelect: Boolean) {
        val backgroundResource =
            if (isSelect) {
                view.setBackground(
                    R.drawable.shape_bg_search_result_day,
                    R.drawable.shape_bg_search_result_night
                )
                if (NightModeGlobal.isNightMode()) R.drawable.shape_bg_search_result_night else R.drawable.shape_bg_search_result_day
            } else {
                R.drawable.shape_bg_search_transparent
            }
        view.setBackgroundResource(backgroundResource)
    }

    /**
     *  预搜索结果左侧图标
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchResultRightImage"], requireAll = false)
    fun setSearchResultRightImage(view: SkinImageView, type: Int) {
        val resource = if (type == 2 || type == 3 || type == 4 || type == 5 || type == 9) {
            view.setImageResource(
                R.drawable.ic_search_alongway_suggestion_right_day,
                R.drawable.ic_search_alongway_suggestion_right_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.ic_search_alongway_suggestion_right_night else R.drawable.ic_search_alongway_suggestion_right_day
        } else {
            view.setImageResource(
                R.drawable.selector_ic_search_history_navigation_day,
                R.drawable.selector_ic_search_history_navigation_night
            )
            if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_search_history_navigation_night else R.drawable.selector_ic_search_history_navigation_day
        }
        view.setImageResource(resource)
    }

    /**
     *  搜索列表item title
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchListTitleTextColor"], requireAll = false)
    fun setSearchListTitleTextColor(textView: TextView, isNight: Boolean) {
        textView.setTextColor(
            if (isNight) ContextCompat.getColor(
                textView.context,
                com.desaysv.psmap.model.R.color.onPrimaryNight
            ) else
                ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onPrimaryDay
                )
        )
    }

    /**
     *  搜索列表item title
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchListPositionDayNight", "setSearchListPositionSelect"], requireAll = false)
    fun setSearchListPositionTextColor(textView: TextView, isNight: Boolean, isSelect: Boolean) {
        textView.setTextColor(
            if (isNight) {
                if (isSelect) {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.customColorSearchPositionSelectTextColorNight
                    )
                } else {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.onPrimaryNight
                    )

                }
            } else {
                if (isSelect) {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.customColorSearchPositionSelectTextColorDay
                    )
                } else {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.onPrimaryDay
                    )

                }
            }
        )
    }

    /**
     *  搜索列表item HistoryVia
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchHistoryVia"], requireAll = false)
    fun setSearchHistoryVia(imageView: ImageView, isNight: Boolean) {
        imageView.setBackgroundResource(if (isNight) R.drawable.ic_search_history_via_night else R.drawable.ic_search_history_via_day)
    }

    /**
     *  搜索列表item mark
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchMark"], requireAll = false)
    fun setSearchMark(textView: TextView, poi: POI) {
        if (poi.hisMark > SearchSuggestMark.SearchSuggestMarkNULL && poi.hisMark < SearchSuggestMark.SearchSuggestMarkBook) {
            textView.visibility = View.VISIBLE
            textView.text = when (poi.hisMark) {
                SearchSuggestMark.SearchSuggestMarkHome -> "家"
                SearchSuggestMark.SearchSuggestMarkCompany -> "公司"
                SearchSuggestMark.SearchSuggestMarkView -> "浏览过"
                SearchSuggestMark.SearchSuggestMarkCollect -> "已收藏"
                SearchSuggestMark.SearchSuggestMarkNavi -> "导航过"
                SearchSuggestMark.SearchSuggestMarkBook -> "浏览过"
                else -> ""
            }
        } else {
            textView.visibility = View.GONE
        }
    }


    /**
     *  搜索列表item mark
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchMarkVisiable", "setSearchMarShow"], requireAll = false)
    fun setSearchMarkVisiable(view: View, poi: POI, show: Boolean) {
        if (poi.hisMark > SearchSuggestMark.SearchSuggestMarkNULL && poi.hisMark < SearchSuggestMark.SearchSuggestMarkBook) {
            view.visibility = if (show) View.VISIBLE else View.GONE
        } else {
            view.visibility = if (show) View.GONE else View.VISIBLE
        }
    }

    /**
     *  搜索列表item address
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchListAddressTextColor"], requireAll = false)
    fun setSearchListAddressTextColor(textView: TextView, isNight: Boolean) {
        textView.setTextColor(
            if (isNight) ContextCompat.getColor(
                textView.context,
                com.desaysv.psmap.model.R.color.onSecondaryNight
            ) else
                ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.onSecondaryDay
                )
        )
    }

    /**
     *  搜索列表item line
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchLine"], requireAll = false)
    fun setSearchLine(view: View, isNight: Boolean) {
        view.setBackgroundResource(if (isNight) com.desaysv.psmap.model.R.color.lineNight else com.desaysv.psmap.model.R.color.lineDay)
    }

    /**
     *  预搜索列表item iv_suggestion
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchIvSuggestion"], requireAll = false)
    fun setSearchIvSuggestion(imageView: ImageView, isNight: Boolean) {
        imageView.setImageResource(if (isNight) R.drawable.ic_search_history_periphery_night else R.drawable.ic_search_history_periphery_day)
    }

    /**
     *  预搜索列表item suggestion_child
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearSuggestionChild"], requireAll = false)
    fun setSearSuggestionChild(view: View, isNight: Boolean) {
        view.setBackgroundResource(if (isNight) R.drawable.shape_bg_search_result_child_night else R.drawable.shape_bg_search_result_child_day)
    }

    /**
     *  预搜索列表item suggestion_child
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearSuggestionChildSelect"], requireAll = false)
    fun setSearSuggestionChildSelect(view: View, isNight: Boolean) {
        view.setBackgroundResource(if (isNight) R.drawable.shape_bg_search_result_child_select_night else R.drawable.shape_bg_search_result_child_select_day)
    }

    /**
     *  搜索 城市离线数据 Arrow
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearOfflineCityArrow"], requireAll = false)
    fun setSearOfflineCityArrow(view: View, isNight: Boolean) {
        view.setBackgroundResource(if (isNight) R.drawable.iv_offline_city_list_arrow_down_night else R.drawable.iv_offline_city_list_arrow_down_day)
    }

    /**
     *  搜索 子poi背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchResultChildBg"], requireAll = false)
    fun setSearchResultChildBg(view: View, isNight: Boolean) {
        view.setBackgroundResource(if (isNight) com.desaysv.psmap.model.R.drawable.selector_bg_inverse_primary_night else com.desaysv.psmap.model.R.drawable.selector_bg_inverse_primary_day)
    }

    /**
     *  搜索 子poi text背景
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchResultChildTextBg"], requireAll = false)
    fun setSearchResultChildTextBg(view: View, isNight: Boolean) {
        view.setBackgroundResource(if (isNight) R.drawable.shape_bg_search_result_child_night else R.drawable.shape_bg_search_result_child_day)
    }

    /**
     *  搜索结果列表item line
     */
    @JvmStatic
    @BindingAdapter(value = ["setSearchResultLine"], requireAll = false)
    fun setSearchResultLine(view: View, isNight: Boolean) {
        view.setBackgroundResource(if (isNight) com.desaysv.psmap.model.R.color.lineNight else com.desaysv.psmap.model.R.color.lineDay)
    }

    /**
     *  设置个人中心车牌号文字大小
     */
    @JvmStatic
    @BindingAdapter(value = ["setVehicleNumberTextSize"], requireAll = false)
    fun setVehicleNumberTextSize(textView: TextView, text: String) {
        if (TextUtils.isEmpty(text)) {
            textView.textSize = textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_24).toFloat()
        } else if (TextUtils.equals(text, textView.resources.getString(R.string.sv_setting_rb4))) {
            textView.textSize = textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_24).toFloat()
        } else {
            val length = text.filter { it.isLetter() }.length
            if (length <= 1) {
                textView.textSize = textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_24).toFloat()
            } else if (length < 6) {
                textView.textSize = textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_22).toFloat()
            } else if (length < 7) {
                textView.textSize = textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_20).toFloat()
            } else {
                textView.textSize = textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_19_5).toFloat()
            }
        }
    }

    /**
     * 设置fragment 距离左边和底部边距
     */
    @JvmStatic
    @BindingAdapter(value = ["marginFragment", "screenStatusFragment"], requireAll = false)
    fun marginFragment(view: View, showActivateAgreement: Boolean, screenStatus: Boolean) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.leftMargin = if (showActivateAgreement) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_0)
        else if (screenStatus) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_822)
        else view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_0)

        params.bottomMargin = view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_130)
        view.layoutParams = params
    }

    /**
     * 设置激活界面和地图提示页面边距
     */
    @JvmStatic
    @BindingAdapter(value = ["marginActivateAgreement"], requireAll = false)
    fun marginActivateAgreement(view: View, showActivateAgreement: Boolean) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.leftMargin =
            if (showActivateAgreement) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_0) else view.resources.getDimensionPixelSize(
                com.desaysv.psmap.base.R.dimen.sv_dimen_822
            )
        params.bottomMargin = view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_130)
        view.layoutParams = params
    }

    /**
     * 字体是否需要加粗
     */
    @JvmStatic
    @BindingAdapter(value = ["setTypefaceBold"], requireAll = false)
    fun setTypefaceBold(textView: TextView, isBold: Boolean) {
        textView.setTypeface(null, if (isBold) Typeface.BOLD else Typeface.NORMAL)
    }

    /**
     *
     */
    @JvmStatic
    @BindingAdapter(value = ["setRadioButtonSelect"], requireAll = false)
    fun setRadioButtonSelect(textView: SkinRadioButton, isSelect: Boolean) {
        textView.setTextAppearance(if (isSelect) com.desaysv.psmap.model.R.style.CustomTextAppearanceTabSelect else com.desaysv.psmap.model.R.style.CustomTextAppearanceTabUnSelect)
        if (isSelect) {
            textView.setTextColor(
                if (NightModeGlobal.isNightMode()) {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.onPrimaryNight
                    )
                } else {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.onPrimaryDay
                    )
                }
            )
            textView.setTextColor(com.desaysv.psmap.model.R.color.onPrimaryDay, com.desaysv.psmap.model.R.color.onPrimaryNight)
            textView.setTypeface(null, Typeface.BOLD)
        } else {
            textView.setTextColor(
                if (NightModeGlobal.isNightMode()) {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.onSecondaryNight
                    )
                } else {
                    ContextCompat.getColor(
                        textView.context,
                        com.desaysv.psmap.model.R.color.onSecondaryContainerDay
                    )
                }
            )
            textView.setTextColor(com.desaysv.psmap.model.R.color.onSecondaryContainerDay, com.desaysv.psmap.model.R.color.onSecondaryNight)
            textView.setTypeface(null, Typeface.NORMAL)
        }
    }

    /**
     * 图层比例尺右边距
     */
    @JvmStatic
    @BindingAdapter(value = ["marginCmpLogoScaleLine"], requireAll = false)
    fun marginCmpLogoScaleLine(view: View, isJetOurGaoJie: Boolean) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.rightMargin =
            if (isJetOurGaoJie) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_164) else view.resources.getDimensionPixelSize(
                com.desaysv.psmap.base.R.dimen.sv_dimen_36
            )
        view.layoutParams = params
    }

    /**
     * 路书 天数：几天
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(value = ["setAhaTripDayText"], requireAll = false)
    fun setAhaTripDayText(textView: TextView, day: Int) {
        textView.text = "天数：" + day + "天"
    }

    /**
     * 路书 里程：几km
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(value = ["setAhaTripMileageText"], requireAll = false)
    fun setAhaTripMileageText(textView: TextView, mileage: Int) {
        val mileageString =
            NavigationUtil.ahaMeterToStrEnglish(
                SdkApplicationUtils.getApplication(),
                mileage.toLong()
            )
        textView.text = "里程：$mileageString"
    }

    /**
     * 路书 里程：几km
     */
    @JvmStatic
    @BindingAdapter(value = ["setAhaTripMileage"], requireAll = false)
    fun setAhaTripMileage(textView: TextView, mileage: Int) {
        val mileageString =
            NavigationUtil.ahaMeterToStrEnglish(
                SdkApplicationUtils.getApplication(),
                mileage.toLong()
            )
        textView.text = mileageString
    }

    /**
     * 收藏轨迹路书 天里程：几km
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(value = ["setAhaTankCollectMileageText"], requireAll = false)
    fun setAhaTankCollectMileageText(textView: TextView, mileage: String) {
        textView.text = mileage
    }

    /**
     * 轨迹路书 创建时间
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(value = ["setAhaTripCreateText"], requireAll = false)
    fun setAhaTripCreateText(textView: TextView, time: String) {
        textView.text = "创建时间：$time"
    }

    /**
     * 轨迹路书 创建时间点
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(value = ["setAhaMarkAtTime"], requireAll = false)
    fun setAhaMarkAtTime(textView: TextView, marker: TankMarkers) {
        if (marker != null) {
            val instant = Instant.ofEpochSecond(marker.time.toLong())
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())

            val formattedTime = formatter.format(instant)
            textView.text = formattedTime
        }
    }

    /**
     *  全览切换按钮
     */
    @JvmStatic
    @BindingAdapter(value = ["setOverviewImage", "setOverviewImageNight"], requireAll = false)
    fun setOverviewImage(imageView: ImageView, inFullView: Boolean, isNightMode: Boolean) {
        if (inFullView) {
            imageView.setImageResource(if (isNightMode) R.drawable.ic_un_overview_night else R.drawable.ic_un_overview_day)
        } else {
            imageView.setImageResource(if (isNightMode) R.drawable.ic_overview_night else R.drawable.ic_overview_day)
        }
    }

    /**
     * 轨迹路书 创建时间点
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["ahaCollectLoadingSelectTab", "ahaCollectLoadingGuide", "ahaCollectLoadingLine", "ahaCollectLoadingTank"],
        requireAll = false
    )
    fun ahaMyCollectLoadingVisible(
        view: View,
        selectTab: Int,
        guideCollectLoading: Boolean,
        lineCollectLoading: Boolean,
        tankCollectLoading: Boolean
    ) {
        when (selectTab) {
            0 -> {
                view.visibility = if (guideCollectLoading) View.VISIBLE else View.GONE
            }

            1 -> {
                view.visibility = if (lineCollectLoading) View.VISIBLE else View.GONE
            }

            2 -> {
                view.visibility = if (tankCollectLoading) View.VISIBLE else View.GONE
            }

            else -> {
                view.visibility = View.GONE
            }
        }
    }

    /**
     * 轨迹路书 创建时间点
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @BindingAdapter(
        value = ["ahaCollectEmptySelectTab", "ahaCollectEmptyGuide", "ahaCollectEmptyLine", "ahaCollectEmptyTank", "ahaCollectEmptyGuideList", "ahaCollectEmptyLineList", "ahaCollectEmptyTankList"],
        requireAll = false
    )
    fun ahaMyCollectEmptyVisible(
        view: View, selectTab: Int, guideCollectLoading: Boolean, lineCollectLoading: Boolean, tankCollectLoading: Boolean,
        guideListCollect: List<MineGuideList>?, lineListCollect: List<LineListModel.DataDTO.ListDTO>?, tankListCollect: List<TankCollectItem>?
    ) {
        when (selectTab) {
            0 -> {
                view.visibility = if (!guideCollectLoading && guideListCollect?.isNotEmpty() == false) View.VISIBLE else View.GONE
            }

            1 -> {
                view.visibility = if (!lineCollectLoading && lineListCollect?.isNotEmpty() == false) View.VISIBLE else View.GONE
            }

            2 -> {
                view.visibility = if (!tankCollectLoading && tankListCollect?.isNotEmpty() == false) View.VISIBLE else View.GONE
            }

            else -> {
                view.visibility = View.GONE
            }
        }
    }

    /**
     * 路书详情高度
     */
    @JvmStatic
    @BindingAdapter(value = ["ahaDetailHeight"], requireAll = false)
    fun ahaDetailHeight(view: View, selectTab: Int) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.height = if (selectTab == 0) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_966)
        else view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_1178)
        view.layoutParams = params
    }

    /**
     * 设置景点推荐卡片距离顶部距离
     */
    @JvmStatic
    @BindingAdapter(value = ["marginTopAhaScenic"], requireAll = false)
    fun marginTopAhaScenic(view: View, hasCruiseLine: Boolean) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.topMargin =
            if (hasCruiseLine) view.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_334) else view.resources.getDimensionPixelSize(
                com.desaysv.psmap.base.R.dimen.sv_dimen_254
            )
        view.layoutParams = params
    }

    /**
     * 自定义绑定适配器，用于设置 SkinImageView 的选中状态。
     * @param view 要设置选中状态的 SkinImageView 实例。
     * @param isSelected 布尔值，表示是否选中。
     */
    @JvmStatic
    @BindingAdapter(value = ["stateSelected"], requireAll = false)
    fun stateSelected(view: View, isSelected: Boolean) {
        view.isSelected = isSelected
    }

    /**
     *  更多路书文字
     */
    @JvmStatic
    @BindingAdapter(value = ["setMoreAhaText"], requireAll = false)
    fun setMoreAhaText(textView: TextView, isNight: Boolean) {
        textView.setTextColor(
            if (isNight) ContextCompat.getColor(
                textView.context,
                com.desaysv.psmap.model.R.color.onPrimaryNight
            ) else
                ContextCompat.getColor(
                    textView.context,
                    com.desaysv.psmap.model.R.color.customAhaSecenicPointTextDay
                )
        )
    }

    /**
     *  路书收藏星星
     */
    @JvmStatic
    @BindingAdapter(value = ["setAhaFav"], requireAll = false)
    fun setAhaFav(view: ImageView, isNight: Boolean) {
        view.setImageResource(if (isNight) R.drawable.ic_aha_fav_night else R.drawable.ic_aha_fav_day)
    }

    /**
     *  路书详情title
     */
    @JvmStatic
    @BindingAdapter(value = ["setAhaDetailTitle", "setAhaDetailTitleNight"], requireAll = false)
    fun setAhaDetailTitle(textview: TextView, title: String?, isNight: Boolean) {
        if (title.isNullOrEmpty()) {
            textview.text = ""
            return
        }
        var parts: List<String>? = null
        for (i in title.indices) {
            if (title[i] == '|') { // 同时匹配半角和全角竖线
                parts = title.split("|")
                break
            } else if (title[i] == '｜') { // 同时匹配半角和全角竖线
                parts = title.split("｜")
                break
            }
        }
        if (!parts.isNullOrEmpty()) {
            val spannable = SpannableStringBuilder(parts[0]).apply {
                val d =
                    ContextCompat.getDrawable(textview.context, if (isNight) R.drawable.vertical_divider_night else R.drawable.vertical_divider_day)!!
                d.setBounds(
                    0, 0,
                    d.intrinsicWidth, d.intrinsicHeight
                )
                append("  ") // 空格占位
                setSpan(ImageSpan(d), length - 1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                append(" ") // 空格占位
                append(parts[1])
            }
            textview.text = spannable
        } else {
            textview.text = title
        }
    }

    /**
     *  设置英文和中文文本大小
     */
    @JvmStatic
    @BindingAdapter(value = ["setEnglishChineseSizeText"], requireAll = false)
    fun setEnglishChineseSizeText(textView: TextView, text: String?) {
        text?.let {
            val spannable = SpannableString(it)

            it.forEachIndexed { index, char ->
                if (char.toString().matches(Regex("[a-zA-Z0-9]"))) {
                    spannable.setSpan(
                        AbsoluteSizeSpan(textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_30)),
                        index, index + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                } else {
                    spannable.setSpan(
                        AbsoluteSizeSpan(textView.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_28)),
                        index, index + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            textView.text = spannable
        }

    }
}