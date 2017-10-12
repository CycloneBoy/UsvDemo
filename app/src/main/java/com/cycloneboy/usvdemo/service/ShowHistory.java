package com.cycloneboy.usvdemo.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.cycloneboy.usvdemo.R;
import com.cycloneboy.usvdemo.utils.WebServicePost;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ShowHistory extends Activity implements OnClickListener {
	
	private TextView tvListName;
	private TextView lvShowInfo;
	private ListView lvShowPollutions;
	private Intent intent ;//接收来自usv_map_activity 传来的数据
	private String strImagePath = new String();
	
	private Button btnUploadRecord;
	private Button btnShowHistoryList;
	 // 创建等待框
    private ProgressDialog dialog;
    // 返回的数据
    private String info;
    // 返回主线程更新数据
    private static Handler handler = new Handler();
    private int flagLoad=0;
    private int flagOneLoad=0; //显示某次的结果
    
    private String showOneImgname ;//长按显示一次结果的巡航图片名
    private String infoOne;//显示一次的显示路径
    //某一次污染源的信息
    String[][] strFindPollutionsListOne;
    
    //污染源的信息
    String[][] strFindPollutionsList;
	Intent myHistoryIntend = new Intent();//历史数据intent
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_history);
		
		//UI相关
				//listView
				lvShowPollutions = (ListView)findViewById(R.id.lvShowPollutionH);
				tvListName = (TextView)findViewById(R.id.tvListNameH);
				lvShowInfo = (TextView)findViewById(R.id.tvShowInfoH);
				
				btnShowHistoryList =(Button) findViewById(R.id.btnShowHistoryListH);
				
				//开始显示,从服务器 获取数据;
				showHistory();
				
				//show();
				
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.btnShowHistoryList:
			//showHistory();
			//show();
			//lvShowInfo.setText("清空了");
			//showHistory();
			//showListViewHistory(info);
			//显示从服务器获取到的数据
			//showPollutionsList(myHistoryIntend);
            break;
		
		case R.id.btnTest:
			//showHistory();
			break;
		}
	}
	
	/**
	 * 获取历史记录,下载记录
	 * @author CycloneBoy
	 *
	 */
	public void showHistory(){
		// 检测网络，无法检测wifi
        if (!checkNetwork()) {
            Toast toast = Toast.makeText(ShowHistory.this,"网络未连接", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        Log.i("info","网络已连接", null);
        // 提示框
        dialog = new ProgressDialog(this);
        dialog.setTitle("提示");
        dialog.setMessage("正在更新列表，请稍后...");
        dialog.setCancelable(false);
        dialog.show();
        // 创建子线程，分别进行Get和Post传输
        flagLoad = 1;
        new Thread(new MyThreadPost()).start();
        
       
        lvShowInfo.setText("更新列表成功");
	}
	
	 // 子线程接收数据，主线程修改数据
    public class MyThreadPost implements Runnable {
    	
        public MyThreadPost() {
			super();
		}
        
	@Override
    public void run() {
		
			String infoRet= new String(WebServicePost.executeHttpFindByPName("1"));
			 info  = new String(infoRet);
			// Log.i("info","服务器返回的数据--------------------------:"+ info , null);
			 //处理服务器返回的数据
			 
			 handler.post(new Runnable() {
	                @Override
	                public void run() {
	                	//lvShowInfo.setText(lvShowInfo.getText()+info);
	                	lvShowInfo.setText("获取下载完毕,请点击显示按钮显示历史记录！");
	                	flagLoad = 2;
	                    Log.i("info","获取数据完毕:", null);
	                    dialog.dismiss();
	                }
	            });
		}
}
	
	/**
	 * 获取一次的历史记录
	 * @author CycloneBoy
	 *
	 */
	public void showOneHistory(){
		// 检测网络，无法检测wifi
        if (!checkNetwork()) {
            Toast toast = Toast.makeText(ShowHistory.this,"网络未连接", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            return;
        }
        Log.i("info","网络已连接", null);
        // 提示框
        dialog = new ProgressDialog(this);
        dialog.setTitle("提示");
        dialog.setMessage("正在更新列表，请稍后...");
        dialog.setCancelable(false);
        dialog.show();
        // 创建子线程，分别进行Get和Post传输
        flagOneLoad = 1;
        new Thread(new MyThreadPostOne()).start();
        
        lvShowInfo.setText("更新列表成功");
       
	}
	
	 // 子线程接收数据，主线程修改数据
    public class MyThreadPostOne implements Runnable {
    	
        public MyThreadPostOne() {
			super();
		}
        
	@Override
    public void run() {
		
			String infoRet= new String(WebServicePost.executeHttpFindByImgname(showOneImgname));
			 infoOne  = new String(infoRet);
			// Log.i("info","服务器返回的数据--------------------------:"+ info , null);
			 //处理服务器返回的数据
			 
			 handler.post(new Runnable() {
	                @Override
	                public void run() {
	                	//lvShowInfo.setText(lvShowInfo.getText()+info);
	                	lvShowInfo.setText("获取下载完毕,请点击显示按钮显示历史记录！");
	                	flagOneLoad = 2;
	                    Log.i("info","获取数据完毕:", null);
	                   
	                    dialog.dismiss();
	                    showInfoOne(infoOne);
	                }
	            });
		}
}
	
    /**
     * 处理服务器返回的数据
     * 
     */
    public Intent showListViewHistory(String infos){
    	//Intent intent = new Intent(ShowHistory.this,ListPollutionsActivity.class);
		
    	
    	Intent tempIntent = new Intent();
    	//String[][] strFindPollutionsList;
    	
    	Log.i("info", "从服务器返回的数据:"+info, null);
    	
    	String[] strFind = info.split("\r\n");
    	Log.i("info", "从数据库返回的长度:"+strFind.length, null);
    	
    	for (int j = 0; j < strFind.length; j++) {
			Log.i("info","第"+j+"条记录:"+ strFind[j], null);
    	}
    	
    	String[] tempLine ;
    	strFindPollutionsList = new String[strFind.length][7];
    	
    	//将要传递的值附加到Intent对象
    	myHistoryIntend.putExtra("ListName", "历史数据:");
    	myHistoryIntend.putExtra("FindPollutionsTotal", String.valueOf(strFind.length-1));
    	
    	//检查是否有数据
//    	if (strFind.length >0) {
//    		strFindPollutionsList = new String[strFind.length][7];
//		}else{
//			return null;
//		}
			
    	for (int j = 0; j < strFind.length -1; j++) {
			Log.i("info", strFind[j], null);
			tempLine = strFind[j].split(" ");
			
//			//填充数据
				    Log.i("info", tempLine.toString(), null);
				     //污染源被发现的序号
					 strFindPollutionsList[j][0] =  String.valueOf(j);
					 //污染源名称
					 strFindPollutionsList[j][1] =  tempLine[3].split("=")[1];
					 //污染源类型
					 strFindPollutionsList[j][2] = tempLine[2].split("=")[1];
					 //污染源污染程度
					 strFindPollutionsList[j][3] = tempLine[4].split("=")[1];
					 //污染源污染坐标位置
					 strFindPollutionsList[j][4] = tempLine[5].split("=")[1];
					 //污染源污染被发现时间
					 strFindPollutionsList[j][5] = tempLine[6].split("=")[1];
					 //污染源巡航被发现的巡航截屏图片保存路径
					 
					 strFindPollutionsList[j][6] = tempLine[1].split("=")[1];
					 Log.i("info", "showListPollutions->截图路径:"+strFindPollutionsList[j][6],null);
					 //添加到intent中
					 myHistoryIntend.putExtra("findPollutionsList"+ String.valueOf(j),strFindPollutionsList[j]);
			 
//			 for (int i = 0; i < 7; i++) {
//				 System.out.println("strFindPollutionsList["+j+"]["+i+"]="+strFindPollutionsList[j][i]);  
//			 } 
		}
    	
    	
    	String name = "历史记录";//myHistoryIntend.getStringExtra("ListName");
		tvListName.setText(name);
		Log.i("info","要显示的名称"+name , null);
		//定义一个动态数组
		ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String strTemp =" ";
		//在数组中存放数据
		for (int i = 0; i < strFind.length -1; i++) {
			HashMap<String, Object> map = new HashMap<String,Object>();
			map.put("Title", "污染源名称:"+strFindPollutionsList[i][1]);//名称
			
			//设置污染程度
			if (strFindPollutionsList[i][3].trim().equals("1")) {
				strTemp = "轻度污染";
			}else if (strFindPollutionsList[i][3].trim().equals("2")) {
				strTemp = "中度污染";
			} else if(strFindPollutionsList[i][3].trim().equals("3")) {
				strTemp = "重度污染";
			}
			map.put("Degree", "污染程度:"+strTemp);//污染程度
			map.put("Position", strFindPollutionsList[i][4].replaceAll(" ", ""));//位置经纬度
			
			map.put("FindTime", "发现时间:"+sdf.format(Long.valueOf(strFindPollutionsList[i][5])));//发现时间
			map.put("PathImage",R.drawable.icon_path);//巡航路径
			
			//添加进去
			listItem.add(map);
		}
		//获取截图的路径
		strImagePath = strFindPollutionsList[0][6];
		Log.i("info", "showPollutionsList->截图路径:"+strImagePath ,null);
		
		SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,//需要绑定到本视图
				listItem, //需要绑定的数据
				R.layout.item_show_pollution,//每一行的布局
				//动态数组中的数据源的键对应到定义布局的view中
				new String[] {"Title","Degree","Position","FindTime","PathImage"},
				//每一个数据源对应视图中每一个ID
				new int[]{R.id.tvItemTitle,R.id.tvItemDegree,R.id.tvItemPosition,
						R.id.tvItemFindTime,R.id.ivItemPathImage});
		//简单视图显示出来
		//lvShowPollutions.setAdapter(new ArrayAdapter<String>(this,
			//		android.R.layout.simple_list_item_1,strShow));
		
		//复杂视图显示
		lvShowPollutions.setAdapter(mSimpleAdapter);
		
		//设置监听事件
//		lvShowPollutions.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				lvShowInfo.setText("info:你点击了第"+id +"行");//设置Info信息栏中显示点击的行数
//			}
//		});
		
		//设置长按监听事件,显示规矩
		//lvShowPollutions.setOnItemLongClickListener(showImgListener);
    	
    	
    	
    	return tempIntent;
    }
	 // 检测网络
    /** 
     * 对网络连接状态进行判断 
     * @return  true, 可用； false， 不可用 
     */ 
    private boolean checkNetwork() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
	
	/**
	 * 显示信息
	 * @author CycloneBoy
	 */
	public void showPollutionsList(Intent myIntent){
		//取出Intent中附加的数据
				String name = "历史记录";//myHistoryIntend.getStringExtra("ListName");
				tvListName.setText(name);
				Log.i("info","要显示的名称"+name , null);
				
				//String FindPollutionsTotal = myHistoryIntend.getStringExtra("FindPollutionsTotal");
				//Log.i("info","要显示的数量String="+FindPollutionsTotal , null);
				//int PollutionsTotal = Integer.valueOf(FindPollutionsTotal);
				//Log.i("info","要显示的数量Int="+PollutionsTotal , null);
				//String[] strShow = new String[PollutionsTotal];
				//strFindPollutionsList = new String[PollutionsTotal][7];
				
//				if ( PollutionsTotal> 0 ) {
//					 
//						for (int i = 0; i < strFindPollutionsList.length; i++) {
//							 //污染源被发现的序号
//							 strFindPollutionsList[i] = myHistoryIntend.getStringArrayExtra("findPollutionsList"+i);
//							 
//						}
//						
//						for (int i = 0; i < strShow.length; i++) {
//							strShow[i] = strFindPollutionsList[i][1];
//						}
//				}
				
				//定义一个动态数组
				ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
				DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
				String strTemp =" ";
				//在数组中存放数据
				for (int i = 0; i < strFindPollutionsList.length; i++) {
					HashMap<String, Object> map = new HashMap<String,Object>();
					map.put("Title", "污染源名称:"+strFindPollutionsList[i][1]);//名称
					
					//设置污染程度
					if (strFindPollutionsList[i][3].trim().equals("1")) {
						strTemp = "轻度污染";
					}else if (strFindPollutionsList[i][3].trim().equals("2")) {
						strTemp = "中度污染";
					} else if(strFindPollutionsList[i][3].trim().equals("3")) {
						strTemp = "重度污染";
					}
					map.put("Degree", "污染程度:"+strTemp);//污染程度
					map.put("Position", strFindPollutionsList[i][4].replaceAll(" ", ""));//位置经纬度
					
					map.put("FindTime", "发现时间:"+sdf.format(Long.valueOf(strFindPollutionsList[i][5])));//发现时间
					map.put("PathImage",R.drawable.icon_path);//巡航路径
					
					//添加进去
					listItem.add(map);
				}
				//获取截图的路径
				strImagePath = strFindPollutionsList[0][6];
				Log.i("info", "showPollutionsList->截图路径:"+strImagePath ,null);
				
				SimpleAdapter mSimpleAdapter = new SimpleAdapter(this,//需要绑定到本视图
						listItem, //需要绑定的数据
						R.layout.item_show_pollution,//每一行的布局
						//动态数组中的数据源的键对应到定义布局的view中
						new String[] {"Title","Degree","Position","FindTime","PathImage"},
						//每一个数据源对应视图中每一个ID
						new int[]{R.id.tvItemTitle,R.id.tvItemDegree,R.id.tvItemPosition,
								R.id.tvItemFindTime,R.id.ivItemPathImage});
				//简单视图显示出来
				//lvShowPollutions.setAdapter(new ArrayAdapter<String>(this,
					//		android.R.layout.simple_list_item_1,strShow));
				
				//复杂视图显示
				lvShowPollutions.setAdapter(mSimpleAdapter);
				
				//设置监听事件
				lvShowPollutions.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						lvShowInfo.setText("info:你点击了第"+id +"行");//设置Info信息栏中显示点击的行数
					}
				});
				
				//设置长按监听事件,显示规矩
				lvShowPollutions.setOnItemLongClickListener(showImgListener);
	}
	
	OnItemLongClickListener showImgListener = new OnItemLongClickListener(){

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			//显示具体的污染源
			//获取路径名称
			showOneImgname = new String(strFindPollutionsList[(int) id][6].toString());
			
			//进行从服务器下载这次的数据
			showOneHistory();
			//等待数据加载完毕
			
			
				//showInfoOne(infoOne);
				//Toast.makeText(ShowHistory.this, "数据加载完毕", Toast.LENGTH_LONG)
				//.show();
			//}else {
				Toast.makeText(ShowHistory.this, "等待数据加载完毕", Toast.LENGTH_LONG)
				.show();
			//}
			
			
			
			/*String strTemp = strImagePath;
			String imgSrcPath = "/storage/emulated/0/data/123.jpg";
			strImagePath = strFindPollutionsList[(int) id][6].toString();
			//显示图片
			Log.i("info", " 长按监听事件:准备显示图片路径,"+strImagePath, null);
			
			showPathImge(strImagePath);*/
			return true;
		}
	};
	
	
	/**
	 * 显示某一次的结果
	 */
	public void showInfoOne(String infomation){
		
		 String[] strFind = infoOne.split("\r\n");
	    	Log.i("info", "从数据库返回的长度:"+strFind.length, null);
	    	
	    	for (int j = 0; j < strFind.length; j++) {
				Log.i("info","第"+j+"条记录:"+ strFind[j], null);
	    	}
	    	
	    	String[] tempLine ;
	    	strFindPollutionsListOne = new String[strFind.length][7];
		 
	    	for (int j = 0; j < strFind.length -1; j++) {
				Log.i("info", strFind[j], null);
				tempLine = strFind[j].split(" ");
						//填充数据
					    Log.i("info", tempLine.toString(), null);
					     //污染源被发现的序号
					    strFindPollutionsListOne[j][0] =  String.valueOf(j);
						 //污染源名称
					    strFindPollutionsListOne[j][1] =  tempLine[3].split("=")[1];
						 //污染源类型
					    strFindPollutionsListOne[j][2] = tempLine[2].split("=")[1];
						 //污染源污染程度
					    strFindPollutionsListOne[j][3] = tempLine[4].split("=")[1];
						 //污染源污染坐标位置
					    strFindPollutionsListOne[j][4] = tempLine[5].split("=")[1];
						 //污染源污染被发现时间
					    strFindPollutionsListOne[j][5] = tempLine[6].split("=")[1];
						 //污染源巡航被发现的巡航截屏图片保存路径
						 
					    strFindPollutionsListOne[j][6] = tempLine[1].split("=")[1];
						 Log.i("info", "showListPollutions->截图路径:"+strFindPollutionsListOne[j][6],null);
	    	}
	    	
	    	LinearLayout newImgLayout = (LinearLayout)getLayoutInflater()
					.inflate(R.layout.show_one_infomation, null);
		    TextView tvName = (TextView) newImgLayout.findViewById(R.id.tvShowOneInfo);
		    
		    String tempShow = new String();
		    for (int i = 0; i < strFind.length -1; i++) {
		    	if (strFindPollutionsListOne[i][3].equals("1")) {
		    		tempShow ="序号:"+ String.valueOf(i+1) + " -轻微污染,位置: "+strFindPollutionsListOne[i][4];
				} else if (strFindPollutionsListOne[i][3].equals("2")) {
					tempShow ="序号:"+ String.valueOf(i+1) + " -中度污染,位置: "+strFindPollutionsListOne[i][4];
				}else if (strFindPollutionsListOne[i][3].equals("3")) {
					tempShow ="序号:"+ String.valueOf(i+1) + " -严重污染,位置: "+strFindPollutionsListOne[i][4];
				}
		    	tvName.setText(tvName.getText()+"\r\n"+tempShow);
			}
		    
		    
		    //tvName.setText(tvName.getText() );
		  //新建一个对话框
			new AlertDialog.Builder(ShowHistory.this)
					.setTitle("显示这次巡航发现的污染源信息")
					.setView(newImgLayout)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.i("info", "showPathImge:确定", null);
						}
					})
					//设置一个取消按钮
					.setNegativeButton("取消",new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							Log.i("info", "showPathImge:取消", null);
						}
					})
					.show();
		
	}
	
	
	
	/**
	 * 显示图片
	 * @author CycloneBoy
	 */
	public void showPathImge(String imgSrcPath){
		//装载对话框布局
		Log.i("info", "showPathImge:"+imgSrcPath, null);
				LinearLayout newImgLayout = (LinearLayout)getLayoutInflater()
						.inflate(R.layout.show_img_path, null);
			    TextView imgName = (TextView) newImgLayout.findViewById(R.id.tvImgName);
			    imgName.setText(imgName.getText() + imgSrcPath);
			    
				ImageView imageView = (ImageView) newImgLayout.findViewById(R.id.imgPlanPath);
		        
				Bitmap bitmap = BitmapFactory.decodeFile(imgSrcPath);
		        imageView.setImageBitmap(bitmap);//设置图片
		        Log.i("info", "showPathImge->截图路径:"+imgSrcPath ,null);
				
				//新建一个对话框
				new AlertDialog.Builder(ShowHistory.this)
						.setTitle("显示规划路径")
						.setView(newImgLayout)
						.setPositiveButton("确定", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.i("info", "showPathImge:确定", null);
							}
						})
						//设置一个取消按钮
						.setNegativeButton("取消",new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								Log.i("info", "showPathImge:取消", null);
							}
						})
						.show();
	}
	
	/**
	 * 监听menu点击事件
	 * @author CycloneBoy
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//根据ID判断点击位置
		if (item.getItemId() == 1) {
			lvShowInfo.setText("info:你点击了 查看历史记录 第"+item.getItemId() +"行");//设置Info信息栏中显示点击的行数
		} else if (item.getItemId() == 2) {
			lvShowInfo.setText("info:你点击了 清空当前记录 第"+item.getItemId() +"行");//设置Info信息栏中显示点击的行数
		} else {
			
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	//显示结果
	public void show(){
		//info = new String(lvShowInfo.getText().toString());
		lvShowInfo.setText(info);
//		info = " imgname[0]=/mnt/sdcard/usv_20170420075839.png number[0]=1 name[0]=1 degree[0]=1 position[0]=lat:30.562725,lgt:114.390187 findtime[0]=1492305385640\r\n"
//				+ " imgname[1]=/mnt/sdcard/usv_20170420075839.png number[1]=1 name[1]=1 degree[1]=1 position[1]=lat:30.562725,lgt:114.390187 findtime[1]=1492305385640\r\n"
//				+ " imgname[2]=/mnt/sdcard/usv_20170420080736.png number[2]=1 name[2]=1 degree[2]=1 position[2]=lat:30.562725,lgt:114.390187 findtime[2]=1492305385640\r\n"
//				+ " imgname[3]=/mnt/sdcard/usv_20170420080736.png number[3]=1 name[3]=1 degree[3]=1 position[3]=lat:30.562725,lgt:114.390187 findtime[3]=1492305385640\r\n"
//				+ " imgname[4]=/mnt/sdcard/usv_20170420080736.png number[4]=1 name[4]=1 degree[4]=1 position[4]=lat:30.562725,lgt:114.390187 findtime[4]=1492305385640\r\n"
//				+ " imgname[5]=/mnt/sdcard/usv_20170420081505.png number[5]=1 name[5]=1 degree[5]=3 position[5]=lat:30.563402,lgt:114.398335 findtime[5]=1492426758457\r\n"
//				+ " imgname[6]=/mnt/sdcard/usv_20170420110431.png number[6]=1 name[6]=1 degree[6]=2 position[6]=lat:30.552121,lgt:114.392002 findtime[6]=1492349672769\r\n"
//				+ " imgname[7]=/mnt/sdcard/usv_20170420122759.png number[7]=1 name[7]=1 degree[7]=3 position[7]=lat:30.563402,lgt:114.398335 findtime[7]=1492426758457\r\n"
//				+ " imgname[8]=/mnt/sdcard/usv_20170420123719.png number[8]=1 name[8]=1 degree[8]=3 position[8]=lat:30.563969,lgt:114.401021 findtime[8]=1492388383555\r\n"
//				+ " imgname[9]=/mnt/sdcard/usv_20170420124624.png number[9]=1 name[9]=1 degree[9]=3 position[9]=lat:30.563402,lgt:114.398335 findtime[9]=1492426758457\r\n"
//				+ " imgname[10]=/mnt/sdcard/usv_20170420125247.png number[10]=1 name[10]=1 degree[10]=3 position[10]=lat:30.563402,lgt:114.398335 findtime[10]=1492426758457\r\n"
//				+ " imgname[11]=/mnt/sdcard/usv_20170420125918.png number[11]=1 name[11]=1 degree[11]=3 position[11]=lat:30.563402,lgt:114.398335 findtime[11]=1492426758457\r\n"
//				+ " 查询name成功!";
		 String[] strFind = info.split("\r\n");
	    	Log.i("info", "从数据库返回的长度:"+strFind.length, null);
	    	
	    	for (int j = 0; j < strFind.length; j++) {
				Log.i("info","第"+j+"条记录:"+ strFind[j], null);
	    	}
	    	
	    	String[] tempLine ;
	    	strFindPollutionsList = new String[strFind.length][7];
		 
	    	for (int j = 0; j < strFind.length -1; j++) {
				Log.i("info", strFind[j], null);
				tempLine = strFind[j].split(" ");
				
				//填充数据
					    Log.i("info", tempLine.toString(), null);
					     //污染源被发现的序号
						 strFindPollutionsList[j][0] =  String.valueOf(j);
						 //污染源名称
						 strFindPollutionsList[j][1] =  tempLine[3].split("=")[1];
						 //污染源类型
						 strFindPollutionsList[j][2] = tempLine[2].split("=")[1];
						 //污染源污染程度
						 strFindPollutionsList[j][3] = tempLine[4].split("=")[1];
						 //污染源污染坐标位置
						 strFindPollutionsList[j][4] = tempLine[5].split("=")[1];
						 //污染源污染被发现时间
						 strFindPollutionsList[j][5] = tempLine[6].split("=")[1];
						 //污染源巡航被发现的巡航截屏图片保存路径
						 
						 strFindPollutionsList[j][6] = tempLine[1].split("=")[1];
						 Log.i("info", "showListPollutions->截图路径:"+strFindPollutionsList[j][6],null);
						 //添加到intent中
						 myHistoryIntend.putExtra("findPollutionsList"+ String.valueOf(j),strFindPollutionsList[j]);
				 
//				 for (int i = 0; i < 7; i++) {
//					 System.out.println("strFindPollutionsList["+j+"]["+i+"]="+strFindPollutionsList[j][i]);  
//				 } 
			}
	    	
	    	
	    	String name = "历史记录:单击显示路径,长按显示污染源";//myHistoryIntend.getStringExtra("ListName");
			tvListName.setText(name);
			Log.i("info","要显示的名称"+name , null);
			//定义一个动态数组
			ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String,Object>>();
			DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String strTemp =" ";
			//在数组中存放数据
			for (int i = 0; i < strFind.length -1; i++) {
				HashMap<String, Object> map = new HashMap<String,Object>();
				map.put("Title", "污染源名称:"+strFindPollutionsList[i][1]);//名称
				
				//设置污染程度
				if (strFindPollutionsList[i][3].trim().equals("1")) {
					strTemp = "轻度污染";
				}else if (strFindPollutionsList[i][3].trim().equals("2")) {
					strTemp = "中度污染";
				} else if(strFindPollutionsList[i][3].trim().equals("3")) {
					strTemp = "重度污染";
				}
				map.put("Degree", "污染程度:"+strTemp);//污染程度
				map.put("Position", strFindPollutionsList[i][4].replaceAll(" ", ""));//位置经纬度
				
				map.put("FindTime", "发现时间:"+sdf.format(Long.valueOf(strFindPollutionsList[i][5])));//发现时间
				map.put("PathImage",R.drawable.icon_path);//巡航路径
				
				//添加进去
				listItem.add(map);
			}
			//获取截图的路径
			strImagePath = strFindPollutionsList[0][6];
			Log.i("info", "showPollutionsList->截图路径:"+strImagePath ,null);
			
			
			SimpleAdapter mSimpleAdapter = new SimpleAdapter(ShowHistory.this,//需要绑定到本视图
					listItem, //需要绑定的数据
					R.layout.item_show_pollution,//每一行的布局
					//动态数组中的数据源的键对应到定义布局的view中
					new String[] {"Title","Degree","Position","FindTime","PathImage"},
					//每一个数据源对应视图中每一个ID
					new int[]{R.id.tvItemTitle,R.id.tvItemDegree,R.id.tvItemPosition,
							R.id.tvItemFindTime,R.id.ivItemPathImage});
			//简单视图显示出来
			//lvShowPollutions.setAdapter(new ArrayAdapter<String>(this,
				//		android.R.layout.simple_list_item_1,strShow));
			
			//复杂视图显示
			lvShowPollutions.setAdapter(mSimpleAdapter);
			
			//设置点击响应事件
			//设置监听事件
			lvShowPollutions.setOnItemClickListener(new OnItemClickListener() {
				//单击显示巡航路径
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					String strTemp = strImagePath;
					String imgSrcPath = "/storage/emulated/0/data/123.jpg";
					strImagePath = strFindPollutionsList[(int) id][6].toString();
					//显示图片
					Log.i("info", " 单击监听事件:准备显示图片路径,"+strImagePath, null);
					
					showPathImge(strImagePath);
					
					lvShowInfo.setText("info:你点击了第"+id +"行");//设置Info信息栏中显示点击的行数
				}
			});
			
			//设置长按监听事件,显示截屏
			lvShowPollutions.setOnItemLongClickListener(showImgListener);
		
		
		 // 设置按钮监听器
		btnShowHistoryList.setOnClickListener(this);
	}
	
	//测试
	public void test(View view){
		if (flagLoad ==2) { //下载完毕
			show();
			flagLoad=0;
		} else {

		}
	}
	
	//下载数据
	public void upload(View view){
		if (flagLoad == 0) {	//可以开始下载数据
			showHistory();
		} else {

		}
		
	}
}
