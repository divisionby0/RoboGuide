package dev.div0.roboguide;

import android.os.Parcel;
import android.os.Parcelable;

// using bundle.putParcelable("tag_here", setting);


public class TTLVolume implements Parcelable {
    protected TTLVolume(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TTLVolume> CREATOR = new Creator<TTLVolume>() {
        @Override
        public TTLVolume createFromParcel(Parcel in) {
            return new TTLVolume(in);
        }

        @Override
        public TTLVolume[] newArray(int size) {
            return new TTLVolume[size];
        }
    };
}
