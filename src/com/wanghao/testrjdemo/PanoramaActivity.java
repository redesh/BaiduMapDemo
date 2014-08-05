package com.wanghao.testrjdemo;

import android.app.Activity;
import android.content.Intent;
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.controller.PanoramaController;
import com.baidu.lbsapi.panoramaview.PanoramaView;
import com.baidu.lbsapi.panoramaview.PanoramaViewListener;



public class PanoramaActivity extends Activity implements PanoramaViewListener{

	private PanoramaView mPanoView;
	private TextView txt_panorama;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //œ»≥ı ºªØBMapManager
        DemoApplication app = (DemoApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);

            app.mBMapManager.init(new DemoApplication.MyGeneralListener());
        }
        setContentView(R.layout.panorama_view);
        txt_panorama = (TextView)findViewById(R.id.txt_panorama);
        mPanoView = (PanoramaView)findViewById(R.id.panorama);
        mPanoView.setPanoramaImageLevel(5);
    	mPanoView.setPanoramaViewListener(this);
    	
    	mPanoView.setShowTopoLink(true);
        mPanoView.setZoomGestureEnabled(true);
        mPanoView.setRotateGestureEnabled(true);
         
        Intent intent = getIntent();
        mPanoView.setPanorama(intent.getDoubleExtra("longitude",121.534878),intent.getDoubleExtra("latitude",38.89336));
        String address = intent.getStringExtra("address");
        if(TextUtils.isEmpty(address)){
        	txt_panorama.setVisibility(View.GONE);
        }else{
        	txt_panorama.setText(address);
        }
         
	}

	@Override
	public void onLoadPanoramBegin() {
		
	}

	@Override
	public void onLoadPanoramaEnd() {
		
	}

	@Override
	public void onLoadPanoramaError() {
		
	}
    
    @Override
    protected void onPause() {
        super.onPause();
        mPanoView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPanoView.onResume();
    }

    @Override
    protected void onDestroy() {
        mPanoView.destroy();
        super.onDestroy();
    }
	
}
