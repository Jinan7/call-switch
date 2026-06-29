package com.undefinedbehaviourgames.callswitch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReplyLab {

    private static ReplyLab sReplyLab;
    private List<Reply> mReplies;
    private ReplyLab() {
        mReplies = new ArrayList<>();
    };

    public static ReplyLab getInstance() {
        if (sReplyLab == null) {
            sReplyLab = new ReplyLab();
        }

        return sReplyLab;
    }

    public Reply get(UUID id) {

        for (Reply reply : mReplies) {
            if (reply.getId().equals(id)) {
                return reply;
            }
        }

        return null;
    }
    public List<Reply> getReplies() {
        return mReplies;
    }

    public void add(Reply reply) {
        mReplies.add(reply);
    }
    public void update(Reply reply) {}
}
