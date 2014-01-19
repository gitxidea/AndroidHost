package com.example.a;



import org.xidea.android.host.HostApplication;
import org.xidea.android.host.HostApplication.ApplicationCallback;
import org.xidea.android.host.HostEnv;
import org.xidea.android.host.Plugin;
import org.xidea.android.host.PluginPackage;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.SupportMapFragment;
import com.example.a.R;
import com.example.a.R.layout;
import com.example.a.internal.DemoApplication;
import com.example.a.internal.Test;


import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class A implements Plugin{


	public void test() {
		Log.e("Test", "Plugin A Test");
	}
	public Test a = new Test();
	private ApplicationCallback mapCallback = new ApplicationCallback() {

	    public boolean m_bKeyRight = true;
	    BMapManager mBMapManager = null;

	    public static final String strKey = "请输入您的Key";

		public void initEngineManager(Context context) {
	        if (mBMapManager == null) {
	            mBMapManager = new BMapManager(context);
	        }

	        if (!mBMapManager.init(strKey,new MyGeneralListener())) {
	            Toast.makeText(HostApplication.getInstance().getApplicationContext(), 
	                    "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
	        }
		}
		
		@Override
		public void onLowMemory() {
		}
		
		@Override
		public void onConfigurationChanged(Configuration newConfig) {
		}
		
		@Override
		public void onTrimMemory(int level) {
		}
		
		@Override
		public void onCreate(Application app) {
			initEngineManager(app);
		}
	};

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {
        
        @Override
        public void onGetNetworkState(int iError) {
            if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                Toast.makeText(HostApplication.getInstance().getApplicationContext(), "您的网络出错啦！",
                    Toast.LENGTH_LONG).show();
            }
            else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                Toast.makeText(HostApplication.getInstance().getApplicationContext(), "输入正确的检索条件！",
                        Toast.LENGTH_LONG).show();
            }
            // ...
        }

        @Override
        public void onGetPermissionState(int iError) {
        	//非零值表示key验证未通过
            if (iError != 0) {
                //授权Key错误：
                Toast.makeText(HostApplication.getInstance().getApplicationContext(), 
                        "请在 DemoApplication.java文件输入正确的授权Key,并检查您的网络连接是否正常！error: "+iError, Toast.LENGTH_LONG).show();
            }
            else{
            	Toast.makeText(HostApplication.getInstance().getApplicationContext(), 
                        "key认证成功", Toast.LENGTH_LONG).show();
            }
        }
    }
	public View buildView(){
		LayoutInflater layoutInflater = HostEnv.findClassPackage(A.class).getLayoutInflater();
		TextView view =(TextView) layoutInflater.inflate(R.layout.text, null);
		return view;
	}

	@Override
	public void install(PluginPackage context) {
		// TODO Auto-generated method stu/
		//LayoutInflater layoutInflater = HostEnv.getPluginLoader(.class).getLayoutInflater();b
		HostApplication.getInstance().registerApplicationCallbacks(mapCallback );
	}

	@Override
	public void uninstall() {
		// TODO Auto-generated method stub
		
	}
	public String toString(){
		return a.toString();
	}

	@Override
	public Fragment index() {
		return SupportMapFragment.newInstance();
	}
}
