package com.undefinedbehaviourgames.callswitch;

import java.util.ArrayList;
import java.util.List;

public class ReplyLab {

    private static ReplyLab sReplyLab;
    private List<Reply> mReplies;
    private ReplyLab() {
        mReplies = new ArrayList<>();
    };

    private static ReplyLab getInstance() {
        if (sReplyLab == null) {
            sReplyLab = new ReplyLab();
        }

        return sReplyLab;
    }

    public List<Reply> getReplies() {
        return mReplies;
    }

    public void addReply(Reply reply) {
        mReplies.add(reply);
    }
}
