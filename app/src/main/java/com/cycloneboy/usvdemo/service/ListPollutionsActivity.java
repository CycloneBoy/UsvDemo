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
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.cycloneboy.usvdemo.R;
import com.cycloneboy.usvdemo.utils.WebServicePost;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ListPollutionsActivity extends Activity implements OnClickListener {
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

    //污染源的信息
    String[][] strFindPollutionsList;
	
    private int firstUpload;//是否是第一次上传
	/**
	 * @author CycloneBoy
	 * 创建
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_pollutions);
		
		//UI相关
		//listView
		lvShowPollutions = (ListView)findViewById(R.id.lvShowPollution);
		tvListName = (TextView)findViewById(R.id.tvListName);
		lvShowInfo = (TextView)findViewById(R.id.tvShowInfo);
		
		btnUploadRecord = (Button) findViewById(R.id.btnUploadRecord);
		btnShowHistoryList =(Button) findViewById(R.id.btnShowHistoryList);
		
		
		//取得启动该Activity的Intent对象
		intent = getIntent();
		firstUpload = 1;//第一次上传
		showPollutionsList(intent);//显示
		
		
		  // 设置按钮监听器
		btnUploadRecord.setOnClickListener(this);
		btnShowHistoryList.setOnClickListener(this);
	}
	
	/**
	 * 显示信息
	 * @author CycloneBoy
	 */
	public void showPollutionsList(Intent myIntent){
		//取出Intent中附加的数据
				String name = myIntent.getStringExtra("ListName");
				tvListName.setText(name);
				
				String FindPollutionsTotal = myIntent.getStringExtra("FindPollutionsTotal");
				int PollutionsTotal = Integer.parseInt(FindPollutionsTotal);
				
				String[] strShow = new String[PollutionsTotal];
				strFindPollutionsList = new String[PollutionsTotal][7];
				
				if ( PollutionsTotal> 0 ) {
					 
						for (int i = 0; i < strFindPollutionsList.length; i++) {
							 //污染源被发现的序号
							 strFindPollutionsList[i] = myIntent.getStringArrayExtra("findPollutionsList"+i);
							 
						}
						
						for (int i = 0; i < strShow.length; i++) {
							strShow[i] = strFindPollutionsList[i][1];
						}
				}
				
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
			String strTemp = strImagePath;
			String imgSrcPath = "/storage/emulated/0/data/123.jpg";
			//显示图片
			Log.i("info", " 长按监听事件:准备显示图片路径,"+strImagePath, null);
			showPathImge(strImagePath);
			return true;
		}
		
	};
	
	/**
	 * 创建菜单
	 * @author CycloneBoy
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 1, "查看历史记录")
			.setIcon(android.R.drawable.ic_menu_add);//设置图标
		menu.add(0,2,2,"清空当前记录")
			.setIcon(android.R.drawable.ic_menu_delete);
			
		return super.onCreateOptionsMenu(menu);
		
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
				new AlertDialog.Builder(ListPollutionsActivity.this)
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
	
	/**
	 * 
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0,3,1,"删除");
	}
	
	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		// TODO Auto-generated method stub
		final AdapterContextMenuInfo info  = (AdapterContextMenuInfo)item.getMenuInfo();
		if (item.getItemId() == 3) {
			new AlertDialog.Builder(this)
				.setTitle("警告")
				.setMessage("确定要删除吗？")
				.setIcon(R.drawable.plugin_notice)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//刷新显示
						lvShowInfo.setText("info:准备删除");//设置Info信息栏中显示点击的行数
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				})
				.show();
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
        case R.id.btnShowHistoryList:
//            // 检测网络，无法检测wifi
//            if (!checkNetwork()) {
//                Toast toast = Toast.makeText(ListPollutionsActivity.this,"网络未连接", Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
//                break;
//            }
//            Log.i("info","网络已连接", null);
//            // 提示框
//            dialog = new ProgressDialog(this);
//            dialog.setTitle("提示");
//            dialog.setMessage("正在登陆，请稍后...");
//            dialog.setCancelable(false);
//            dialog.show();
//            // 创建子线程，分别进行Get和Post传输
//            //new Thread(new MyThread()).start();
            
        	
          Intent historyItn = new Intent(ListPollutionsActivity.this, ShowHistory.class);
          // overridePendingTransition(anim_enter);
          //跳转到注册界面
          startActivity(historyItn);
            break;
        case R.id.btnUploadRecord:
        	
        	
        	if (firstUpload ==1) {//第一次上传
        		// 检测网络，无法检测wifi
                if (!checkNetwork()) {
                    Toast toast = Toast.makeText(ListPollutionsActivity.this,"网络未连接", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    break;
                }
                Log.i("info","网络已连接", null);
                // 提示框
                dialog = new ProgressDialog(this);
                dialog.setTitle("提示");
                dialog.setMessage("正在上传，请稍后...");
                dialog.setCancelable(false);
                dialog.show();
                // 创建子线程，分别进行Get和Post传输
                new Thread(new MyThreadPost(strFindPollutionsList)).start();
                
                firstUpload =2 ;//上传成功
                lvShowInfo.setText(lvShowInfo.getText()+":第一次上传成功");
                break;
			}else if (firstUpload == 2) {
				 Toast toast = Toast.makeText(ListPollutionsActivity.this,"你已经上传过一次了,无须重新上传", Toast.LENGTH_SHORT);
				 toast.setGravity(Gravity.CENTER, 0, 0);
                 toast.show();
                 lvShowInfo.setText(lvShowInfo.getText()+"不能重复上传");
			}
        	
        	

//            break;
        }
		
	}
	
	 // 子线程接收数据，主线程修改数据
    public class MyThread implements Runnable {
        @Override
        public void run() {
//            info = WebService.executeHttpGet(username.getText().toString(), password.getText().toString());
//            // info = WebServicePost.executeHttpPost(username.getText().toString(), password.getText().toString());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    infotv.setText(infotv.getText()+info);
//                    Log.i("info",infotv.getText()+info, null);
//                    dialog.dismiss();
//                }
//            });
        }
    }
    
    // 子线程接收数据，主线程修改数据
    public class MyThreadPost implements Runnable {
    	private String imgname;
    	private String name;
    	private String degree;
    	private String position;
    	private String findtime;
    	private String[][] allpollutions;
    	
        public MyThreadPost() {
			super();
		}
        
        public MyThreadPost(String[][] pos) {
			super();
			allpollutions=pos;
		}
        
        
        
		public MyThreadPost(String imgname, String name, String degree, String position, String findtime) {
			super();
			this.imgname = imgname;
			this.name = name;
			this.degree = degree;
			this.position = position;
			this.findtime = findtime;
		}


		public MyThreadPost(String[] uplode) {
			super();
			this.imgname = uplode[6];//"imgname";//
			this.name = "cyclone" ;//uplode[1];//
			this.degree = uplode[3];
			this.position = uplode[4].replaceAll(" ", "")
					.replaceAll("latitude", "lat")
					.replaceAll("longitude","lgt");//去除经纬度中间的空格//"30.553,114.39";//
			
			//进行格式转换
			//DateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
			//this.findtime ="2017-04-19";//sdf.format(Long.valueOf(uplode[5]));
			this.findtime = String.valueOf(Long.valueOf(uplode[5]));
		}


		@Override
        public void run() {
			
			if (allpollutions.length < 1) {
				return;
			}
			
			for (int i = 0; i < allpollutions.length; i++) {
				imgname = allpollutions[i][6];
				name = String.valueOf(i+1);
				degree = allpollutions[i][3];
				position = allpollutions[i][4].replaceAll(" ", "")
						.replaceAll("latitude", "lat")
						.replaceAll("longitude","lgt");//去除经纬度中间的空格//"30.553,114.39";//
				findtime = String.valueOf(Long.valueOf(allpollutions[i][5]));
				
				info = WebServicePost.executeHttpGet(imgname,name,degree,position,findtime);
			        
				 handler.post(new Runnable() {
		                @Override
		                public void run() {
		                	//lvShowInfo.setText(lvShowInfo.getText()+info);
		                	lvShowInfo.setText(info);
		                    Log.i("info",lvShowInfo.getText()+info, null);
		                    dialog.dismiss();
		                }
		            });
				
			}
			
			
			
//            info = WebServicePost.executeHttpGet(imgname,name,degree,position,findtime);
//            // info = WebServicePost.executeHttpPost(username.getText().toString(), password.getText().toString());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                	lvShowInfo.setText(lvShowInfo.getText()+info);
//                    Log.i("info",lvShowInfo.getText()+info, null);
//                    dialog.dismiss();
//                }
//            });
        }
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
}
