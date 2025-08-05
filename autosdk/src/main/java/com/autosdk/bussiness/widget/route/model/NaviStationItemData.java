package com.autosdk.bussiness.widget.route.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.autonavi.gbl.common.model.Coord2DInt32;

import java.util.ArrayList;
import java.util.List;

/**
 * 路线详情页各路段信息结构体
 */
public class NaviStationItemData implements Parcelable {
    // 描述类型
    private int mDesType;
    // 道路名称
    private String mRoadName;
    private String mDistanceDes;
    private int mNextDistance;
    private boolean isRightPassArea;
    private int mActionIcon = 0;
    private int mGroupActionIcon = 0;
    private int mNavigtionAction = 0;
    private byte mAciontType;
    private int mIndex;
    private String mActionDes;
    /* 聚合路段描述（距离 费用 红绿灯数） */
    private String mGroupDes;
    private String mGroupTrafficDes;

    private boolean isSubListExpand = false;
    private List<SubItem> mSubList;

    /**
     * 是否被选中避让
     */
    private boolean mIsSelect;

    public NaviStationItemData() {
        mActionIcon = -1;
        mGroupActionIcon = -1;
        mNavigtionAction = -1;
        mRoadName = "";
        mDistanceDes = "";
        mAciontType = 0;
        mGroupTrafficDes = "";
        mSubList = new ArrayList<SubItem>();
    }

    public int getDesType() {
        return mDesType;
    }

    public void setDesType(int mDesType) {
        this.mDesType = mDesType;
    }

    public String getRoadName() {
        return mRoadName;
    }

    public void setRoadName(String mRoadName) {
        this.mRoadName = mRoadName;
    }

    public String getDistanceDes() {
        return mDistanceDes;
    }

    public void setDistanceDes(String mDistanceDes) {
        this.mDistanceDes = mDistanceDes;
    }

    public int getNextDistance() {
        return mNextDistance;
    }

    public void setNextDistance(int mNextDistance) {
        this.mNextDistance = mNextDistance;
    }

    public boolean isRightPassArea() {
        return isRightPassArea;
    }

    public void setRightPassArea(boolean rightPassArea) {
        isRightPassArea = rightPassArea;
    }

    public int getActionIcon() {
        return mActionIcon;
    }

    public void setActionIcon(int mActionIcon) {
        this.mActionIcon = mActionIcon;
    }

    public int getNavigtionAction() {
        return mNavigtionAction;
    }

    public void setNavigtionAction(int mNavigtionAction) {
        this.mNavigtionAction = mNavigtionAction;
    }

    public int getGroupActionIcon() {
        return mGroupActionIcon;
    }

    public void setGroupActionIcon(int mGroupActionIcon) {
        this.mGroupActionIcon = mGroupActionIcon;
    }

    public byte getAciontType() {
        return mAciontType;
    }

    public void setAciontType(byte mAciontType) {
        this.mAciontType = mAciontType;
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int mIndex) {
        this.mIndex = mIndex;
    }

    public String getActionDes() {
        return mActionDes;
    }

    public void setActionDes(String mActionDes) {
        this.mActionDes = mActionDes;
    }

    public String getGroupDes() {
        return mGroupDes;
    }

    public void setGroupDes(String mGroupDes) {
        this.mGroupDes = mGroupDes;
    }

    public String getGroupTrafficDes() {
        return mGroupTrafficDes;
    }

    public void setGroupTrafficDes(String mGroupTrafficDes) {
        this.mGroupTrafficDes = mGroupTrafficDes;
    }

    public boolean isSubListExpand() {
        return isSubListExpand;
    }

    public void setSubListExpand(boolean subListExpand) {
        isSubListExpand = subListExpand;
    }

    public List<SubItem> getSubList() {
        return mSubList;
    }

    public void setSubList(List<SubItem> mSubList) {
        this.mSubList = mSubList;
    }

    /**
     * 设置是否被选中
     *
     * @param select
     */
    public void setSelect(boolean select) {
        mIsSelect = select;
    }

    /**
     * 是否被选中
     *
     * @return
     */
    public boolean isSelect() {
        return mIsSelect;
    }

    /**
     * 聚合线路信息
     */
    public static class SubItem implements Parcelable {
        private int stationIndex;
        private String distanceDes;
        private boolean isRightPassArea;
        /**
         * 子路线详情箭头图标
         */
        private int actionIcon;
        /**
         * 父路线详情箭头图标
         */
        private int groupActionIcon;
        private int navigtionAction;
        private byte aciontType;
        private String actionDes;

        public int getNavigtionAction() {
            return navigtionAction;
        }

        public void setNavigtionAction(int navigtionAction) {
            this.navigtionAction = navigtionAction;
        }

        private ArrayList<Long> routeLinks;

        private ArrayList<Coord2DInt32> routelinkPoints;

        public int getStationIndex() {
            return stationIndex;
        }

        public void setStationIndex(int stationIndex) {
            this.stationIndex = stationIndex;
        }

        public String getDistanceDes() {
            return distanceDes;
        }

        public void setDistanceDes(String distanceDes) {
            this.distanceDes = distanceDes;
        }

        public boolean isRightPassArea() {
            return isRightPassArea;
        }

        public void setRightPassArea(boolean rightPassArea) {
            isRightPassArea = rightPassArea;
        }

        public int getActionIcon() {
            return actionIcon;
        }

        public void setActionIcon(int actionIcon) {
            this.actionIcon = actionIcon;
        }

        public int getGroupActionIcon() {
            return groupActionIcon;
        }

