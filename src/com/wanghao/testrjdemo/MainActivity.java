package com.wanghao.testrjdemo;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfigeration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.LatLngBounds.Builder;
import com.baidu.mapapi.overlayutil.DrivingRouteOvelray;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.TransitRouteOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;


public class MainActivity extends FragmentActivity implements OnGetRoutePlanResultListener, OnClickListener, OnGetPoiSearchResultListener{

	private BaiduMap mBaiduMap=null;
	private static final String LTAG = MainActivity.class.getSimpleName();
	// ��λ���
	LocationClient mLocClient;
    //�������
    private RoutePlanSearch rt_Search = null;// ����ģ�飬Ҳ��ȥ����ͼģ�����ʹ��
    //Poi����
    private PoiSearch mPoiSearch = null;
    private int load_Index = 0;
	public MyLocationListenner myListener = new MyLocationListenner();
	boolean isFirstLoc = true;// �Ƿ��״ζ�λ
	private LocationMode mCurrentMode;
	private BitmapDescriptor mCurrentMarker;
	private MapStatus mapStatus;
	private SupportMapFragment map;
	private PopupWindow popuproute;
	private LatLngBounds.Builder builder;
	private int nodeIndex = -2;//�ڵ�����,������ڵ�ʱʹ��
	private RouteLine route = null;
	private OverlayManager routeOverlay = null;
	private PlanNode stNode;
	private PlanNode enNode;
	private LatLng st_ll;
	private LatLng en_ll;
	private String st_address;
	private String city;
	private double en_latitude = 38.89336;
	private double en_longitude = 121.534878;
	private String poi_key = "bus";
	private GridView grid_control;
	private int position;
	private int reposition=1000;
	
