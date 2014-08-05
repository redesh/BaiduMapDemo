package com.wanghao.testrjdemo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import com.baidu.mapapi.SDKInitializer;



public class DemoApplication extends Application{
	
    private static DemoApplication mInstance = null;
    BMapManager mBMapManager = null;

	@Override
    public void onCreate() {
	    super.onCreate();
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);
		mInstance = this;
		initEngineManager(this);
	}
	
	public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(DemoApplication.getInstance().getApplicationContext(), 
                    "BMapManager  初始化错误", Toast.LENGTH_LONG).show();
        }
	}
	
	public static DemoApplication getInstance() {
		return mInstance;
	}
	
	
	// 常用事件监听，用来处理异常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {
        
        @Override
        public void onGetPermissionState(int iError) {
        	//非零值表示key验证未通过
            if (iError != 0) {
                //授权Key错误
                Toast.makeText(DemoApplication.getInstance().getApplicationContext(), 
                        "请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: "+iError, Toast.LENGTH_LONG).show();
            }
        }
    }
}