        public void setGroupActionIcon(int groupActionIcon) {
            this.groupActionIcon = groupActionIcon;
        }

        public byte getAciontType() {
            return aciontType;
        }

        public void setAciontType(byte aciontType) {
            this.aciontType = aciontType;
        }

        public String getActionDes() {
            return actionDes;
        }

        public void setActionDes(String actionDes) {
            this.actionDes = actionDes;
        }

        public ArrayList<Long> getRouteLinks() {
            return routeLinks;
        }

        public void setRouteLinks(ArrayList<Long> routeLinks) {
            this.routeLinks = routeLinks;
        }

        public ArrayList<Coord2DInt32> getRoutelinkPoints() {
            return routelinkPoints;
        }

        public void setRoutelinkPoints(ArrayList<Coord2DInt32> routelinkPoints) {
            this.routelinkPoints = routelinkPoints;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.stationIndex);
            dest.writeString(this.distanceDes);
            dest.writeByte(this.isRightPassArea ? (byte) 1 : (byte) 0);
            dest.writeInt(this.actionIcon);
            dest.writeInt(this.groupActionIcon);
            dest.writeInt(this.navigtionAction);
            dest.writeByte(this.aciontType);
            dest.writeString(this.actionDes);
            dest.writeList(this.routeLinks);
            dest.writeList(this.routelinkPoints);
        }

        public void readFromParcel(Parcel source) {
            this.stationIndex = source.readInt();
            this.distanceDes = source.readString();
            this.isRightPassArea = source.readByte() != 0;
            this.actionIcon = source.readInt();
            this.groupActionIcon = source.readInt();
            this.navigtionAction = source.readInt();
            this.aciontType = source.readByte();
            this.actionDes = source.readString();
            this.routeLinks = new ArrayList<Long>();
            source.readList(this.routeLinks, Long.class.getClassLoader());
            this.routelinkPoints = new ArrayList<Coord2DInt32>();
            source.readList(this.routelinkPoints, Coord2DInt32.class.getClassLoader());
        }

        public SubItem() {
        }

        protected SubItem(Parcel in) {
            this.stationIndex = in.readInt();
            this.distanceDes = in.readString();
            this.isRightPassArea = in.readByte() != 0;
            this.actionIcon = in.readInt();
            this.groupActionIcon = in.readInt();
            this.navigtionAction = in.readInt();
            this.aciontType = in.readByte();
            this.actionDes = in.readString();
            this.routeLinks = new ArrayList<Long>();
            in.readList(this.routeLinks, Long.class.getClassLoader());
            this.routelinkPoints = new ArrayList<Coord2DInt32>();
            in.readList(this.routelinkPoints, Coord2DInt32.class.getClassLoader());
        }

        public static final Parcelable.Creator<SubItem> CREATOR = new Parcelable.Creator<SubItem>() {
            @Override
            public SubItem createFromParcel(Parcel source) {
                return new SubItem(source);
            }

            @Override
            public SubItem[] newArray(int size) {
                return new SubItem[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mDesType);
        dest.writeString(this.mRoadName);
        dest.writeString(this.mDistanceDes);
        dest.writeInt(this.mNextDistance);
        dest.writeByte(this.isRightPassArea ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mActionIcon);
        dest.writeInt(this.mGroupActionIcon);
        dest.writeInt(this.mNavigtionAction);
        dest.writeByte(this.mAciontType);
        dest.writeInt(this.mIndex);
        dest.writeString(this.mActionDes);
        dest.writeString(this.mGroupDes);
        dest.writeString(this.mGroupTrafficDes);
        dest.writeByte(this.isSubListExpand ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.mSubList);
        dest.writeByte(this.mIsSelect ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.mDesType = source.readInt();
        this.mRoadName = source.readString();
        this.mDistanceDes = source.readString();
        this.mNextDistance = source.readInt();
        this.isRightPassArea = source.readByte() != 0;
        this.mActionIcon = source.readInt();
        this.mGroupActionIcon = source.readInt();
        this.mNavigtionAction = source.readInt();
        this.mAciontType = source.readByte();
        this.mIndex = source.readInt();
        this.mActionDes = source.readString();
        this.mGroupDes = source.readString();
        this.mGroupTrafficDes = source.readString();
        this.isSubListExpand = source.readByte() != 0;
        this.mSubList = source.createTypedArrayList(SubItem.CREATOR);
        this.mIsSelect = source.readByte() != 0;
    }

    protected NaviStationItemData(Parcel in) {
        this.mDesType = in.readInt();
        this.mRoadName = in.readString();
        this.mDistanceDes = in.readString();
        this.mNextDistance = in.readInt();
        this.isRightPassArea = in.readByte() != 0;
        this.mActionIcon = in.readInt();
        this.mGroupActionIcon = in.readInt();
        this.mNavigtionAction = in.readInt();
        this.mAciontType = in.readByte();
        this.mIndex = in.readInt();
        this.mActionDes = in.readString();
        this.mGroupDes = in.readString();
        this.mGroupTrafficDes = in.readString();
        this.isSubListExpand = in.readByte() != 0;
        this.mSubList = in.createTypedArrayList(SubItem.CREATOR);
        this.mIsSelect = in.readByte() != 0;
    }

    public static final Parcelable.Creator<NaviStationItemData> CREATOR = new Parcelable.Creator<NaviStationItemData>() {
        @Override
        public NaviStationItemData createFromParcel(Parcel source) {
            return new NaviStationItemData(source);
        }

        @Override
        public NaviStationItemData[] newArray(int size) {
            return new NaviStationItemData[size];
        }
    };
}