	/**
	 * ����㲥�����࣬���� SDK key ��֤�Լ������쳣�㲥
	 */
	public class SDKReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			String s = intent.getAction();
			Log.d(LTAG, "action: " + s);
			if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
				Toast.makeText(getApplicationContext(),"key ��֤����! ���� AndroidManifest.xml �ļ��м�� key ����", Toast.LENGTH_LONG).show();
			} else if (s
					.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {  
				Toast.makeText(getApplicationContext(),"�������", Toast.LENGTH_LONG).show();
			}
		}
	}
	private SDKReceiver mReceiver;
	
	//������Ϣ
	private static final GriditemInfo[] griditemInfos = {
		new GriditemInfo(R.id.icon_bus,R.drawable.unselected_bus,R.drawable.icon_bus_selected,"����","bus"),
		new GriditemInfo(R.id.icon_food,R.drawable.unselected_food,R.drawable.icon_food_selected,"����","food"),
		new GriditemInfo(R.id.icon_hospital,R.drawable.unselected_hospital,R.drawable.icon_hospital_selected,"ҽԺ","hospital"),
		new GriditemInfo(R.id.icon_school,R.drawable.unselected_school,R.drawable.icon_school_selected,"ѧУ","school"),
		new GriditemInfo(R.id.icon_shop,R.drawable.unselected_shop,R.drawable.icon_shop_selected,"����","shop")
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		// ע�� SDK �㲥������
 		IntentFilter iFilter = new IntentFilter();
 		iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
 		iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
 		mReceiver = new SDKReceiver();
 		registerReceiver(mReceiver, iFilter);
		mCurrentMode = LocationMode.NORMAL;
		//���õ���
		for(int i=0;i<griditemInfos.length;i++){
			((ImageView)findViewById(griditemInfos[i].img_id)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					for(int si=0;si<griditemInfos.length;si++){
						if(v.getId()==griditemInfos[si].img_id) position=si;
					}
					((ImageView)findViewById(griditemInfos[position].img_id)).setImageResource(griditemInfos[position].img_selected);
					if(reposition<griditemInfos.length)
						((ImageView)findViewById(griditemInfos[reposition].img_id)).setImageResource(griditemInfos[reposition].img_unselected);
					reposition = position;
					mBaiduMap.clear();
					mPoiSearch.searchNearby((new PoiNearbySearchOption())
							.location(st_ll)
							.radius(2000)
							.keyword(griditemInfos[position].img_keyword));
					poi_key = griditemInfos[position].img_poikey;
				}
			});
		}
		
		MapStatus ms = new MapStatus.Builder().overlook(-20).zoom(14).build();
		BaiduMapOptions bo = new BaiduMapOptions().mapStatus(ms)
				.compassEnabled(false).zoomControlsEnabled(false);
		map = SupportMapFragment.newInstance(bo);
		FragmentManager manager = getSupportFragmentManager();
		manager.beginTransaction().add(R.id.mMapView, map, "map_fragment").commit();

 		en_ll = new LatLng(en_latitude,en_longitude);
 		// ��ʼ������ģ�飬ע���¼�����
 		rt_Search = RoutePlanSearch.newInstance();
 		rt_Search.setOnGetRoutePlanResultListener(this);
 		// ��ʼ������ģ�飬ע��Poi�����¼�����
		mPoiSearch = PoiSearch.newInstance();
		mPoiSearch.setOnGetPoiSearchResultListener(this);
 		//����·�߰�ť�¼�
 		findViewById(R.id.btn_route).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(reposition<griditemInfos.length)
					((ImageView)findViewById(griditemInfos[reposition].img_id)).setImageResource(griditemInfos[reposition].img_unselected);
				initPopUpWindow();
				//���·�߰�ť�����Ĵ���
				popuproute.showAtLocation(findViewById(R.id.mMapView),Gravity.CENTER, 0, 0);
			}
		});
 		//����ȫ���¼�
 		findViewById(R.id.btn_panorama).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(reposition<griditemInfos.length)
					((ImageView)findViewById(griditemInfos[reposition].img_id)).setImageResource(griditemInfos[reposition].img_unselected);
				Intent intent = new Intent(MainActivity.this,PanoramaActivity.class);
				intent.putExtra("latitude", st_ll.latitude);
				intent.putExtra("longitude", st_ll.longitude);
				intent.putExtra("address", st_address);
				startActivity(intent);
			}
		});
 		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//��ȡ��ͼ�ؼ�����  
		mBaiduMap = map.getBaiduMap();
		// ������λͼ��
		mBaiduMap.setMyLocationEnabled(true);
		builder = new Builder();
		//���Ӷ����ǩ
		addMarker();
		// ��λ��ʼ��
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(com.baidu.location.LocationClientOption.LocationMode.Battery_Saving);//���ֶ�λģʽ�£���ͬʱʹ�����綨λ��GPS��λ�����ȷ�����߾��ȵĶ�λ�����
		option.setOpenGps(true); //��GPS  
		option.setIsNeedAddress(true);//���صĶ�λ���������ַ��Ϣ  ,������Ϊtrue�����û�ȡ����
		option.setCoorType("bd09ll");//���صĶ�λ����ǰٶȾ�γ��,Ĭ��ֵgcj02  
		option.setScanSpan(5000); //���÷���λ����ļ��ʱ��Ϊ5000ms 
		mLocClient.setLocOption(option);
		mLocClient.start();
		//�������յ���Ϣ������tranist search ��˵��������������
		stNode = PlanNode.withLocation(st_ll);
		enNode = PlanNode.withLocation(en_ll);
	}

	/**
	 * ��ʼ��PopUpWindow
	 */
	private void initPopUpWindow() {
		View popup_route = getLayoutInflater().inflate(R.layout.popup_route, null);
 		popuproute = new PopupWindow(popup_route, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,true);
 		popuproute.setTouchable(true);
 		popuproute.setOutsideTouchable(true);
 		popuproute.setBackgroundDrawable(new BitmapDrawable(getResources(),(Bitmap)null));
 		popup_route.findViewById(R.id.btn_bus).setOnClickListener(this);
 		popup_route.findViewById(R.id.btn_drive).setOnClickListener(this);
 		popup_route.findViewById(R.id.btn_walk).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		mBaiduMap.clear();
		//�������յ���Ϣ������tranist search ��˵��������������
		stNode = PlanNode.withLocation(st_ll);
		enNode = PlanNode.withLocation(en_ll);
		switch(v.getId()){
			case R.id.btn_bus:
				rt_Search.transitSearch((new TransitRoutePlanOption())
	                    .from(stNode)
	                    .city(city)
	                    .to(enNode));
				popuproute.dismiss();
				break;
			case R.id.btn_drive:
				rt_Search.drivingSearch((new DrivingRoutePlanOption())
	                    .from(stNode)
	                    .to(enNode));
				popuproute.dismiss();
				break;
			case R.id.btn_walk:
				rt_Search.walkingSearch((new WalkingRoutePlanOption())
		                    .from(stNode)
		                    .to(enNode));
				popuproute.dismiss();
				break;
			default:
				break;
		}
		
	}

	private void addMarker() {
		//����Maker�����  �мǾ�γ�Ȳ�Ҫд���ˡ�γ�ȣ�����
		LatLng point = new LatLng(en_latitude, en_longitude);  
		//����Markerͼ��  
		BitmapDescriptor bitmap = BitmapDescriptorFactory  
		    .fromResource(R.drawable.icon_marker);  
		//����MarkerOption�������ڵ�ͼ�����Marker  
		OverlayOptions option = new MarkerOptions()  
		    .position(point)  
		    .icon(bitmap);  
		//�ڵ�ͼ�����Marker������ʾ  
		mBaiduMap.addOverlay(option);
		builder.include(point);
	}

	/**
	 * ��λSDK��������
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view ���ٺ��ڴ����½��յ�λ��
			if (location == null || map == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// �˴����ÿ����߻�ȡ���ķ�����Ϣ��˳ʱ��0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			if (isFirstLoc) {
				city = location.getCity();
				isFirstLoc = false;
				st_ll = new LatLng(location.getLatitude(),location.getLongitude());
				if(location.hasAddr())
					st_address = location.getAddrStr();
				builder.include(st_ll);
				LatLngBounds bounds = builder.build();
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(bounds);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	public void onGetDrivingRouteResult(DrivingRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
        	//���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
        	result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            route = result.getRouteLines().get(0);
            DrivingRouteOvelray overlay = new MyDrivingRouteOverlay(mBaiduMap);
            routeOverlay = overlay;
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
        
	}

	@Override
	public void onGetTransitRouteResult(TransitRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
            result.getSuggestAddrInfo();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            route = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new MyTransitRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
	}

	@Override
	public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //���յ��;�����ַ����壬ͨ�����½ӿڻ�ȡ�����ѯ��Ϣ
            //result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();
            overlay.zoomToSpan();
        }
	}
	
	//����RouteOverly
    private class MyDrivingRouteOverlay extends DrivingRouteOvelray {

        public MyDrivingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        public MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }

    private class MyTransitRouteOverlay extends TransitRouteOverlay {

        public MyTransitRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }
    }

	@Override
	public void onGetPoiDetailResult(PoiDetailResult result) {
		if (result.error != SearchResult.ERRORNO.NO_ERROR) {
			Toast.makeText(MainActivity.this, "��Ǹ��δ�ҵ����", Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onGetPoiResult(PoiResult result) {
		if (result == null
				|| result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
			return;
		}
		if (result.error == SearchResult.ERRORNO.NO_ERROR) {
			mBaiduMap.clear();
			MpoiOverlay overlay = new MpoiOverlay(mBaiduMap);
			mBaiduMap.setOnMarkerClickListener(overlay);
			overlay.setPoiResult(result);
			overlay.addToMap();
			overlay.zoomToSpan();
			return;
		}
		if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

			Toast.makeText(MainActivity.this, "�ڸ�����û����ؽ��", Toast.LENGTH_LONG)
					.show();
		}
	}

	private class MpoiOverlay extends OverlayManager{
		private PoiResult poiResult;
		
		public void setPoiResult(PoiResult poiResult){
			this.poiResult = poiResult;
		}
		
		public MpoiOverlay(BaiduMap baiduMap) {
			super(baiduMap);
		}

		@Override
		public boolean onMarkerClick(Marker marker) {
			PoiInfo poiInfo = poiResult.getAllPoi().get(marker.getZIndex());
			if (poiInfo.hasCaterDetails) {
				mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
						.poiUid(poiInfo.uid));
			}else{
				Toast.makeText(MainActivity.this, poiInfo.name, Toast.LENGTH_SHORT).show();
			}
			return true;
		}

		@Override
		public List<OverlayOptions> getOverlayOptions() {
			List<OverlayOptions> ops = new ArrayList<OverlayOptions>();
            List<PoiInfo> pois = poiResult.getAllPoi();
            int icon_id = R.drawable.icon_buspoint;
            switch(poi_key){
	            case "bus":
	            	icon_id=R.drawable.icon_buspoint;
	            	break;
	            case "food":
	            	icon_id=R.drawable.icon_foodpoint;
	            	break;
	            case "hospital":
	            	icon_id=R.drawable.icon_hospitalpoint;
	            	break;
	            case "school":
	            	icon_id=R.drawable.icon_schoolpoint;
	            	break;
	            case "shop":
	            	icon_id=R.drawable.icon_shoppoint;
	            	break;
            }
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(icon_id);
            for(int i = 0;i < pois.size();i++){
                    OverlayOptions op = new MarkerOptions().position(pois.get(i).location).icon(bitmap).zIndex(i);
                    ops.add(op);
                    mBaiduMap.addOverlay(op);
            }
            return ops;
		}
	}
	
	private static class GriditemInfo{
		private final int img_id;
		private final int img_unselected;
		private final int img_selected;
		private final String img_keyword;
		private final String img_poikey;
		public GriditemInfo(int img_id, int img_unselected, int img_selected,
				String img_keyword, String img_poikey) {
			this.img_id = img_id;
			this.img_unselected = img_unselected;
			this.img_selected = img_selected;
			this.img_keyword = img_keyword;
			this.img_poikey = img_poikey;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// �˳�ʱ���ٶ�λ
		mLocClient.stop();
		// �رն�λͼ��
		mBaiduMap.setMyLocationEnabled(false);
		//�ͷż���ʵ��
		rt_Search.destroy();
		mPoiSearch.destroy();
		DemoApplication app = (DemoApplication)this.getApplication();
			if (app.mBMapManager != null) {
				app.mBMapManager.destroy();
				app.mBMapManager = null;
			}
		super.onDestroy();
		
	}
}
