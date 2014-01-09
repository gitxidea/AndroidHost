package org.xidea.android.host;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xidea.android.ApkPluginLoader;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import android.view.View;

public class PluginTest {
	
	public ApkPluginLoader c;
	public ApkPluginLoader a;
	public ApkPluginLoader b;
	public PluginTest(Application app) throws IOException{
		File fa = installPlugin("A", app);
		File fb = installPlugin("B", app);
		File fc = installPlugin("C", app);

		ApkPluginLoader a = new ApkPluginLoader(app, fa);
		a.addPublicPackage("com.example.a");
//		a.getBaseContext().getAssets().get
		ApkPluginLoader b = new ApkPluginLoader(app, fb);
		b.addDependence(a);
		b.addPublicPackage("com.example.b");
		ApkPluginLoader c = new ApkPluginLoader(app, fc);
		c.addDependence(b);
		c.addPublicPackage("com.example.c");
		this.a = a;
		this.b=b;
		this.c = c;
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
	public static File installPlugin(String name,Application app) throws IOException{
		File pluginDir = app.getDir("plugin", 0);
		String fileName = name+".apk";
		InputStream in = app.getResources().getAssets().open(fileName);
		File plugin = new File(pluginDir,fileName);
		FileOutputStream out = new FileOutputStream(plugin);
		byte[] buf = new byte[128];
		int c;
		while((c= in.read(buf))>=0){
			out.write(buf,0,c);
		}
		out.close();in.close();
		return plugin;
	}
	

}
