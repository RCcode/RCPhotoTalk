package com.rcplatform.phototalk.bean;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-6 下午02:36:50
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class FriendChat extends Friend {

    private String letter;

    private int position;

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public int getPostion() {
        return position;
    }

    public void setPostion(int position) {
        this.position = position;
    }
}
