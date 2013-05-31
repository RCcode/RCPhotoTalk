package com.rcplatform.tigase;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class TigaseMessageBinderService extends Service {

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	TigaseMessageBinderService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TigaseMessageBinderService.this;
        }
    }
    
	@Override
    public IBinder onBind(Intent intent) {
	    // TODO Auto-generated method stub
	    return mBinder;
    }
	
	//发送消息至tagise
	public void sendMessage(String msg,String toUser,String toRcID, String action){
		
	}
	
	//注册接收消息监听器
	public void setOnMessageReciver(TigaseMessageReceiver messageReceiver){
		
	}

}