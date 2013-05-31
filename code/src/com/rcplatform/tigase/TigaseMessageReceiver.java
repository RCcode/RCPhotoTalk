package com.rcplatform.tigase;


public interface TigaseMessageReceiver {

	boolean onMessageHandle(String msg,String from);
}
