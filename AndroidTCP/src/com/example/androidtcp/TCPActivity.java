package com.example.androidtcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import android.R.bool;
import android.R.string;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class TCPActivity extends Activity {
	private TextView txtReceiveInfo;
	private EditText edtRemoteIP,edtRemotePort,edtSendInfo;
	private Button btnConnect,btnSend;
	private boolean isConnected=false;
	private Socket socketClient=null;
	private String receiveInfoClient;
	static BufferedReader bufferedReaderClient	= null;
	static PrintWriter printWriterClient = null;
	
	public List<Sensor> allSensors;
	private SensorManager mSensorManager;
	private LocationManager locationManager;
    private float mLux;
    private double xLoc=36.36314905086591;
    private double yLoc=120.6853992232026;
    private double Ax,Ay,Az;
    private double Ox,Oy,Oz;

	private  TextView tv1,tv2,tv3,tv4,tv5,tv6,tv7;
	  
    private ScrollView scrollview1;
    
    
    private TextView mTextView;
    private Sensor mSensor;
	
    private int experiment =1;
	
    private String INFO ="";
    private String msg;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tcp);
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();   

		StrictMode.setThreadPolicy(policy);
		
		tv1 = (TextView)findViewById(R.id.textView1);
		tv2 = (TextView)findViewById(R.id.textView2);
		tv3 = (TextView)findViewById(R.id.textView3);
		tv4 = (TextView)findViewById(R.id.textView4);
		tv5 = (TextView)findViewById(R.id.textView5);
		tv6 = (TextView)findViewById(R.id.textView6);
		tv7 = (TextView)findViewById(R.id.textView7);
		
		scrollview1 = (ScrollView)findViewById(R.id.scrollview1);
		
		btnConnect=(Button)findViewById(R.id.btnConnect);
		btnSend=(Button)findViewById(R.id.btnSend);
		txtReceiveInfo=(TextView)findViewById(R.id.txtReceiveInfo);
		edtRemoteIP=(EditText)findViewById(R.id.edtRemoteIP);
		edtRemotePort=(EditText)findViewById(R.id.edtRemotePort);
		edtSendInfo=(EditText)findViewById(R.id.edtSendInfo);
		
		mSensorManager = (SensorManager) 
                getSystemService(Context.SENSOR_SERVICE);
		
		List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);  
		EditText et = (EditText)findViewById(R.id.edtSendInfo);
		for (Sensor sensor : deviceSensors) {  
			//et.setText(et.getText()+","+sensor.getName());
		
		}
		//***************Accelerometer************************
		mSensorManager.registerListener(AccelerateListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		
		//****************************************************
		
		
		//***************Light********************************
        mSensorManager.registerListener(Lightlistener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);
        //****************************************************
        
        //***************Location*****************************
        String serviceString = Context.LOCATION_SERVICE;// 获取的是位置服务
        locationManager = (LocationManager) getSystemService(serviceString);// 调用getSystemService()方法来获取LocationManager对象
        String provider = LocationManager.GPS_PROVIDER;// 指定LocationManager的定位方法
        Location location = locationManager.getLastKnownLocation(provider);// 调用getLastKnownLocation()方法获取当前的位置信息
        
        TextView tv2 = (TextView)findViewById(R.id.textView2);
     
        tv2.setText(xLoc+"."+yLoc);
        
        if(location != null)
        {
        	tv2.setText("!!!");
            xLoc = location.getLatitude();
            yLoc = location.getLongitude();
        	tv2.setText(location.getLatitude()+","+location.getLongitude());
        }
        
        locationManager.requestLocationUpdates(provider, 2000, 10,locationListener);// 产生位置改变事件的条件设定为距离改变10米，时间间隔为2秒，设定监听位置变化
        
       // String provider = LocationManager.GPS_PROVIDER;
        
        
        //****************************************************
        
        //**************ACCELEROMETER*************************
        mSensorManager.registerListener(Orientlistener, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        		SensorManager.SENSOR_DELAY_NORMAL);
        //****************************************************
        
        //**************Temperature***************************
        mSensorManager.registerListener(Temperaturelistener, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);  
        
        //****************************************************
        
        //**************proximity*****************************
        mSensorManager.registerListener(Proximitylistener, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);  
        //****************************************************
        
        //**************magnetic******************************
        mSensorManager.registerListener(Magneticlistener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);  
        //****************************************************
        
        
        List<Sensor> list = mSensorManager.getSensorList(Sensor.TYPE_ALL);  //获取传感器的集合
        msg="msg#name#power#vendor#MaximumRange#mindelay#type";
        for (Sensor sensor:list){
            if(sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)||
            		sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)||
            		sensor == mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION))
            {
            	msg += sensor.getName()+"#"+sensor.getPower()+"#"+sensor.getVendor()+"#"+sensor.getMaximumRange()
            			+"#"+sensor.getMinDelay()+"#"+sensor.getType();
            }
        } 
        
        
        

        mHandler.postDelayed(r, 1000);//延时100毫秒
	}
	
	Handler mHandler = new Handler();
    Runnable r = new Runnable() {

       @Override
       public void run() {
               //do something
    	   SendMsg(" ");
               //每隔1s循环执行run方法
           mHandler.postDelayed(this, 5000);
       }
   };
	
	
	
	//连接按钮单击事件
	public void ConnectButtonClick(View source)
	{
		if(isConnected)
		{
			isConnected=false;
			if(socketClient!=null)
			{
				try 
				{
					socketClient.close();
					socketClient=null;	
					printWriterClient.close();
					printWriterClient = null;
				} 
				catch (IOException e) 
				{
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			new tcpThread().interrupt();
			btnConnect.setText("开始连接");
			edtRemoteIP.setEnabled(true);
			edtRemotePort.setEnabled(true);
			txtReceiveInfo.setText("ReceiveInfo:\n");
		}
		else
		{
			isConnected=true;
			btnConnect.setText("停止连接");
			edtRemoteIP.setEnabled(false);
			edtRemotePort.setEnabled(false);
			new tcpThread().start();
		}
	}
	//发送信息按钮单击事件
	public void SendButtonClick(View source)
	{
		/*if ( isConnected && socketClient!=null) 
		{
			String sendInfo =edtSendInfo.getText().toString();//取得编辑框中我们输入的内容
			
			try 
			{				    	
				printWriterClient.print(sendInfo);//发送给服务器
		    	printWriterClient.flush();
				receiveInfoClient = "Send "+"\""+sendInfo+"\""+" to server"+"\n";//消息换行
				Message msg = new Message();
				handler.sendMessage(msg);
			}
			catch (Exception e) 
			{
				receiveInfoClient = e.getMessage() + "\n";//消息换行
				Message msg = new Message();
				handler.sendMessage(msg);
			}
		}*/
		
		
		
		String sendInfo =edtSendInfo.getText().toString();//取得编辑框中我们输入的内容
		
		if(sendInfo.contains("msg"))
		{
			SendMsg(msg);
		}
		else{
			String []ss = sendInfo.split(",");
			xLoc = Double.parseDouble(ss[0]);
			yLoc = Double.parseDouble(ss[1]);
			
			TextView tv2 = (TextView)findViewById(R.id.textView2);
		     
	        tv2.setText(xLoc+"#"+yLoc);
		}
	}
	//线程:监听服务器发来的消息
	private class tcpThread	extends Thread 
	{
		public void run()
		{
			try 
			{				
				//连接服务器
				socketClient = new Socket(edtRemoteIP.getText().toString(), Integer.parseInt(edtRemotePort.getText().toString()));	
				//取得输入、输出流
				bufferedReaderClient = new BufferedReader(new InputStreamReader(socketClient.getInputStream()));
				printWriterClient = new PrintWriter(socketClient.getOutputStream(), true);
				receiveInfoClient = "Connect to the server successfully!\n";//消息换行
				Message msg = new Message();
				handler.sendMessage(msg);	
			}
			catch (Exception e) 
			{
				receiveInfoClient = "error in 1" + "\n";//消息换行
				Message msg = new Message();
				handler.sendMessage(msg);
			}			
			char[] buffer = new char[256];
			int count = 0;
			while (isConnected)
			{
				try
				{
					if((count = bufferedReaderClient.read(buffer))>0)
					{			
						String message = getInfoBuff(buffer, count);
						receiveInfoClient = "Receive "+"\""+message+"\"" +" from server"+ "\n";//消息换行
						Message msg = new Message();
						handler.sendMessage(msg);
						
						message = InfoClean(message);
						if(message.length()!=0){
							String SensorInfo = GetInfo(message);
							
							if(SensorInfo!=null)
								SendMsg(SensorInfo);
						}
					}
				}
				catch (Exception e)
				{
					receiveInfoClient = "error in 2" + "\n"+e.getMessage()+"\n";//消息换行
					Message msg = new Message();
					handler.sendMessage(msg);
				}
			}
		}
	};
	Handler handler = new Handler()
	{										
		  public void handleMessage(Message msg)										
		  {											
			  txtReceiveInfo.append("TCPClient: "+receiveInfoClient);	// 刷新
		  }									
	 };
	private String getInfoBuff(char[] buff, int count)
	{
		char[] temp = new char[count];
		for(int i=0; i<count; i++)
		{
			temp[i] = buff[i];
		}
		return new String(temp);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tc, menu);
		return true;
	}
	
	public void ShowMsg(String str)
	{
		receiveInfoClient = str+"\n";//消息换行
		Message msg = new Message();
		handler.sendMessage(msg);
	}
	
	public String InfoClean(String response)
	{
		String ss="";
		int length = response.length();
		for(int i=0;i<length;++i)
			if(response.charAt(i)!='?')
				ss += response.charAt(i);
		return ss;
	}
	
	
	public String GetInfo(String response)
	{
		String[]ss = response.trim().split("@");

		if(ss[0].equals("server"))
		{
			String str ="android@";
			str += ss[1]+"@";
			str += GetSensorInfo();
			
			return str;
		}
		return null;
	}
	public boolean isstart=false;
	
	public String GetSensorInfo()
	{
		String str="";
		
		if(experiment==0){
			final int d = 19;
			double []sensorlist = new double[d];
			Random random =new Random();
			
			str=d+"#";
			for(int i=0;i<d;++i)
				sensorlist[i] = random.nextDouble();
			
			for(int i=0;i<d;++i)
				if(i<d-1)
					str+=sensorlist[i]+",";
				else
					str +=sensorlist[i];
			//isstart =true;
		}
		else if(experiment==1){
			
			final int d =9;
			str = d +"#";
			str += mLux+","+xLoc+","+yLoc+","+Ax+","+Ay+","+Az+","+Ox+","+Oy+","+Oz;
		}
		
		return str;
	}
	
	public void SendMsg(String sendInfo)
	{
		if ( isConnected && socketClient!=null) 
		{
			//String sendInfo =edtSendInfo.getText().toString();//取得编辑框中我们输入的内容
			
			try 
			{				    	
				printWriterClient.print(sendInfo);//发送给服务器
		    	printWriterClient.flush();
				receiveInfoClient = "Send "+"\""+sendInfo+"\""+" to server"+"\n";//消息换行
				Message msg = new Message();
				handler.sendMessage(msg);
			}
			catch (Exception e) 
			{
				receiveInfoClient = e.getMessage() + "\n";//消息换行
				Message msg = new Message();
				handler.sendMessage(msg);
			}
		}
	}

	@Override
    protected void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(Lightlistener);
            mSensorManager.unregisterListener(Orientlistener);
            mSensorManager.unregisterListener(Temperaturelistener);
            mSensorManager.unregisterListener(Proximitylistener);
            mSensorManager.unregisterListener(Magneticlistener);
        }
        super.onDestroy();
    }

	Handler light = new Handler(){
		public void handleMessage(Message msg)										
		  {											
			  tv1.setText(mLux+" lux");
		  }	
	};
	
	Handler locate = new Handler(){
		public void handleMessage(Message msg)										
		  {		
            tv2.setText(xLoc+","+yLoc);
		  }	
	};
	
	
	
    private SensorEventListener Lightlistener = 
            new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            	mLux = event.values[0];
            	Message msg = new Message();
            	light.handleMessage(msg);
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
	
    
    private final LocationListener locationListener = 
    		new LocationListener() {
    	 
        @Override
        public void onLocationChanged(Location location) {
            // TODO Auto-generated method stub
        	//TextView tv2 = (TextView)findViewById(R.id.textView2);
            
            xLoc = location.getLatitude();
            yLoc = location.getLongitude();
            
            Message msg = new Message();
            locate.handleMessage(msg);
        }
 
        @Override
        public void onProviderDisabled(String arg0) {
            // TODO Auto-generated method stub
             
        }
 
        @Override
        public void onProviderEnabled(String arg0) {
            // TODO Auto-generated method stub
             
        }
 
        @Override
        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            // TODO Auto-generated method stub
 
        }
 
    };
    private SensorEventListener AccelerateListener = 
    		new SensorEventListener() {

				@Override
				public void onAccuracyChanged(Sensor arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onSensorChanged(SensorEvent event) {
					// TODO Auto-generated method stub
					if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
					{
						float[] values = event.values;
						float ax = values[0];
		                float ay = values[1];
		                float az = values[2];
		                
		                TextView tv3 = (TextView)findViewById(R.id.textView3);
		                tv3.setText(ax+","+ay+","+az);
		                
		                Ax = ax;
		                Ay = ay;
		                Az = az;
					}
				}
    	
    };
    
	
    private SensorEventListener Orientlistener = 
            new SensorEventListener() {

        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                // 获取光线强度
            	float[] values = event.values;
                float ax = values[0];
                float ay = values[1];
                float az = values[2];
                
                TextView tv4 = (TextView)findViewById(R.id.textView4);
                tv4.setText(ax+","+ay+","+az);

                Ox = ax;
                Oy = ay;
                Oz = az;
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
	
    private SensorEventListener Temperaturelistener = 
            new SensorEventListener() {

		public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                
            	float temperatureValue = event.values[0]; // 得到温度  
                //TextView tv7 = (TextView)findViewById(R.id.textView7);
                //tv7.setText(temperatureValue+"°C");

            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    
    private SensorEventListener Proximitylistener = 
            new SensorEventListener() {

		public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                
            	float proximityValue = event.values[0]; 
                //TextView tv6 = (TextView)findViewById(R.id.textView6);
                //tv6.setText(String.valueOf(proximityValue));

            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
    
    private SensorEventListener Magneticlistener = 
            new SensorEventListener() {

		public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                
            	float magneticValue1 = event.values[0]; 
            	float magneticValue2 = event.values[1]; 
            	float magneticValue3 = event.values[2]; 
            	
                TextView tv5 = (TextView)findViewById(R.id.textView5);
                tv5.setText(String.valueOf(magneticValue1+","+magneticValue2+","+magneticValue3));

            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
