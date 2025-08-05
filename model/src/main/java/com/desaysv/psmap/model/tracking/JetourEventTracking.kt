package com.desaysv.psmap.model.tracking

import android.content.Context
import android.os.IBinder
import com.desaysv.bigdata.IBigDataCallBack
import com.desaysv.bigdatalibrary.BigDataInterfaceManager
import com.desaysv.bigdatalibrary.TrackBean
import com.desaysv.bigdatalibrary.TrackJsonFactory
import com.desaysv.bigdatalibrary.TrackUtil
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.tracking.IEventTrack
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 捷途埋点实现类
 */
class JetourEventTracking : IEventTrack {
    /**
     * /**
     *  * 埋点事件
     *  *
     *  * @param id 应用编码                               eg:JZ05
     *  * @param eventId 事件编码                          eg:Dock_Set
     *  * @param contentJson 属性集合的json字符串
     *  */
     * public void trackEvent(String id, String eventId, String contentJson) {
     *     BigDataInterfaceManager.getInstances().trackValue(id, eventId, contentJson, System.currentTimeMillis());
     * }
     * id 应用编码
     * eventId 事件编码
     * content{
     * 【
     * locationId 功能编码
     * attributeId  属性编码
     * attributeValue  属性值
     * 】
     */

    private var connected: Boolean? = null
    private var mContext: Context? = null

    private lateinit var gson: Gson

    val callback = object : IBigDataCallBack {
        override fun asBinder(): IBinder? {
            return null
        }

        override fun onSendMessage(p0: String?, p1: String?) {
            //
        }

        override fun onServiceConnectStatus(connect: Boolean) {
            Timber.d("JetourEventTracking onServiceConnectStatus connect=$connect")
            connected = connect
        }

    }

    override fun init(context: Context) {
        Timber.d("JetourEventTracking init")
        mContext = context
        BigDataInterfaceManager.getInstances().init(mContext, callback)
        gson = Gson()
    }

    /**
     * 埋点事件
     * @param params
     * 时间参数传时间戳过来这里再转换
     * 参数属性可以直接传递文档定义过来，但是不建议。
     * 为了以后切换埋点，建议传递地图自己的定义，在这个类写方法转换
     */
    override suspend fun trackEvent(eventName: EventTrackingUtils.EventName, params: Map<EventTrackingUtils.EventValueName, Any>) {
        if (connected != true) {
            Timber.i("JetourEventTracking trackEvent connected=false")
            if (connected == false) {
                connected = null
                BigDataInterfaceManager.getInstances().init(mContext, callback)
                delay(1000)
            } else {
                Timber.d("JetourEventTracking trackEvent connected=null, connecting")
                delay(1000)
            }
        }
        withContext(Dispatchers.IO) {
            Timber.d("JetourEventTracking trackEvent eventName=$eventName")
            val trackBeans = when (eventName) {
                EventTrackingUtils.EventName.App_Open -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 打开方式
                             * 1:语音;
                             * 2:点击APPlist;
                             * 3:首页快捷窗口;
                             * 4:点击DOCK栏快捷应用入口
                             */
                            EventTrackingUtils.EventValueName.OpsMode -> TrackBean("JBO30FOF", it.key.name, it.value.toString())
                            /*
                             * 打开时间
                             */
                            EventTrackingUtils.EventValueName.OpsTime -> TrackBean("JBO30FOF", it.key.name, getFormatTime(it.value as Long))
                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.App_Close -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 关闭方式
                             * 1:语音;
                             * 2:点击;
                             */
                            EventTrackingUtils.EventValueName.ClsMode -> TrackBean("JBO30FOF", it.key.name, it.value.toString())
                            /*
                             * 关闭时间
                             */
                            EventTrackingUtils.EventValueName.ClsTime -> TrackBean("JBO30FOF", it.key.name, getFormatTime(it.value as Long))
                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.Nav_Start -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 导航开启时间
                             */
                            EventTrackingUtils.EventValueName.StartTime -> TrackBean("JB030101", it.key.name, getFormatTime(it.value as Long))
                            /*
                             * 导航开始地点经纬度
                             * 经纬度数据,精确到小数点后5位
                             */
                            EventTrackingUtils.EventValueName.Departure -> TrackBean("JB030102", it.key.name, it.value.toString())
                            /*
                             * 地图容器形态
                             * 0:2/3屏;
                             * 1:全屏;
                             * 2:1/3屏;
                             */
                            EventTrackingUtils.EventValueName.MappageForm -> TrackBean("JB030106", it.key.name, it.value.toString())
                            /*
                             *点击回家图标的时间
                             */
                            EventTrackingUtils.EventValueName.HomeClick -> TrackBean("JB030107", it.key.name, getFormatTime(it.value as Long))
                            /*
                             *点击去公司图标的时间
                             */
                            EventTrackingUtils.EventValueName.CompanyClick -> TrackBean("JB030108", it.key.name, getFormatTime(it.value as Long))
                            /*
                             *点击去收藏夹图标的时间
                             */
                            EventTrackingUtils.EventValueName.FavoritesClick -> TrackBean("JB030109", it.key.name, getFormatTime(it.value as Long))
                            /*
                             * 点击组队图标的时间
                             */
                            EventTrackingUtils.EventValueName.TeamClick -> TrackBean("JB030110", it.key.name, getFormatTime(it.value as Long))
                            /*
                             *点击GPS定位按键图标的时间
                             */
                            EventTrackingUtils.EventValueName.GPSLocClick -> TrackBean("JB030111", it.key.name, getFormatTime(it.value as Long))

                            /**
                             * 点击一键加油(补能)按键图标的时间
                             */
                            EventTrackingUtils.EventValueName.OnerefuelClick -> TrackBean("JB030112", it.key.name, getFormatTime(it.value as Long))

                            /**
                             * 点击行程分享时间
                             */
                            EventTrackingUtils.EventValueName.TravelShareClick -> TrackBean("JB030206", it.key.name, getFormatTime(it.value as Long))

                            /**
                             *短信分享的时间
                             */
                            EventTrackingUtils.EventValueName.MailShareTime -> TrackBean("JB030207", it.key.name, getFormatTime(it.value as Long))


                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.Nav_Finish -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 导航结束时间
                             */
                            EventTrackingUtils.EventValueName.EndTime -> TrackBean("JB030103", it.key.name, getFormatTime(it.value as Long))
                            /*
                           * 导航结束地点经纬度
                           * 经纬度数据,精确到小数点后5位
                           */
                            EventTrackingUtils.EventValueName.Destination -> TrackBean("JB030104", it.key.name, it.value.toString())
                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.Search_Click -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 0:语音;
                             * 1:手动输入;
                             */
                            EventTrackingUtils.EventValueName.SearchType -> TrackBean("JB030105", it.key.name, it.value.toString())
                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.Map_Set -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 0:非静音(默认);
                             * 1:静音;
                             */
                            EventTrackingUtils.EventValueName.MuteSet -> TrackBean("JB030201", it.key.name, it.value.toString())
                            /*
                             * 点击静音切换按键图标的时间
                             */
                            EventTrackingUtils.EventValueName.MuteClick -> TrackBean("JB030202", it.key.name, getFormatTime(it.value as Long))
                            /*
                             * 方位切换
                             * 1:2D车头向上;
                             * 2:2D正北向上;
                             * 3:3D车头向上;
                             */
                            EventTrackingUtils.EventValueName.TowardSet -> TrackBean("JB030203", it.key.name, convertMapViewMode(it.value as Int))
                            /*
                             *点击方位切换按键图标的时间
                             */
                            EventTrackingUtils.EventValueName.TowardClick -> TrackBean("JB030204", it.key.name, getFormatTime(it.value as Long))

                            /**
                             * 路线偏好设置
                             * 0:全部关闭;
                             * 1:高德推荐;
                             * 2:躲避拥堵;
                             * 3:高速优先;
                             * 4:不走高速;
                             * 5:少收费;
                             * 6:大路优先;
                             * 7:速度最快;
                             */
                            EventTrackingUtils.EventValueName.RoutePerferSet -> TrackBean("JB030205", it.key.name, it.value.toString())

                            /**
                             * 避开限行
                             * 0:关闭;
                             * 1:开启;
                             */
                            EventTrackingUtils.EventValueName.AvoidLimitSw -> TrackBean("JB030208", it.key.name, it.value.toString())

                            /**
                             * 路况概览模式
                             * 0:极简;
                             * 1:经典;
                             */
                            EventTrackingUtils.EventValueName.OverviewModeSet -> TrackBean("JB030209", it.key.name, it.value.toString())

                            /**
                             * 导航播报模式
                             * 0:详细播报;
                             * 1:简洁播报;
                             * 2:超简洁播报;
                             */
                            EventTrackingUtils.EventValueName.BroadcastModeSet -> TrackBean("JB030210", it.key.name, it.value.toString())

                            /**
                             * 语音播报
                             * 返回选择的语音类型,比如标准女音,比如小团团
                             */
                            EventTrackingUtils.EventValueName.BroadcastSet -> TrackBean("JB030211", it.key.name, it.value.toString())

                            /**
                             * 巡航播报
                             * 0:前方路况;
                             * 1:电子眼;
                             * 2:安全提醒;
                             */
                            EventTrackingUtils.EventValueName.CruiseBroadcastSet -> TrackBean("JB030212", it.key.name, it.value.toString())

                            /**
                             * 巡航播报开关
                             * 0:关闭;
                             * 1:开启;
                             */
                            EventTrackingUtils.EventValueName.CBSw -> TrackBean("JB030213", it.key.name, it.value.toString())

                            /**
                             * 车标罗盘
                             * 0:罗盘1;
                             * 1:罗盘2;
                             */
                            EventTrackingUtils.EventValueName.VCSet -> TrackBean("JB030215", it.key.name, it.value.toString())

                            /**
                             *地图文字大小
                             * 0:标准字号;
                             * 1:大字号;
                             */
                            EventTrackingUtils.EventValueName.WordSize -> TrackBean("JB030216", it.key.name, it.value.toString())

                            /**
                             * 实时路况开关
                             * 0:关闭;
                             * 1:开启;
                             */
                            EventTrackingUtils.EventValueName.RoadCondSw -> TrackBean("JB030217", it.key.name, it.value.toString())

                            /**
                             * 收藏点标注开关
                             * 0:关闭;
                             * 1:开启;
                             */
                            EventTrackingUtils.EventValueName.FpointSw -> TrackBean("JB030218", it.key.name, it.value.toString())

                            /**
                             * 自动比例尺开关
                             * 0:关闭;
                             * 1:开启;
                             */
                            EventTrackingUtils.EventValueName.AutscalebarSw -> TrackBean("JB030219", it.key.name, it.value.toString())

                            /**
                             * 意图导航开关
                             *  0:关闭;
                             *  1:开启;
                             */
                            EventTrackingUtils.EventValueName.IntenNavSw -> TrackBean("JB030220", it.key.name, it.value.toString())

                            /**
                             * 登录状态
                             * 0:已登录;
                             * 1:未登录;
                             */
                            EventTrackingUtils.EventValueName.LoginStatus -> TrackBean("JB030301", it.key.name, it.value.toString())

                            /**
                             * 登录账号时间
                             */
                            EventTrackingUtils.EventValueName.LogonTime -> TrackBean("JB030302", it.key.name, getFormatTime(it.value as Long))

                            /**
                             * 退出登录时间
                             */
                            EventTrackingUtils.EventValueName.LogoutTime -> TrackBean("JB030303", it.key.name, getFormatTime(it.value as Long))

                            /**
                             * 登录方式
                             * 0:二维码登录;
                             * 1:验证码登录;
                             */
                            EventTrackingUtils.EventValueName.LoginType -> TrackBean("JB030304", it.key.name, it.value.toString())

                            /**
                             * 微信互联状态
                             * 0:未绑定;
                             * 1:已绑定;
                             * 2:未知
                             */
                            EventTrackingUtils.EventValueName.WeChatStatus -> TrackBean("JB030305", it.key.name, it.value.toString())

                            /**
                             *自动记录行程开关
                             * 0:关闭;
                             * 1:开启;
                             */
                            EventTrackingUtils.EventValueName.AutoRecordSw -> TrackBean("JB030306", it.key.name, it.value.toString())

                            /**
                             * 手车互联状态
                             * 0:未连接;
                             * 1:已连接;
                             * 2:未知
                             */
                            EventTrackingUtils.EventValueName.PhoneToCarStatus -> TrackBean("JB030307", it.key.name, it.value.toString())

                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.SurroundSearch_Click -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 周边搜发起时间
                             */
                            EventTrackingUtils.EventValueName.SearchTime -> TrackBean("JB030113", it.key.name, getFormatTime(it.value as Long))
                            /*
                             * 周边搜索类型
                             * 卫生间,加油站,美食,酒店等
                             */
                            EventTrackingUtils.EventValueName.SearchCategory -> TrackBean("JB030114", it.key.name, it.value.toString())
                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.OnthewaySearch_Click -> {
                    params.map {
                        when (it.key) {
                            /*
                             * 顺路搜发起时间
                             */
                            EventTrackingUtils.EventValueName.SearchTime -> TrackBean("JB030115", it.key.name, getFormatTime(it.value as Long))
                            /*
                             * 顺路搜索类型
                             * 卫生间,加油站,美食,酒店等
                             */
                            EventTrackingUtils.EventValueName.SearchCategory -> TrackBean("JB030116", it.key.name, it.value.toString())
                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.JetourOnly_Click -> {
                    params.map {
                        when (it.key) {
                            /**
                             *点击去捷途专属图标的时间
                             */
                            EventTrackingUtils.EventValueName.JetourOnlyClick -> TrackBean("JB030221", it.key.name, getFormatTime(it.value as Long))

                            /**
                             * 捷途权益类型
                             *捷途驿站/越野场地/露营地/餐
                             * 饮/咖啡店/景区/住宿/当季精选
                             */
                            EventTrackingUtils.EventValueName.JetourEQuity -> TrackBean("JB030401", it.key.name, it.value.toString())

                            /**
                             * 点击去捷途权益类型TAB的时间
                             */
                            EventTrackingUtils.EventValueName.JetourEQuityClick -> TrackBean("JB030402", it.key.name, getFormatTime(it.value as Long))

                            /**
                             *点击去捷途路书TAB的时间
                             */
                            EventTrackingUtils.EventValueName.JetourRbookClick -> TrackBean("JB030501", it.key.name, getFormatTime(it.value as Long))

                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.RoadBookSearch_Click -> {
                    params.map {
                        when (it.key) {

                            /**
                             * 发起搜索时间
                             */
                            EventTrackingUtils.EventValueName.SearchTime -> TrackBean("JB030502", it.key.name, getFormatTime(it.value as Long))

                            else -> {
                                null
                            }
                        }
                    }
                }

                EventTrackingUtils.EventName.IntSearch_Click -> {
                    params.map {
                        when (it.key) {
                            /**
                             * 探趣搜索
                             * 点击捷途探趣TAB的时间
                             */
                            EventTrackingUtils.EventValueName.JetourInterestClick -> TrackBean(
                                "JB030117",
                                it.key.name,
                                getFormatTime(it.value as Long)
                            )

                            else -> {
                                null
                            }
                        }
                    }
                }
            }
            trackBeans?.let {
                Timber.d("JetourEventTracking trackEvent trackBeans=${gson.toJson(trackBeans)}")
                TrackUtil.getInstance().trackEvent("JB03", eventName.name, TrackJsonFactory.createJson(*(trackBeans.filterNotNull()).toTypedArray()))
            }
        }
    }

    /**
     * YYYY/MM/DD HH:MM:SS
     */
    private fun getFormatTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
            .withZone(ZoneId.systemDefault()) // 使用系统默认时区
        return formatter.format(instant)
    }

    /** 方位切换
     * 1:2D车头向上;
     * 2:2D正北向上;
     * 3:3D车头向上;
     */
    private fun convertMapViewMode(switchMapViewMode: Int): String {
        return when (switchMapViewMode) {
            MapModeType.VISUALMODE_3D_CAR -> {
                "3"
            }

            MapModeType.VISUALMODE_2D_CAR -> {
                "1"
            }

            MapModeType.VISUALMODE_2D_NORTH -> {
                "2"
            }

            else -> "1"
        }
    }
}