package com.rcplatform.phototalk.bean;

public class Information {

    protected String recordId;

    protected String noticeId;

    protected long createtime;

    protected long lastUpdateTime;

    protected int type; // 发送图片，接收图片，通知

    protected int statu;

    protected int limitTime;

    protected String url;

    protected int notifyStatu;

    protected RecordUser sender;

    protected RecordUser receiver;

    protected boolean opened = false;

    protected boolean showing = false;

    protected boolean destroyed = false;

    protected boolean isLoaded = false;

    protected boolean loading = false;

    public Information() {
        // TODO Auto-generated constructor stub
    }

    public Information(String id, int statu, long lastUpdateTime) {
        this.recordId = id;
        this.statu = statu;
        this.lastUpdateTime = lastUpdateTime;
    }

    public int getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(int limitTime) {
        this.limitTime = limitTime;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatu() {
        return statu;
    }

    public void setStatu(int statu) {
        this.statu = statu;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNotifyStatu() {
        return notifyStatu;
    }

    public void setNotifyStatu(int notifyStatu) {
        this.notifyStatu = notifyStatu;
    }

    public RecordUser getSender() {
        return sender;
    }

    public void setSender(RecordUser own) {
        this.sender = own;
    }

    public RecordUser getReceiver() {
        return receiver;
    }

    public void setReceiver(RecordUser receiver) {
        this.receiver = receiver;
    }

    public boolean isShowing() {
        return showing;
    }

    public void setShowing(boolean showing) {
        this.showing = showing;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
        if (destroyed)
            showing = false;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
        if (opened)
            setShowing(true);
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public String getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Information))
            return false;
        Information info = (Information) o;
        return this.recordId.equals(info.getRecordId()) && this.type == info.getType();

    }

    @Override
    public String toString() {
        return "InfoRecord [recordId=" + recordId + ", limitTime=" + limitTime + ", opened=" + opened + ", showing=" + showing + ", destroyed="
                + destroyed + "]";
    }

}
