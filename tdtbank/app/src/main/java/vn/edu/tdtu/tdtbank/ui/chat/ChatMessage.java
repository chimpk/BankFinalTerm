package vn.edu.tdtu.tdtbank.ui.chat;

import android.os.Bundle;

/** Tin nhắn trong màn hình chat; có thể kèm một nút điều hướng tới chức năng liên quan. */
public class ChatMessage {

    public final String text;
    public final boolean fromUser;
    public final int actionLabelRes;
    public final int destinationId;
    public final Bundle args;

    public ChatMessage(String text, boolean fromUser) {
        this(text, fromUser, 0, 0, null);
    }

    public ChatMessage(String text, boolean fromUser, int actionLabelRes, int destinationId, Bundle args) {
        this.text = text;
        this.fromUser = fromUser;
        this.actionLabelRes = actionLabelRes;
        this.destinationId = destinationId;
        this.args = args;
    }

    public boolean hasAction() {
        return destinationId != 0;
    }
}
