package org.xidea.android.host;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import dalvik.system.DexClassLoader;


import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import android.view.View;

public class PluginTest {
	
	public PluginPackage c;
	public PluginPackage a;
	public PluginPackage b;
	public PluginTest(Application app) throws IOException{
		a = HostEnv.requirePluginPackage("com.example.a");
		b = HostEnv.requirePluginPackage("com.example.b");
		c = HostEnv.requirePluginPackage("com.example.c");
//		((DexClassLoader)a.getClassLoader()).findLibrary(name)
	}
	@SuppressLint("NewApi")
	public Class<?>[] loadClasses(){
		try {

			Class<?>[] cls = new Class<?>[]{
					b.loadClass("com.example.b.B"),
					a.loadClass("com.example.a.A"),
					c.loadClass("com.example.c.C")
			};
			//c.loadClass("com.example.c.C").getMethod("test").invoke(clazz.newInstance());
			Log.e("S0","success");
			return cls;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("F0","failed",e);
			return null;
		}
	}
	public void run(Class<?>...cls){
		for (int i = 0; i < cls.length; i++) {
			try {
				cls[i].getMethod("test").invoke(cls[i].newInstance());
				Log.e("S1","success");
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("F1","failed:"+cls[i],e);
			}
		}
	}
	public View[] getView(Class<?>...cls){
		View[] views = new View[cls.length];
		for (int i = 0; i < cls.length; i++) {
			try {
				views[i]=(View) cls[i].getMethod("buildView").invoke(cls[i].newInstance());
				Log.e("S2","success");
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("F2","failed:"+cls[i],e);
			}
		}
		return views;
	}

}
