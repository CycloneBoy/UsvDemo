package com.cycloneboy.usvdemo.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.favorite.FavoriteManager;
import com.baidu.mapapi.favorite.FavoritePoiInfo;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.*;
import com.cycloneboy.usvdemo.R;
import com.cycloneboy.usvdemo.algo.AcoPath;
import com.cycloneboy.usvdemo.algo.MinimumBoundingPolygon;
import com.cycloneboy.usvdemo.algo.Point;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class UsvBaseMap extends Activity implements OnGetGeoCoderResultListener,
        OnMapLongClickListener, OnMarkerClickListener, OnMapClickListener {
	//地图相关
	GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用
    BaiduMap mBaiduMap = null;  //百度地图
    MapView mMapView = null;    //百度地图视图
    
    // 定位相关
    LocationClient mLocClient;
    public MyLocationListenner myListener = new MyLocationListenner();
    private LocationMode mCurrentMode;  //定位模式
    BitmapDescriptor mCurrentMarker;   //定位标签
    private static final int accuracyCircleFillColor = 0xAAFFFF88;
    private static final int accuracyCircleStrokeColor = 0xAA00FF00;

    // UI相关
    OnCheckedChangeListener radioButtonListener;
    Button requestLocButton;  //需要定位按钮
    boolean isFirstLoc = true; // 是否首次定位
    
    //Marker相关
	String strMarkerLatj,strMarkerLngw;
    String strMarkerType;
	String strMarkerDegree;
	FavoritePoiInfo info ;
	
	private View mPop;	//UI界面
	private View mModify;
	EditText mdifyName;
	// 保存点中的点id
    private String currentID;
    // 现实marker的图标
    BitmapDescriptor bdObstale = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_gcoding);//障碍物图标
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);
    
    BitmapDescriptor logoP = BitmapDescriptorFactory    //三种污染源图标
            .fromResource(R.drawable.logop); 
    BitmapDescriptor logoY = BitmapDescriptorFactory
            .fromResource(R.drawable.logoy);
    BitmapDescriptor logoR = BitmapDescriptorFactory
            .fromResource(R.drawable.logor);
    
    BitmapDescriptor logoStart = BitmapDescriptorFactory //航行起点图标
            .fromResource(R.drawable.icon_st);
    BitmapDescriptor logoEnd = BitmapDescriptorFactory   //航行终点图标
            .fromResource(R.drawable.icon_en);
    
    List<Marker> markers = new ArrayList<Marker>();
    int nMarkerNum=0;
   
    //避碰相关   2017.04.13 10:36添加
    List<FavoritePoiInfo> obstaclePoiInfo;//障碍物坐标点
    List<FavoritePoiInfo> pollutionPoiInfo;//障碍物坐标点
    // 普通折线，点击时改变宽度
    Polyline mPolyline;
    Polyline mPolylineTest;
    
    //避碰相关
    List<LatLng> mPointObstacles = new ArrayList<LatLng>();//绘制线条的数据
    List<LatLng> mGraphCorner = new ArrayList<LatLng>();//地图四个角落
    int[][] mGraphIntCorner = new int[4][2];//地图边界映射到栅格地图
    double mLngjLength,mLatwLength;
    public static final int GRAID_RADIUS = 10; //问题规模
    int[][] graph = new int[GRAID_RADIUS][GRAID_RADIUS];//初始化地图20*20个栅格
    LatLng startPoint ;//航行起点坐标
    LatLng endPoint ; //航行终点坐标
    int startPointNum=0;//航行起点个数
    int endPointNum=0;//航行终点个数
   
    
    // 普通折线，蚁群算法计算出来的最优路径
    Polyline mBestTourPolyline;
    int[] bestTour;//最优路径
    double bestTourLength = 0;//最优路径长度
    List<LatLng> pointsToShowTour = new ArrayList<LatLng>();  //待行走的最优路径
    
    //行走路径
    private Marker mMoveMarker;
    private Handler mHandler;
    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 80;
    private static final double DISTANCE = 0.00009;// 0.00002;
    
    //行走图标行走多少次标志位
    private int moveMarkerflagToMove =0;
    
    Thread threadMove;//图标行走线程
    private int threadFlagOne=0;
    
    //污染源相关
    //存储所有的污染源
    List<FavoritePoiInfo> poiInfoPollutes = new ArrayList<FavoritePoiInfo>();//污染源收藏夹
    List<LatLng> mPointPollutes = new ArrayList<LatLng>();//存储所有的污染源
    private static final int DISTANCE_POINT = 500;//附近水域500米的污染源
    List<FavoritePoiInfo> poiInfoFindPollutes = new ArrayList<FavoritePoiInfo>();//已经找到的污染源收藏夹
    
    //截屏相关
    Button btnScreenShot;//截图按钮
    private String strScreenShotPath = new String();//最新保存的路径截图
    
    
    //测试数据
    private  LatLng[]  bestPathTourShow = new LatLng[] {
            new LatLng(30.56318115, 114.39806105),
            new LatLng(30.56318115, 114.39523135),
            new LatLng(30.56020205, 114.39240165),
            new LatLng(30.55722295, 114.38957195),
            new LatLng(30.55424385, 114.38674225),
            new LatLng(40.056816, 116.308352),
            new LatLng(40.057997, 116.307725),
            new LatLng(40.058022, 116.307693),
            new LatLng(40.058029, 116.307590),
            new LatLng(40.057913, 116.307119),
            new LatLng(40.057850, 116.306945),
            new LatLng(40.057756, 116.306915),
            new LatLng(40.057225, 116.307164),
            new LatLng(40.056134, 116.307546),
            new LatLng(40.055879, 116.307636),
            new LatLng(40.055826, 116.307697),
    };
    /**
     * 构建函数
     * app初始化函数
     * @author CycloneBoy
     * @param
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usv_base_map);
//        CharSequence titleLable = "Usv主操作界面";
//        setTitle(titleLable);

        requestLocButton = (Button) findViewById(R.id.button1);//跟随普通模式
        mCurrentMode = LocationMode.NORMAL;
        requestLocButton.setText("普通");
        //设置按钮监听按钮响应事件
        requestLocButton.setOnClickListener(new android.view.View.OnClickListener() {

						@Override
			public void onClick(View v) {
							switch (mCurrentMode) {
		                    case NORMAL:
		                        requestLocButton.setText("跟随");
		                        mCurrentMode = LocationMode.FOLLOWING;
		                        mBaiduMap
		                                .setMyLocationConfigeration(new MyLocationConfiguration(
		                                        mCurrentMode, true, mCurrentMarker));
		                        break;
		                    case COMPASS:
		                        requestLocButton.setText("普通");
		                        mCurrentMode = LocationMode.NORMAL;
		                        mBaiduMap
		                                .setMyLocationConfigeration(new MyLocationConfiguration(
		                                        mCurrentMode, true, mCurrentMarker));
		                        break;
		                    case FOLLOWING:
		                        requestLocButton.setText("罗盘");
		                        mCurrentMode = LocationMode.COMPASS;
		                        mBaiduMap
		                                .setMyLocationConfigeration(new MyLocationConfiguration(
		                                        mCurrentMode, true, mCurrentMarker));
		                        break;
		                    default:
		                        break;
		                }

			}
		});

        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup); //定义显示定位图标类型
        radioButtonListener = new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    // 传入null则，恢复默认图标
                    mCurrentMarker = null;
                    mBaiduMap
                            .setMyLocationConfigeration(new MyLocationConfiguration(
                                    mCurrentMode, true, null));
                }
                if (checkedId == R.id.customicon) {
                    // 修改为自定义marker
                    mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_geo);
                    mBaiduMap
                            .setMyLocationConfigeration(new MyLocationConfiguration(
                                    mCurrentMode, true, mCurrentMarker,
                                                    accuracyCircleFillColor, accuracyCircleStrokeColor));
                }
            }
        };
        group.setOnCheckedChangeListener(radioButtonListener);

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);

        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000); //设置定位时间间隔
        mLocClient.setLocOption(option);
        mLocClient.start();

        //marker相关
        mBaiduMap.setOnMapLongClickListener(this);
        mBaiduMap.setOnMarkerClickListener(this);
        mBaiduMap.setOnMapClickListener(this);
        // 初始化收藏夹
        FavoriteManager.getInstance().init();
        	//初始化UI界面
        LayoutInflater mInflater = getLayoutInflater();
        mPop = (View) mInflater.inflate(R.layout.activity_favorite_infowindow, null, false);

       //初始化航行起点和航行终点
        startPointNum = 0;
        endPointNum = 0;

        getAllClick(mMapView);//显示所有坐标点

        //初始化最优路径
        bestTour = new int[GRAID_RADIUS * GRAID_RADIUS + 1];
 	    for (int i = 0; i < GRAID_RADIUS * GRAID_RADIUS + 1; i++) {
 	    	bestTour[i] =0;
		}
 	   bestTour[GRAID_RADIUS * GRAID_RADIUS]=0;
 	   //drawPolyLine();

 	   //路径行走先关
 	   mHandler = new Handler(Looper.getMainLooper());

 	   //屏幕截屏相关
 	   btnScreenShot = (Button) findViewById(R.id.btnSaveScreen);
	}

	/**
	 * 保存截图
	 * @author CycloneBoy
	 */
	public void saveScreenShot(View view){
        // 截图，在SnapshotReadyCallback中保存图片到 sd 卡
        mBaiduMap.snapshot(new SnapshotReadyCallback() {
            public void onSnapshotReady(Bitmap snapshot) {

                File file = new File(strScreenShotPath);

                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    if (snapshot.compress(
                            Bitmap.CompressFormat.PNG, 100, out)) {
                        out.flush();
                        out.close();
                        Log.i("info","截图保存在:"+file.toString(),null);
                    }

                    Toast.makeText(UsvBaseMap.this,
                            "屏幕截图成功，图片存在: " + file.toString(),
                            Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Toast.makeText(UsvBaseMap.this, "正在截取屏幕图片...",
                Toast.LENGTH_SHORT).show();
	}

	 /**
	 * 获取SDCard的目录路径功能
	 * @return
	 */
	private String getSDCardPath(){
		File sdcardDir = null;
		//判断SDCard是否存在
		boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(sdcardExist){
			sdcardDir = Environment.getExternalStorageDirectory();
		}
		return sdcardDir.toString();
	}


	/**
     * 修改收藏点
     *
     * @param v
     */
    public void modifyClick(View v) {
        mBaiduMap.hideInfoWindow();
        // 弹框修改
        LayoutInflater mInflater = getLayoutInflater();
        mModify = (LinearLayout) mInflater.inflate(R.layout.input_modify_marker_dialog, null);

      //装载对话框布局
      	//取得对话框中的输入框对象
      	final EditText mdifyName = (EditText) mModify.findViewById(R.id.etMarkerName);
      	final EditText et_dia_marker_type = (EditText) mModify.findViewById(R.id.etMarkerType);
      	final EditText et_dia_marker_degree = (EditText) mModify.findViewById(R.id.etMarkerDegree);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(mModify);
        String oldName = FavoriteManager.getInstance().getFavPoi(currentID).getPoiName();
        mdifyName.setText(oldName);
    	et_dia_marker_type.setText(String.valueOf(FavoriteManager.getInstance().getFavPoi(currentID).getAddr()));
      	et_dia_marker_degree.setText(String.valueOf(FavoriteManager.getInstance().getFavPoi(currentID).getCityName()));

        builder.setPositiveButton("确认", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = mdifyName.getText().toString();
                String newtype = et_dia_marker_type.getText().toString();
                String newDegree = et_dia_marker_degree.getText().toString();
                if (newName == null || newName.equals("")) {
		            Toast.makeText(UsvBaseMap.this, "名称必填", Toast.LENGTH_LONG)
		                 .show();
		            return;
		        }

				if (newtype.equals("") || newtype.equals("") ) {
					Toast.makeText(UsvBaseMap.this, "坐标点类型和程度不能为空!", Toast.LENGTH_LONG)
					 .show();
					return;
				}

                    // modify
                    FavoritePoiInfo info = FavoriteManager.getInstance().getFavPoi(currentID);
                    info.poiName(newName);
                    info.addr(newtype);
                    info.cityName(newDegree);
                    if (FavoriteManager.getInstance().updateFavPoi(currentID, info)) {
                        Toast.makeText(UsvBaseMap.this, "修改成功", Toast.LENGTH_LONG).show();
                        getAllClick(mMapView);//更新显示插入的坐标
                    }

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    /**
     * 删除一个指定点
     *
     * @param v
     */
    public void deleteOneClick(View v) {
    	String strShow =" ";
    	LatLng tempPoint;
    	FavoritePoiInfo poiInfo;

    	//获取当前收藏的坐标点
    	poiInfo = FavoriteManager.getInstance().getFavPoi(currentID);
    	Log.i("info", "3.删除坐标点,"+poiInfo.getPt().toString(),null);
    	tempPoint = poiInfo.getPt();//获取当前Marker的坐标
    	 //判断是否是航行起点或航行终点
        if (startPointNum ==1 && tempPoint.toString().equals(startPoint.toString())) {
			startPointNum = 0;
			strShow = ",删除航行起点";

			//删除避碰路径
			if (pointsToShowTour.size() >0) {
				pointsToShowTour.clear();//清空路径
				mPolylineTest.remove();
				 moveMarkerflagToMove = 0;
				 //mMoveMarker.setVisible(false);
				 mMoveMarker.remove();
				Log.i("info", "删掉规划的路径",null);

			}

			 Log.i("info", "3.删除航行起点",null);
		}else if (endPointNum == 1 && tempPoint.toString().equals(endPoint.toString())) {
			endPointNum = 0;
			strShow = ",删除航行终点";

			//删除避碰路径
			if (pointsToShowTour.size() >0) {
				pointsToShowTour.clear();//清空路径
				mPolylineTest.remove();
				 moveMarkerflagToMove = 0;
				// mMoveMarker.setVisible(false);
				 mMoveMarker.remove();
				Log.i("info", "删掉规划的路径",null);
			}

			Log.i("info", "3.删除航行终点",null);
		}else {
			Log.i("info", "3.没有进入到删除起点和终点,startPointNum = "+startPointNum+
					" ,endPointNum = " + endPointNum,null);
		}

       //在收藏夹中是否成功删除当前marker
        if (FavoriteManager.getInstance().deleteFavPoi(currentID)) {

            if (markers != null) {
                for (int i = 0; i < markers.size(); i++) {
                    if (markers.get(i).getExtraInfo().getString("id").equals(currentID)) {

                    	markers.get(i).remove();
                        markers.remove(i);
                        mBaiduMap.hideInfoWindow();
                        Toast.makeText(UsvBaseMap.this, "删除点成功"+strShow, Toast.LENGTH_LONG).show();

                        //绘制一次障碍物边界
                        //showObstaclePolygon(mMapView);
                        getAllClick(mMapView);
                        break;
                    }
                }
            }

        } else {
            Toast.makeText(UsvBaseMap.this, "删除点失败", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Marker相关
     * 获取全部收藏点,并显示
     * @param v
     */
    public void getAllClick(View v) {
    	FavoritePoiInfo poiInfo;
    	MarkerOptions option;
    	Bundle b;
    	mBaiduMap.clear();
        List<FavoritePoiInfo> list = FavoriteManager.getInstance().getAllFavPois();

        if (list == null || list.size() == 0) {
            Toast.makeText(UsvBaseMap.this, "没有收藏点", Toast.LENGTH_LONG).show();
            return;
        }
        //获取已经收藏了多少个坐标marker
        nMarkerNum = list.size();//获取已经收藏了多少个坐标marker

        // 绘制在地图
        markers.clear();
        mPointObstacles.clear();//清空障碍物数组，重新添加障碍物数组
        mPointPollutes.clear();//清空污染源位置数组，重新添加新的污染源
        poiInfoPollutes.clear();//清空污染源收藏夹，重新添加新的污染源
        for (int i = 0; i < list.size(); i++) {
        	poiInfo = list.get(i);//获取当前poiInfo
        	 //Log.i("info", "准备添加一个坐标点:"+poiInfo.getPoiName()+" "
             	//	+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);
        	if (poiInfo.getAddr().trim().equals("1")) {	//添加障碍物

        		//添加障碍物点到障碍物数组
        		LatLng tempLatLng = new LatLng(poiInfo.getPt().latitude, poiInfo.getPt().longitude);
        		mPointObstacles.add(tempLatLng);//障碍物链表中添加障碍物

        	    option = new MarkerOptions().icon(bdObstale)
        					.position(poiInfo.getPt()).title(poiInfo.getPoiName());
        	    b = new Bundle();
                b.putString("id", list.get(i).getID());
                option.extraInfo(b);
                markers.add((Marker) mBaiduMap.addOverlay(option));
                //Log.i("info", "add a obatacle:"+poiInfo.getPoiName()+" "
               // 		+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);

                //绘制一次障碍物边界
                //添加完一个障碍物就要重新绘制一遍障碍物边界
                showObstaclePolygon(mMapView);



			} else if (poiInfo.getAddr().trim().equals("2")) {//添加污染源

				//添加污染源点到污染源数组
        		LatLng tempLatLng = new LatLng(poiInfo.getPt().latitude, poiInfo.getPt().longitude);
        		mPointPollutes.add(tempLatLng);//添加污染源点到污染源数组
        		poiInfoPollutes.add(poiInfo); //添加污染源点到污染源收藏夹

				//Log.i("info", "add a pullotion:"+poiInfo.getPoiName()+" "
                //		+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);
				if (poiInfo.getCityName().trim().equals("1")) { //轻度污染
					option = new MarkerOptions().icon(logoP)
        					.position(poiInfo.getPt()).title(poiInfo.getPoiName());
					b = new Bundle();
	                b.putString("id", list.get(i).getID());
	                option.extraInfo(b);
	                markers.add((Marker) mBaiduMap.addOverlay(option));
				} else if (poiInfo.getCityName().trim().equals("2")) { //中度污染
					option = new MarkerOptions().icon(logoY)
        					.position(poiInfo.getPt()).title(poiInfo.getPoiName());
					b = new Bundle();
	                b.putString("id", list.get(i).getID());
	                option.extraInfo(b);
	                markers.add((Marker) mBaiduMap.addOverlay(option));
				}else if (poiInfo.getCityName().trim().equals("3")) {  //重度污染
					option = new MarkerOptions().icon(logoR)
        					.position(poiInfo.getPt()).title(poiInfo.getPoiName());
					b = new Bundle();
	                b.putString("id", list.get(i).getID());
	                option.extraInfo(b);
	                markers.add((Marker) mBaiduMap.addOverlay(option));
				}else{
					//其他污染程度
					Log.i("info", "添加其他程度的污染源:"+poiInfo.getPoiName()+" "
	                		+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);
				}


			}else if (poiInfo.getAddr().trim().equals("3")) {//添加起点
				Log.i("info", "1.准备显示添加航行起点 ,"+ startPointNum,null);
				if (startPointNum == 1) { //第一次添加起点
					option = new MarkerOptions().icon(logoStart)
	    					.position(poiInfo.getPt()).title(poiInfo.getPoiName());
					b = new Bundle();
	                b.putString("id", list.get(i).getID());
	                option.extraInfo(b);
	                markers.add((Marker) mBaiduMap.addOverlay(option));
	                startPoint = new LatLng(poiInfo.getPt().latitude, poiInfo.getPt().longitude);
	                Log.i("info", "2.显示航行起点,"+ startPointNum+" ,"+ startPoint.toString(),null);
				}else {
					FavoriteManager.getInstance().deleteFavPoi(poiInfo.getID());
					Log.i("info", "3.删除收藏夹中的航行起点,"+startPointNum+" ,"+ poiInfo.getID(),null);

					// Toast.makeText(UsvBaseMap.this, "不能重复添加航行终点", Toast.LENGTH_LONG).show();
				}

			}else if (poiInfo.getAddr().trim().equals("4")) {//添加航行终点
				Log.i("info", "1.准备显示添加航行终点,"+endPointNum,null);
				if (endPointNum == 1) { //第一次添加终点
					option = new MarkerOptions().icon(logoEnd)	//绘制终点标记
	    					.position(poiInfo.getPt()).title(poiInfo.getPoiName());
					b = new Bundle();
	                b.putString("id", list.get(i).getID());
	                option.extraInfo(b);
	                markers.add((Marker) mBaiduMap.addOverlay(option));
	                endPoint = new LatLng(poiInfo.getPt().latitude, poiInfo.getPt().longitude);
	                Log.i("info", "2.显示航行终点,"+endPointNum+" ,"+ endPoint.toString(),null);
				}else {
					FavoriteManager.getInstance().deleteFavPoi(poiInfo.getID());
					Log.i("info", "3.删除收藏夹中的航行终点,"+endPointNum+" ,"+ poiInfo.getID(),null);
					// Toast.makeText(UsvBaseMap.this, "不能重复添加航行终点", Toast.LENGTH_LONG).show();
				}

			}else {
				//添加其他类型的图标
				Log.i("info", "添加其他类型的图标:"+poiInfo.getPoiName()+" "
                		+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);
			}
        }

        //drawPolyLine();
    }

    /**
     * 避碰相关
     * 在地图中画出所有障碍物的边界点
     * 定义试验水域的边界，方便在指定水域进行路径搜索
     * @author CycloneBoy
     * 日期:    2017.04.13 10:36添加
     * 版本:	   v1.0
     * @param v
     */
    public void showObstaclePolygon(View v){
    	// 添加普通折线绘制
        List<LatLng> pointObstacles = new ArrayList<LatLng>();//绘制线条的数据
        List<Point> listPoint = new ArrayList<>();

    	FavoritePoiInfo poiInfo;
    	MarkerOptions option;
    	Bundle b;

    	//mBaiduMap.clear();//不清空添加的图标


        List<FavoritePoiInfo> list = FavoriteManager.getInstance().getAllFavPois();
        if (list == null || list.size() == 0) {
            Toast.makeText(UsvBaseMap.this, "没有收藏点", Toast.LENGTH_LONG).show();
            return;
        }
        // 绘制在地图
        //markers.clear();
        for (int i = 0; i < list.size(); i++) {
        	poiInfo = list.get(i);//获取当前poiInfo
        	/* Log.i("info", "准备添加一个坐标点:"+poiInfo.getPoiName()+" "
             		+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);*/
        	if (poiInfo.getAddr().trim().equals("1")) {	//添加障碍物

        		//obstaclePoiInfo.add(poiInfo);//添加障碍物点到障碍物数组
        		Point ptemp = new Point(poiInfo.getPt().latitude,poiInfo.getPt().longitude);
        		listPoint.add(ptemp);//添加坐标点
                //输出日志
//                Log.i("info", "add a obatacle to obstaclePoiInfo:"+poiInfo.getPoiName()+" "
//                		+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);
			}
        }

        MinimumBoundingPolygon obataclePolygon = new MinimumBoundingPolygon();
        LinkedList<Point> resultObstaclePolygon = new LinkedList<>();
        resultObstaclePolygon = obataclePolygon.findSmallestPolygon(listPoint);

        for (int i = 0; i < resultObstaclePolygon.size(); i++) {
        	pointObstacles.add(new LatLng(resultObstaclePolygon.get(i).getX(),
        			resultObstaclePolygon.get(i).getY()));//填充到绘制边界的障碍物数组中
		}

        pointObstacles.add(pointObstacles.get(0));//获取边界起点坐标点
        OverlayOptions ooPolyline = new PolylineOptions().width(10)
                .color(0xAAFF0000).points(pointObstacles);
      //绘制障碍物线条数据
        mPolyline = (Polyline) mBaiduMap.addOverlay(ooPolyline);//绘制障碍物线条数据

        //绘制避碰路径
        //if (bestTour[GRAID_RADIUS * GRAID_RADIUS] > 0) {
        	// drawPolyLine();
        	//transformBestTourToPoints(bestTour);
        	//Toast.makeText(UsvBaseMap.this, "绘制完成最优路径", Toast.LENGTH_LONG).show();
    		//Log.i("info", "绘制完成最优路径,点数为:"+bestTour[GRAID_RADIUS*GRAID_RADIUS], null);
		//}

        //drawPolyLine();
        //Log.i("info", "绘制完成了障碍物数组的边界", null);
        //Toast.makeText(UsvBaseMap.this, "绘制完成了障碍物数组的边界", Toast.LENGTH_LONG).show();
        //找到所有的障碍物数组数据
    }

    /**
     * 蚁群算法寻找避碰路径初始化
     * 构建n*n地图矩阵
     *
     */
    public void initGraphToFindPath(View v){
    	FavoritePoiInfo poiInfo;
    	// 添加普通折线绘制
    	int obstacleNum = 0;//添加到地图上的障碍物总数
    	//获取地图上的所有点
//    	List<FavoritePoiInfo> list = FavoriteManager.getInstance().getAllFavPois();
//        if (list == null || list.size() == 0) {
//            Toast.makeText(UsvBaseMap.this, "没有收藏点", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        //获取地图中所有障碍物点
//        for (int i = 0; i < list.size(); i++) {
//        	poiInfo = list.get(i);//获取当前poiInfo
//        	 //Log.i("info", "准备添加一个坐标点:"+poiInfo.getPoiName()+" "
//             	//	+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);
//        	if (poiInfo.getAddr().trim().equals("1")) {	//添加障碍物
//
//        		//obstaclePoiInfo.add(poiInfo);//添加障碍物点到障碍物数组
//        		//添加障碍物点到障碍物数组
//        		LatLng tempLatLng = new LatLng(poiInfo.getPt().latitude, poiInfo.getPt().longitude);
//        		mPointObstacles.add(tempLatLng);//障碍物链表中添加障碍物
//                //输出日志
////                Log.i("info", "add a obatacle to obstaclePoiInfo:"+poiInfo.getPoiName()+" "
////                	+poiInfo.getAddr()+" "+poiInfo.getCityName(), null);
//			}
//        }
//

        //当至少有四个点障碍物点的时候才添加到栅格地图上
        if (mPointObstacles.size() >= 4) {
        	 //寻找地图四个顶点
            mGraphCorner.add(mPointObstacles.get(0));//左边界
        	mGraphCorner.add(mPointObstacles.get(1));//上边界
        	mGraphCorner.add(mPointObstacles.get(2));//右边界
        	mGraphCorner.add(mPointObstacles.get(3));//下边界
            for(int i = 0;i < mPointObstacles.size();i++){
            	//左边纬度最小
            	if (mPointObstacles.get(i).latitude < mGraphCorner.get(0).latitude) {
            		mGraphCorner.set(0, mPointObstacles.get(i));
    			}
            	//上边经度最大
            	if (mPointObstacles.get(i).longitude > mGraphCorner.get(1).longitude) {
    				mGraphCorner.set(1, mPointObstacles.get(i));
    			}
            	//右边纬度最大
            	if (mPointObstacles.get(i).latitude > mGraphCorner.get(2).latitude) {
    				mGraphCorner.set(2, mPointObstacles.get(i));
    			}
            	//下边经度最小
            	if (mPointObstacles.get(i).longitude < mGraphCorner.get(3).longitude) {
    				mGraphCorner.set(3,mPointObstacles.get(i));
    			}
            }

            //每个栅格所代表的经度长度和纬度长度
            mLngjLength = (mGraphCorner.get(1).longitude - mGraphCorner.get(3).longitude)/GRAID_RADIUS;
            mLatwLength = (mGraphCorner.get(2).latitude - mGraphCorner.get(0).latitude)/GRAID_RADIUS;

            mGraphIntCorner[0][0] = (int) ((mGraphCorner.get(0).longitude - mGraphCorner.get(3).longitude) /mLngjLength);
            mGraphIntCorner[0][1] = 0;
            mGraphIntCorner[1][0] = (int) ((mGraphCorner.get(1).longitude - mGraphCorner.get(3).longitude)/mLngjLength);
            mGraphIntCorner[1][1] = (int) ((mGraphCorner.get(1).latitude - mGraphCorner.get(0).latitude)/ mLatwLength);
            mGraphIntCorner[2][0] = (int) ((mGraphCorner.get(2).longitude -mGraphCorner.get(3).longitude )/mLngjLength);
            mGraphIntCorner[2][1] = (int) ((mGraphCorner.get(2).latitude - mGraphCorner.get(0).latitude)/ mLatwLength);
            mGraphIntCorner[3][0] = 0;
            mGraphIntCorner[3][1] = (int) ((mGraphCorner.get(3).latitude -mGraphCorner.get(0).latitude)/ mLatwLength);

            Log.i("info","栅格边界坐标: ("+mGraphIntCorner[0][0]+","+mGraphIntCorner[0][1]+") ,("+
            							mGraphIntCorner[1][0]+","+mGraphIntCorner[1][1]+") ,("+
            							mGraphIntCorner[2][0]+","+mGraphIntCorner[2][1]+") ,("+
            							mGraphIntCorner[3][0]+","+mGraphIntCorner[3][1]+")"
            							,null);

            double  k1,k2,k3,k4,b1,b2,b3,b4;

            k1 = (mGraphIntCorner[1][1] - mGraphIntCorner[0][1])*1.0/(mGraphIntCorner[1][0]-mGraphIntCorner[0][0]);
            b1 = mGraphIntCorner[1][1] - k1 *mGraphIntCorner[1][0];

            k2 = (mGraphIntCorner[2][1] - mGraphIntCorner[1][1])*1.0/(mGraphIntCorner[2][0]-mGraphIntCorner[1][0]);
            b2 = mGraphIntCorner[2][1] - k2 *mGraphIntCorner[2][0];

            k3 = (mGraphIntCorner[3][1] - mGraphIntCorner[2][1])*1.0/(mGraphIntCorner[3][0]-mGraphIntCorner[2][0]);
            b3 = mGraphIntCorner[3][1] - k3 *mGraphIntCorner[3][0];

            k4 = (mGraphIntCorner[0][1] - mGraphIntCorner[3][1])*1.0/(mGraphIntCorner[0][0]-mGraphIntCorner[3][0]);
            b4 = mGraphIntCorner[0][1] - k4 *mGraphIntCorner[0][0];

            Log.i("info","栅格边界线方程: k1="+ k1 +", b1="+ b1+", k2="+k2+",b2="+b2
            		+"k3="+ k3+", b3="+b3 + ", k4="+k4+", b4="+b4,null);


            //填充地图中的障碍物
            for (int i = 0; i < GRAID_RADIUS; i++) {
    			for (int j = 0; j < GRAID_RADIUS; j++) {
    				graph[i][j] = 1;

    				//在k1直线之下,在k3直线之上
    				if (k1*(i+0.5)+b1 -(j+0.5)  < 0 && k3 *(i+0.5)+b3 - (j+0.5) > 0) {
    					//在k2直线之下,在k4直线之上
    					if ((j+0.5) - k2*(i+0.5)- b2 < 0 && (j+0.5) - k4*(i+0.5) - b4 > 0) {
    						graph[i][j] = 0; //判断是否在四条直线之内
    						Log.i("info","发现一个栅格边界之内的点: graph["+i+"]["+j+"] = "+graph[i][j],null);
    					}
    				}
    			}
    		}

            for (int i = 0; i < mPointObstacles.size(); i++) {
    			int x = (int) ((mPointObstacles.get(i).longitude - mGraphCorner.get(3).longitude) /mLngjLength);
    			int y = (int) ((mPointObstacles.get(i).latitude - mGraphCorner.get(0).latitude) /mLatwLength);

    			if (x < 0) {
    				x=0;
    			}
    			if (x > GRAID_RADIUS -1) {
    				x = GRAID_RADIUS - 1;
    			}

    			if (y < 0) {
    				y=0;
    			}
    			if (y > GRAID_RADIUS -1) {
    				y = GRAID_RADIUS - 1;
    			}
    			graph[x][y] = 1 ;//添加障碍物
    			obstacleNum++;
    			Log.i("info", "添加障碍物到栅格中，graph["+x+"]["+y+"] = 1", null);
    		}
            Log.i("info", "添加"+obstacleNum+"障碍物到Graph中,等待蚁群算法计算最优路径", null);
            Toast.makeText(UsvBaseMap.this, "添加"+obstacleNum+"个障碍物,等待蚁群算法计算最优路径", Toast.LENGTH_LONG).show();
		}else{//障碍物点少于四个
			Toast.makeText(UsvBaseMap.this, "障碍物点少于四个，继续添加障碍物点", Toast.LENGTH_LONG).show();
            return;
		}


//        for (int i = 0; i < GRAID_RADIUS; i++) {
//			for (int j = 0; j < GRAID_RADIUS; j++) {
//				//Log.i("info", "添加障碍物到地图中，graph["+i+"]["+j+"] = 1", null);
//			}
//        }
    }

    /**
     * 进行蚁群算法路径规划，寻找最佳路径
     * 描述: 蚁群算法
     * @author CycloneBoy
     * 版本: V1.0
     * 时间: 2017-04-16 11:38
     * 地点: 实验室
     */
    public void showBestPath(View v){
    	int startPos = 0;//起点栅格
    	int endPos = 0;//终点栅格
    	int x,y;

    	x = (int) ((startPoint.longitude - mGraphCorner.get(3).longitude )/mLngjLength);
    	y = (int)((startPoint.latitude -mGraphCorner.get(0).latitude) /mLatwLength);
    	Log.i("info", "起始点坐标位置:"+startPoint.toString()+" , 航行起点:("+x+","+y+")",null);
    	if (x < 0 && x > GRAID_RADIUS && y < 0 && y > GRAID_RADIUS) {
    		Toast.makeText(UsvBaseMap.this, "起点超出试验水域边界", Toast.LENGTH_LONG).show();
    		Log.i("info", "起点超出试验水域边界", null);
    		return;
		}
    	startPos = x *GRAID_RADIUS + y;

    	x = (int) ((endPoint.longitude - mGraphCorner.get(3).longitude )/mLngjLength);
    	y = (int)((endPoint.latitude -mGraphCorner.get(0).latitude) /mLatwLength);
    	Log.i("info", "终点坐标位置:"+endPoint.toString()+" , 航行终点:("+x+","+y+")",null);
    	if (x < 0 && x > GRAID_RADIUS && y < 0 && y > GRAID_RADIUS) {
    		Toast.makeText(UsvBaseMap.this, "终点超出试验水域边界", Toast.LENGTH_LONG).show();
    		Log.i("info", "终点超出试验水域边界", null);
    		return;
		}
    	endPos = x * GRAID_RADIUS + y;

    	Log.i("info", "起始点坐标位置, 航行起点: "+startPos+" ,航行终点位置: "+endPos, null);

    	//蚁群算法寻找最优路径
    	AcoPath aco = new AcoPath(GRAID_RADIUS, 30, 20, 1.f, 6.f, 0.1f,14.f,startPos,endPos);//每行栅格数量、蚂蚁数量、迭代测试、alpha、beta、rho、q
 	    aco.init(graph);
 	    aco.solve();

 	    //绘制最优路径


 	    this.bestTour = aco.getBestTourPath();

 	    for (int i = 0; i < bestTour[GRAID_RADIUS*GRAID_RADIUS]; i++) {
 	    	Log.i("info", "蚁群算法找到的最优路径为: bestTour["+i+"]="+ bestTour[i], null);
		}
 	    this.bestTourLength = aco.getBestLengthPath();

        //Log.i("info", "蚁群算法找到的最优路径绘制完毕:" , null);

 	    //绘制最优路径
 	   List<LatLng> pointsBestTour = new ArrayList<LatLng>();//绘制最优路径线条的数据
 	   pointsBestTour = transformBestTourToPoints(bestTour);
 	   for (int i = 0; i < pointsBestTour.size(); i++) {
 		  Log.i("info", "需要绘制到地图上的完成最优路径点:"+pointsBestTour.get(i).toString(), null);
	   }

 	   //绘制线段
 	  PolylineOptions polylineOptions = new PolylineOptions().points(pointsBestTour)
 			  					.width(10).color(Color.YELLOW);
 	  mBestTourPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);


 	  // 添加普通折线绘制
 	  LatLng[] latsTemp = new LatLng[100];

 	  pointsToShowTour.add(startPoint);//修正第一个点为航行起点
 	  for (int i = 1; i < pointsBestTour.size()-1; i++) {
 		  latsTemp[i] =  new LatLng(pointsBestTour.get(i).longitude,pointsBestTour.get(i).latitude);
 		  pointsToShowTour.add(latsTemp[i]);
	  }
 	  pointsToShowTour.add(endPoint);//修正最后一个点为航行终点

      OverlayOptions ooPolyline = new PolylineOptions().width(10)
              .color(Color.BLUE).points(pointsToShowTour);
      mPolylineTest = (Polyline) mBaiduMap.addOverlay(ooPolyline);
      Log.i("info", " 测试绘制曲线", null);

      //开始行走
      OverlayOptions markerOptions;
      markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f)
              .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)).position(startPoint)
              .rotate((float) getAngle(0));
      //添加船只，代表行走
      mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

      //开启线程一直行走
      moveMarkerflagToMove = 1;
      moveLooper();
    }

    /**
     * 绘制最优路径
     * @param bestTourPath 最优路径的位置数组
     * @author CycloneBoy
     * 版本 : V1.0
     * 时间 : 2017-04-16
     */
    public List<LatLng> transformBestTourToPoints(int[] bestTourPath){
    	List<LatLng> pointsBestTour = new ArrayList<LatLng>();//绘制最优路径线条的数据
    	//int length = bestTourPath[GRAID_RADIUS * GRAID_RADIUS];
    	LatLng[] posPointTemp = new LatLng[GRAID_RADIUS * GRAID_RADIUS];

    	double x,y;

    	for (int i = 0; i < bestTourPath[GRAID_RADIUS * GRAID_RADIUS]; i++) {

    		//起点坐标变换
        	x = (bestTourPath[i]/GRAID_RADIUS + 0.5) * mLngjLength  + mGraphCorner.get(3).longitude;
        	y = (bestTourPath[i]%GRAID_RADIUS + 0.5) * mLatwLength + mGraphCorner.get(0).latitude;

        	posPointTemp[i] = new LatLng(x, y);
        	//往路径点数组中添加
        	pointsBestTour.add(posPointTemp[i]);
     	    Log.i("info", "transformBestTourToPoints->蚁群算法找到的最优路径的经纬度为:" + pointsBestTour.get(i).toString(), null);
		}

    	if (pointsBestTour.size() == 0) {
			pointsBestTour.add(startPoint);
			pointsBestTour.add(endPoint);
		}

    	//画出起点和终点之间的连线
//    	 PolylineOptions polylineOptions = new PolylineOptions().points(pointsBestTour).width(10).color(Color.YELLOW);
//    	 mBestTourPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);//绘制障最优路径
//
//         Log.i("info", "蚁群算法找到的最优路径绘制完毕:" , null);

         return pointsBestTour;
    }

    private void drawPolyLine() {

        List<LatLng> polylines = new ArrayList<LatLng>();
        polylines = transformBestTourToPoints(bestTour); //获取点数

        PolylineOptions polylineOptions = new PolylineOptions().points(polylines).width(10).color(Color.YELLOW);

        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);

    }

    /**
     * 删除全部点
     *
     * @param v
     */
    public void deleteAllClick(View v) {
        if (FavoriteManager.getInstance().clearAllFavPois()) {
            Toast.makeText(UsvBaseMap.this, "全部删除成功", Toast.LENGTH_LONG).show();
            mBaiduMap.clear();
            mPointObstacles.clear();//清空障碍物数组，重新添加障碍物数组
            mBaiduMap.hideInfoWindow();

            //可以重新开始添加航行起点和航行终点
            startPointNum = 0;
            endPointNum = 0;
        } else {
            Toast.makeText(UsvBaseMap.this, "全部删除失败", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * 用户界面相关
     * 发起用户定义搜索，根据用户点击的不同的按钮实现不同的功能
     * @author CycloneBoy
     * @param v
     */
    public void searchUserButtonProcess(View v) {
    	switch (v.getId()) {
		case R.id.geocode:	//反解码搜索
			 EditText editCity = (EditText) findViewById(R.id.city);
	            EditText editGeoCodeKey = (EditText) findViewById(R.id.geocodekey);
	            // Geo搜索
	            mSearch.geocode(new GeoCodeOption().city(
	                    editCity.getText().toString()).address(editGeoCodeKey.getText().toString()));
			break;

		case R.id.geocodeUser: //弹出式正解码
			inptGeo(); //打开设定位置弹出对话框
			break;
		case R.id.DeleteAllObstacleMaker:	//删除所有障碍物
			deleteAllClick(mMapView);
			break;
		case R.id.DeleteAllSourceOfPollutionMaker:	//删除所有污染源
			//showObstaclePolygon(mMapView);
			break;
		case R.id.showBestPath:	//显示避碰路径

			break;

		default:
			break;
		}

    }


    /**
	 * 设置试验水面位置地理坐标
	 * 设置指定湖泊经纬度坐标，跳转到指定位置
	 * @author CycloneBoy
	 */
	protected void inptGeo() {

		 //装载对话框布局
		LinearLayout newGeo = (LinearLayout)getLayoutInflater()
				.inflate(R.layout.input_geo_dialog, null);

		//取得对话框中的输入框对象
		final EditText et_dia_latj = (EditText) newGeo.findViewById(R.id.etLatj);
		final EditText et_dia_Lngw = (EditText) newGeo.findViewById(R.id.etLngw);
		et_dia_latj.setText("30.575498");//设置默认值
		et_dia_Lngw.setText("114.379615");

		//新建一个对话框
		new AlertDialog.Builder(UsvBaseMap.this)
				.setTitle("输入指定湖泊经纬度")
				.setView(newGeo)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String dia_lngj = et_dia_latj.getText().toString();
						String dia_latw = et_dia_Lngw.getText().toString();

						if (dia_lngj.equals("") || dia_latw.equals("")) {//判断是否为空值
							Toast.makeText(UsvBaseMap.this, "不能为空值!", Toast.LENGTH_LONG)
							.show();
						} else {		//返回给UsvBaseMap

				            LatLng ptCenter = new LatLng((Float.valueOf(dia_lngj)), (Float.valueOf(dia_latw)));
				            // 反Geo搜索
				            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
				                    .location(ptCenter));
								Toast.makeText(UsvBaseMap.this, "定位到指定位置!", Toast.LENGTH_LONG)
								.show();
						}
					}
				})
				//设置一个取消按钮
				.setNegativeButton("取消",new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub

					}
				})
				.show();
	}

	/**
	 * 设定障碍物或者污染源
	 * 弹出设定障碍物或者污染源的对话框
	 * @author CycloneBoy
	 * @param  point 当前触摸点的经纬度
	 */
	public void inputMarker(LatLng point){

		//装载对话框布局
		LinearLayout newMarker = (LinearLayout)getLayoutInflater()
				.inflate(R.layout.input_add_marker_dialog, null);

		//取得对话框中的输入框对象
		final EditText et_dia_marker_name = (EditText) newMarker.findViewById(R.id.etMarkerName);
		final EditText et_dia_marker_latj = (EditText) newMarker.findViewById(R.id.etMarkerLatj);
		final EditText et_dia_marker_Lngw = (EditText) newMarker.findViewById(R.id.etMarkerLngw);
		final EditText et_dia_marker_type = (EditText) newMarker.findViewById(R.id.etMarkerType);
		final EditText et_dia_marker_degree = (EditText) newMarker.findViewById(R.id.etMarkerDegree);
		et_dia_marker_name.setText("位置"+ String.valueOf(nMarkerNum+1));
		et_dia_marker_latj.setText(String.valueOf(point.latitude));
		et_dia_marker_Lngw.setText(String.valueOf(point.longitude));
		et_dia_marker_type.setText(String.valueOf(1));
		et_dia_marker_degree.setText(String.valueOf(1));

		//新建一个对话框
				new AlertDialog.Builder(UsvBaseMap.this)
						.setTitle("输入坐标点经纬度和类型")
						.setView(newMarker)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								String dia_name = et_dia_marker_name.getText().toString();
								String dia_lngj = et_dia_marker_latj.getText().toString();
								String dia_latw = et_dia_marker_Lngw.getText().toString();
								String dia_type = et_dia_marker_type.getText().toString();
								String dia_degree = et_dia_marker_degree.getText().toString();

								if (dia_name == null || dia_name.equals("")) {
						            Toast.makeText(UsvBaseMap.this, "名称必填", Toast.LENGTH_LONG)
						                 .show();
						            return;
						        }

								if (dia_lngj.equals("") || dia_latw.equals("")) {//判断是否为空值
									Toast.makeText(UsvBaseMap.this, "坐标点必填!", Toast.LENGTH_LONG)
										 .show();
									return;
								}
								if (dia_type.equals("") || dia_degree.equals("") ) {
									Toast.makeText(UsvBaseMap.this, "坐标点类型和程度不能为空!", Toast.LENGTH_LONG)
									 .show();
									return;
								}

								//判断是否已经添加过航行起点和航行终点
								//已经添加过航行起点,不能重复添加
								if (dia_type.trim().equals("3") && startPointNum == 0 ) {
									//startPointNum = 1;
									//Log.i("info", "0.添加第一个航行起点 "+startPointNum,null);
								} else if (dia_type.trim().equals("3") && startPointNum == 1 ) {
									Toast.makeText(UsvBaseMap.this, "不能重复添加航行起点", Toast.LENGTH_LONG)
									 .show();
									return;
								}
								//已经添加过航行终点,不能重复添加
								if ( dia_type.trim().equals("4") && endPointNum == 0) {
									//endPointNum = 1;
									//Log.i("info", "0.添加第一个航行终点 " +endPointNum,null);
								} else if (dia_type.trim().equals("4") && endPointNum == 1) {
									Toast.makeText(UsvBaseMap.this, "不能重复添加航行终点", Toast.LENGTH_LONG)
									 .show();
									return;
								}

								info = new FavoritePoiInfo();
						        info.poiName(dia_name);
						        info.addr(dia_type);//类型
						        info.cityName(dia_degree);//程度
						        nMarkerNum++;
						        LatLng pt;
						        try {

						            pt = new LatLng(Double.parseDouble(dia_lngj), Double.parseDouble(dia_latw));
						            info.pt(pt);
						            if (FavoriteManager.getInstance().add(info) == 1) {

						            	//判断是否已经添加过航行起点和航行终点
										//已经添加过航行起点,不能重复添加
										if (dia_type.trim().equals("3") && startPointNum == 0 ) {
											startPointNum = 1;
											Log.i("info", "0.添加第一个航行起点 "+startPointNum,null);
										}


										//已经添加过航行终点,不能重复添加
										if ( dia_type.trim().equals("4") && endPointNum == 0) {
											endPointNum = 1;
											Log.i("info", "0.添加第一个航行终点 " +endPointNum,null);
										}

						                Toast.makeText(UsvBaseMap.this, "坐标添加成功", Toast.LENGTH_LONG).show();
						                getAllClick(mMapView);//显示坐标
						                initGraphToFindPath(mMapView); //重新更新一下栅格地图
						                Log.i("info", "add a marker:"+info.getPoiName()+" "
						                		+info.getAddr()+" "+info.getCityName(), null);
						            } else {
						                Toast.makeText(UsvBaseMap.this, "坐标添加失败", Toast.LENGTH_LONG).show();
						            }

						        } catch (Exception e) {
						            // TODO: handle exception
						            Toast.makeText(UsvBaseMap.this, "坐标解析错误", Toast.LENGTH_LONG)
						                    .show();
						        }

							}
						})
						//设置一个取消按钮
						.setNegativeButton("取消",new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								
							}
						})
						.show();
				
			//getAllClick(mMapView);//更新显示插入的坐标
		    
	}
	/**
	 * 定位相关
     * 定位SDK监听函数
     * @author CycloneBoy
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
           // 设置定位数据  
            mBaiduMap.setMyLocationData(locData);
           // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）  
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
	
    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
     
        super.onResume();
    }
    
    @Override
    protected void onRestart() {
    	// TODO Auto-generated method stub
    	super.onRestart();
    	//初始化航行起点和航行终点
    	
    	
//        startPointNum = 0;
//        endPointNum = 0;
//        
//        //删除避碰路径
//		if (pointsToShowTour.size() >0) {
//			pointsToShowTour.clear();//清空路径
//			mPolylineTest.remove();
//			 moveMarkerflagToMove = 0;
//			 //mMoveMarker.setVisible(false);
//			 mMoveMarker.remove();
//			Log.i("info", "删掉规划的路径",null);
//			
//		}
//		//重新绘制
//    	getAllClick(mMapView);
    }
    @Override
    protected void onDestroy() {
    	//退出时销毁地址搜索
        mSearch.destroy();
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // 释放收藏夹功能资源
        FavoriteManager.getInstance().destroy();
        bdA.recycle();
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    /**
     * 定位相关
     * 根据指定位置解码到经纬度坐标定位
     * @author CycloneBoy
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(UsvBaseMap.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka)));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));
        String strInfo = String.format("纬度：%f 经度：%f",
                result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(UsvBaseMap.this, strInfo, Toast.LENGTH_LONG).show();
    }

    /**
     * 定位相关
     * 根据经纬度坐标定位到指定位置
     * @author CycloneBoy
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(UsvBaseMap.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka)));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));
        Toast.makeText(UsvBaseMap.this, result.getAddress(),
                Toast.LENGTH_LONG).show();

    }

	@Override
	public void onMapClick(LatLng point) {
		 mBaiduMap.hideInfoWindow();
		
	}

	@Override
	public boolean onMapPoiClick(MapPoi poi) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		mBaiduMap.hideInfoWindow();
        // TODO Auto-generated method stub
        if (marker == null) {
            return false;
        }
        
        //如果是行走的图标
        if (marker == mMoveMarker) {
        	moveMarkerflagToMove = 1;//开始新的一次行走
        	moveLooper();
        	Log.i("info", "你点击了行走的图标", null);
		} else {
			    InfoWindow mInfoWindow = new InfoWindow(mPop, marker.getPosition(), -47);
		        mBaiduMap.showInfoWindow(mInfoWindow);
		        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(marker.getPosition());
		        mBaiduMap.setMapStatus(update);
		        currentID = marker.getExtraInfo().getString("id");
		        return true;
		}
        return true;
		
	}

	@Override
	public void onMapLongClick(LatLng point) {
		inputMarker(point);//添加点
	}

	//路径行走先关
	 /**
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mPolylineTest.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mPolylineTest.getPoints().get(startIndex);
        LatLng endPoint = mPolylineTest.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }
	
    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }
    
    /** 
     * 补充：计算两点之间真实距离 
     * @param  longitude1 起点经度
     * @param  latitude1  起点纬度
     * @param  longitude2 终点经度
     * @param  latitude2  终点纬度
     * @author CycloneBoy
     * @return 米 
     */  
    public  double getTwoPointDistance(double longitude1, double latitude1, double longitude2, double latitude2) {  
        // 维度  
        double lat1 = (Math.PI / 180) * latitude1;
        double lat2 = (Math.PI / 180) * latitude2;
  
        // 经度  
        double lon1 = (Math.PI / 180) * longitude1;
        double lon2 = (Math.PI / 180) * longitude2;
  
        // 地球半径  
        double R = 6371;  
  
        // 两点间距离 km，如果想要米的话，结果*1000就可以了  
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;
  
        return d * 1000;  
    }  
    
    /**
     * 循环进行移动逻辑
     */
    public void moveLooper() {
    	
    	if (threadFlagOne == 0) {
    		threadFlagOne = 1;//标志位置一已经运行过一个线程
    		 new Thread() {
    	            public void run() {
    	            	
    	                while (moveMarkerflagToMove == 1) {//moveMarkerflagToMove == 1
    	                	//开始时清空所有找到的污染源信息
    	                	poiInfoFindPollutes.clear();
    	                    for (int i = 0; i < pointsToShowTour.size() - 1; i++) {
    	                        final LatLng startPoint = pointsToShowTour.get(i);
    	                        final LatLng endPoint = pointsToShowTour.get(i+1);
    	                        
    	                       
    	                        //计算当前点附近的污染源
    	                         for (int j = 0; j < mPointPollutes.size(); j++) {
    	                        	 //获取污染源信息
    	                               LatLng tempPollute = new LatLng(mPointPollutes.get(j).latitude,
    	                            		   mPointPollutes.get(j).longitude);
    	                               double distanceTemp = getTwoPointDistance(startPoint.longitude,
    	                            		     startPoint.latitude,tempPollute.longitude,tempPollute.latitude);
    	                               
    	                               if (distanceTemp < DISTANCE_POINT) { //找到200米之内的一个污染源
    	                            	  //记录下找到的污染源信息  FavoritePoiInfo
    	                            	   
    	                            	   //更新时间戳
    	                        		  // Timestamp ts = new Timestamp(System.currentTimeMillis()); 
    	                            	   FavoritePoiInfo tempPoiInfo = new FavoritePoiInfo();// poiInfoPollutes.get(j);
    	                            	   
    	                            	   tempPoiInfo = poiInfoPollutes.get(j);
    	                            	   Log.i("info", "发现一个污染源,距离本船位置:"+distanceTemp+" 米,名称为:"+poiInfoPollutes.get(j).getPoiName(), null);
    	                            	   //记录下找到的污染源信息
    	                            	   //如果已经添加则不要重复添加
    	                            	   if (!poiInfoFindPollutes.contains(poiInfoPollutes.get(j))) {
    	                            		   
    	                            		   poiInfoFindPollutes.add(tempPoiInfo);
    	                                	   //Toast.makeText(UsvBaseMap.this, "发现一个新的污染源:"+poiInfoPollutes.get(j).getPoiName()
    	                                		//	   +" ,"+poiInfoPollutes.get(j).getCityName(), Toast.LENGTH_LONG).show();
    	                                	   Log.i("info", "发现一个新的污染源:"+poiInfoPollutes.get(j).getPoiName()
    	                                			   +" ,"+poiInfoPollutes.get(j).getCityName() +" ,"+
    	                                			   poiInfoPollutes.get(j).getPt().toString()+" ," +
    	                                			   poiInfoPollutes.get(j).getTimeStamp(), null);
    	                            	   }else {
    	                            		    Log.i("info", "这个污染源已经被发现过:"+poiInfoPollutes.get(j).getPoiName(), null);
    									  }
    	                            	   
    	                            	  
    								}
    	        				}
    	                        
    	                        mMoveMarker
    	                                .setPosition(startPoint);

    	                        mHandler.post(new Runnable() {
    	                            @Override
    	                            public void run() {
    	                                // refresh marker's rotate
    	                                if (mMapView == null) {
    	                                    return;
    	                                }
    	                                mMoveMarker.setRotate((float) getAngle(startPoint,
    	                                        endPoint));
    	                            }
    	                        });
    	                        double slope = getSlope(startPoint, endPoint);
    	                        // 是不是正向的标示
    	                        boolean isReverse = (startPoint.latitude > endPoint.latitude);

    	                        double intercept = getInterception(slope, startPoint);

    	                        double xMoveDistance = isReverse ? getXMoveDistance(slope) : -1 * getXMoveDistance(slope);


    	                        for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse);
    	                             j = j - xMoveDistance) {
    	                            LatLng latLng = null;
    	                            if (slope == Double.MAX_VALUE) {
    	                                latLng = new LatLng(j, startPoint.longitude);
    	                            } else {
    	                                latLng = new LatLng(j, (j - intercept) / slope);
    	                            }

    	                            final LatLng finalLatLng = latLng;
    	                            mHandler.post(new Runnable() {
    	                                @Override
    	                                public void run() {
    	                                    if (mMapView == null) {
    	                                        return;
    	                                    }
    	                                    mMoveMarker.setPosition(finalLatLng);
    	                                }
    	                            });
    	                            try {
    	                                Thread.sleep(TIME_INTERVAL);
    	                            } catch (InterruptedException e) {
    	                                e.printStackTrace();
    	                            }
    	                        }//一个点行走完毕

    	                    }
    	                    moveMarkerflagToMove = 0;
    	                    mMoveMarker.setToTop();//设置到顶端
    	                    
    	                }
    	            }

    	        }.start();
		} else {
			moveMarkerflagToMove = 1;//开始新的一次行走
		}
    	
    }
    
    /**
     * 显示发现的污染源信息
     * @author CycloneBoy
     * 
     */
    public void showListPollutions(View v){
    	String listName = "显示污染源信息:";
		//String et2Str = "具体信息";
		String[][] strFindPollutionsList;
		//创建Intent对象，参数为上下文，要跳转的Activity
		Intent intent = new Intent(UsvBaseMap.this,ListPollutionsActivity.class);
		//将要传递的值附加到Intent对象
		intent.putExtra("ListName", listName);
		intent.putExtra("FindPollutionsTotal", String.valueOf(poiInfoFindPollutes.size()));
		Log.i("info", "添加->"+listName+","+poiInfoFindPollutes.size(), null);
		//如果污染源中有数据
		if (poiInfoFindPollutes.size() > 0) {
			 strFindPollutionsList = new String[poiInfoFindPollutes.size()][7];
			 
			
			//获取时间戳
         	Timestamp ts = new Timestamp(System.currentTimeMillis());
             String currentTime = "";
             DateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
             try {   
             	currentTime = sdf.format(ts);   
                 System.out.println(currentTime);
             } catch (Exception e) {
                 e.printStackTrace();   
             }  
             strScreenShotPath = "/mnt/sdcard/usv_"+currentTime+".png";//获取当前截图的最新路径
             
             //进行手机地图规划路径截屏
			 saveScreenShot(mMapView);
			 
			 //填充数据
			 for (int i = 0; i < strFindPollutionsList.length; i++) {
				     //污染源被发现的序号
					 strFindPollutionsList[i][0] =  String.valueOf(i);
					 //污染源名称
					 strFindPollutionsList[i][1] =  poiInfoFindPollutes.get(i).getPoiName();
					 //污染源类型
					 strFindPollutionsList[i][2] =  poiInfoFindPollutes.get(i).getAddr();
					 //污染源污染程度
					 strFindPollutionsList[i][3] =  poiInfoFindPollutes.get(i).getCityName();
					 //污染源污染坐标位置
					 strFindPollutionsList[i][4] =  poiInfoFindPollutes.get(i).getPt().toString();
					 //污染源污染被发现时间
					 strFindPollutionsList[i][5] = String.valueOf(poiInfoFindPollutes.get(i).getTimeStamp());
					 //污染源巡航被发现的巡航截屏图片保存路径
					 
					 strFindPollutionsList[i][6] = strScreenShotPath;
					 
					 //添加到intent中
					intent.putExtra("findPollutionsList"+ String.valueOf(i),strFindPollutionsList[i]);
			}
			 Log.i("info", "showListPollutions->截图路径:"+strFindPollutionsList[0][6] ,null);
			
		}
		
		//启动该Intent对象，实现跳转
		startActivity(intent);
    }
}
