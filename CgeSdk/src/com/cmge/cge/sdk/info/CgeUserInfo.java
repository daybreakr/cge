
package com.cmge.cge.sdk.info;

import android.os.Parcel;
import android.os.Parcelable;

public class CgeUserInfo implements Parcelable {

    public String id;
    public String name;
    public String token;
    public String extend;

    public CgeUserInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.token);
        dest.writeString(this.extend);
    }
    
    public static final Parcelable.Creator<CgeUserInfo> CREATOR = new Creator<CgeUserInfo>()  {
        
        @Override
        public CgeUserInfo[] newArray(int size) {
            return new CgeUserInfo[size];
        }
        
        @Override
        public CgeUserInfo createFromParcel(Parcel source) {
            CgeUserInfo userInfo = new CgeUserInfo();
            userInfo.id = source.readString();
            userInfo.name = source.readString();
            userInfo.token = source.readString();
            userInfo.extend = source.readString();
            return userInfo;
        }
    };
    
    @Override
    public String toString() {
        return "UserInfo"
                + "[ id:" + id
                + ", name:" + name
                + ", token:" + token
                + ", extend:" + extend
                + "]";
    }
}
