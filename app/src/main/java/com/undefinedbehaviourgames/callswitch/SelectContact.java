package com.undefinedbehaviourgames.callswitch;

import java.io.Serializable;

public class SelectContact extends Contact implements Serializable {

    public boolean mChecked;

    public SelectContact() {
        super();
        mChecked = false;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }
}
