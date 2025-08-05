package com.autosdk.bussiness.widget.route.constant;

/**
 * 路线偏好  默认0：高德推荐，字符类型,不包含2|4|8|16|32|64即为高德推荐 默认态； 2：躲避拥堵； 4：避免收费； 8：不走高速； 16：高速优先 32：速度最快  64：大路优先
 */
public class ConfigRoutePreference {

    /**
     * 默认 高德推荐
     * setRouteStrategy(RouteStrategyPersonalGaodeBest)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_DEFAULT = "0";

    /**
     * 躲避拥堵
     * setRouteStrategy(RouteStrategyPersonalTMC)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_JAN = "2";

    /**
     * 避免收费
     * setRouteStrategy(RouteStrategyPersonalLessMoney)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_CHARGE = "4";

    /**
     * 不走高速
     * setRouteStrategy(RouteStrategyPersonalLessHighway)
     * setConstrainCode(RouteCalcMulti | RouteAvoidFreeway | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_HIGHWAY = "8";

    /**
     * 高速优先
     * setRouteStrategy(RouteStrategyPersonalHighwayFirst)
     * setConstrainCode(RouteCalcMulti | RouteFreewayStrategy | RouteNetWorking)
     */
    public static final String PREFERENCE_USING_HIGHWAY = "16";

    /**
     * 速度最快
     * setRouteStrategy(RouteStrategyPersonalSpeedFirst)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_PERSONAL_SPEED_FIRST = "32";

    /**
     * 大路优先
     * setRouteStrategy(RouteStrategyPersonalWidthFirst)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_PERSONAL_WIDTH_FIRST = "64";

    /**
     * 躲避拥堵+避免收费
     * setRouteStrategy(RequestRouteTypeTMCFree)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_JAN_AND_CHARGE = "2|4";

    /**
     * 躲避拥堵+不走高速
     * setRouteStrategy(RouteStrategyPersonalTMC2LessHighway)
     * setConstrainCode(RouteCalcMulti | RouteAvoidFreeway | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_JAN_AND_HGIHWAY = "2|8";

    /**
     * 躲避拥堵+高速优先
     * setRouteStrategy(RouteStrategyPersonalTMC2Highway)
     * setConstrainCode(RouteCalcMulti | RouteFreewayStrategy | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY = "2|16";

    /**
     * 避免收费+不走高速
     * setRouteStrategy(RouteStrategyPersonalLessMoney2LessHighway)
     * setConstrainCode(RouteCalcMulti | RouteAvoidFreeway | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_CHARGE_AND_HIGHWAY = "4|8";

    /**
     * 躲避拥堵+避免收费+不走高速
     * setRouteStrategy(RouteStrategyPersonalTMC2LessMondy2LessHighway)
     * setConstrainCode(RouteCalcMulti | RouteAvoidFreeway | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY = "2|4|8";

    /**
     * 躲避拥堵+速度最快
     * setRouteStrategy(RouteStrategyPersonalTMC2SpeedFirst)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST = "2|32";

    /**
     * 躲避拥堵+大路优先
     * setRouteStrategy(RouteStrategyPersonalTMC2WidthFirst)
     * setConstrainCode(RouteCalcMulti | RouteNetWorking)
     */
    public static final String PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST = "2|64";

    /**
     * 充电路线
     * setConstrainCode(0x4000)
     */
    public static final String PREFERENCE_ELECTRIC_ROUTE= "16384";

    /**
     * 充电路线 + 躲避拥堵
     * setRouteStrategy(RouteStrategyPersonalTMC)
     * setConstrainCode(0x4000)
     */
    public static final String PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN= "16384|2";

    /**
     * 充电路线 + 高速优先
     * setRouteStrategy(RouteStrategyPersonalHighwayFirst)
     * setConstrainCode(0x4000)
     */
    public static final String PREFERENCE_ELECTRIC_ROUTE_AND_USING_HIGHWAY= "16384|16";

    /**
     * 充电路线 + 速度最快
     * setRouteStrategy(RouteStrategyPersonalHighwayFirst)
     * setConstrainCode(0x4000)
     */
    public static final String PREFERENCE_ELECTRIC_ROUTE_AND_SPEED_FIRST= "16384|32";


    /**
     * 充电路线 + 躲避拥堵 + 高速优先
     * setRouteStrategy(RouteStrategyPersonalTMC2Highway)
     * setConstrainCode(0x4000)
     */
    public static final String PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN_AND_USING_HIGHWAY= "16384|2|16";

    /**
     * 充电路线 + 躲避拥堵 + 速度最快
     *  setRouteStrategy(RouteStrategyPersonalTMC2SpeedFirst)
     * setConstrainCode(0x4000)
     */
    public static final String PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN_AND_SPEED_FIRST= "16384|2|32";
}
