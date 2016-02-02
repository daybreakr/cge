
package com.cmge.cge.sdk.info;

import android.os.Parcel;
import android.os.Parcelable;

public class CgeOrderInfo implements Parcelable {

    public String orderId;
    public String channelOrderId;
    public String status;

    public CgeOrderInfo() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.orderId);
        dest.writeString(this.channelOrderId);
        dest.writeString(this.status);
    }

    public static final Parcelable.Creator<CgeOrderInfo> CREATOR = new Creator<CgeOrderInfo>() {

        @Override
        public CgeOrderInfo[] newArray(int size) {
            return new CgeOrderInfo[size];
        }

        @Override
        public CgeOrderInfo createFromParcel(Parcel source) {
            CgeOrderInfo orderInfo = new CgeOrderInfo();
            orderInfo.orderId = source.readString();
            orderInfo.channelOrderId = source.readString();
            orderInfo.status = source.readString();
            return orderInfo;
        }
    };

    @Override
    public String toString() {
        return "OrderInfo"
                + "[ orderId:" + orderId
                + ", channelOrderId:" + channelOrderId
                + ", status:" + status
                + "]";
    }
}
