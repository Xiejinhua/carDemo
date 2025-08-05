
package com.autosdk.bussiness.widget.navi.base;

/**
 * 卡片碰撞逻辑
 * @author AutoSDK
 * */
public final class CardPriority {

    private static final String TAG = "[drive]CardPriority";
    /**
     * 表示不参与碰撞或者参数不合法;
     */
    public final static int INVALID = -1;
    /**
     * 定义碰撞逻辑的matrix表;
     * 以2维数组表示，行代表的是新卡片，列代表的是当前卡片；
     * 行和列的顺序按照产品给出的文档中的卡片顺序;
     * public final static int INVALID = -1;
     * public final static int NEWCARD_NODISPLAY = 0;
     * public final static int NEWCARD_DISPLAY_OLDCARD_CACHE = 1;
     * public final static int NEWCARD_DISPLAY_OLDCARD_CLOSE = 2;
     * public final static int NEWCARD_CACHE_OLDCARD_DISPLAY = 3;
     *
     * 碰撞结果定义如下：
     *    1. 新卡片与旧卡片没有碰撞逻辑的，(包含属于同一类型，以及逻辑上无法碰撞等），值为-1；
     *    2. 新卡片不允许显示，旧卡片继续显示，值为0
     *    3. 新卡片显示，旧卡片关闭，新卡片结束后旧卡片需要恢复显示，值为1；
     *    4. 新卡片显示，旧卡片关闭，值为2；
     *    5. 新卡片不允许显示，旧卡片继续显示，旧卡片消失后新卡片再显示，值为3；
     *
     * **/

    private static final int MAX_CARD_NUM = 18;

    private static final int COLLISION_MATRIX[][] = {
        {-1,-1, 1, 2, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 2,-1, 1},
        {-1,-1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 1, 2,-1, 1},
        { 0, 0,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,-1, 0},
        { 0, 3, 0,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,-1, 0},
        { 0, 0, 1, 2,-1,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,-1, 0},
        { 0, 0, 1, 2,-1,-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2,-1, 0},
        { 3, 3, 1, 2, 3, 3,-1,-1, 0, 3, 3, 3, 3, 3, 3, 2,-1, 3},
        { 3, 3, 1, 2, 3, 3,-1,-1, 0, 3, 3, 3, 3, 3, 3, 3,-1, 3},
        { 3, 3, 1, 2, 3, 3, 2, 2,-1, 3, 3, 3, 3, 3, 3, 2,-1, 3},
        { 2, 2, 1, 2, 2, 2, 2, 2, 2,-1, 2, 2, 2, 2, 2, 2,-1, 2},
        { 2,-1, 1, 2, 2, 2, 2, 2, 2, 2,-1, 2, 2, 2, 2, 2,-1, 2},
        { 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0,-1, 0, 0, 0, 2,-1, 0},
        { 2,-1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2,-1, 2, 2, 2,-1, 2},
        { 0, 0, 1, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0,-1, 0, 0,-1, 0},
        { 3, 3, 1, 2, 3, 3, 1, 1, 1, 3, 3, 3, 3, 3,-1, 3,-1, 1},
        {-1,-1, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,-1, 2, 1},
        {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 2,-1,-1},
        { 3, 3, 1, 2, 3, 3, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3,-1,-1}
    };

    /**
     * getCollisionType, if the new card will show according to the attributes of the current card
     * @param curCard
     * @param newCard
     * @return 0, New card won't show
     *         1, New card show, old card cache
     *         2, New card show, old card dismissed
     *         3, New card cache, old card kept show
     */
    public static int getCollisionType(Enum curCard, Enum newCard) {

        int curIndex = curCard.ordinal();
        int newIndex = newCard.ordinal();

        //非法值;
        if (curIndex >= MAX_CARD_NUM || newIndex >= MAX_CARD_NUM) {
            return INVALID;
        }

        return COLLISION_MATRIX[newIndex][curIndex];
    }

}
