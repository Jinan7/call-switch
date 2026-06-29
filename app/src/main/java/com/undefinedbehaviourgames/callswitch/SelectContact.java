package com.undefinedbehaviourgames.callswitch;

public class SelectContact extends Contact {

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
