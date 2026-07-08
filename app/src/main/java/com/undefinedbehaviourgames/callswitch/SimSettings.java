package com.undefinedbehaviourgames.callswitch;

import java.io.Serializable;

public class SimSettings implements Serializable {
    private PreferredSimSettings mSettings;
    private int mSimIndex;

    public SimSettings(PreferredSimSettings settings, int simIndex) {
        mSimIndex = simIndex;
        mSettings = settings;
    }

    public PreferredSimSettings getSettings() {
        return mSettings;
    }

    public void setSettings(PreferredSimSettings settings) {
        mSettings = settings;
    }

    public int getSimIndex() {
        return mSimIndex;
    }

    public void setSimIndex(int simIndex) {
        mSimIndex = simIndex;
    }
}
