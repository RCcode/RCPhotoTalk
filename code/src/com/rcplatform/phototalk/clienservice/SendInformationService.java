package com.rcplatform.phototalk.clienservice;

import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.logic.LogicUtils;

import android.app.IntentService;
import android.content.Intent;

public class SendInformationService extends IntentService {
	private static final String SERVICE_NAME="sendInformation";
	
	public SendInformationService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
	}

}
