package org.xidea.android.host;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import android.app.Application;

public class HostEnv {
	private static Application APP;

	public static void init(Application app){
		APP = app;
	}
	private static class PluginInfo<T extends Plugin> {
		static WeakHashMap<String, PluginInfo> map = new WeakHashMap<String, PluginInfo>();
		/**
		 * export. dependence. pluginPackage. pluginName.
		 */
		ApkPluginLoader loader;
		T plugin;
	}

	public static <T extends Plugin> T getPlugin(Class<T> type) {
		return getPlugin(type.getName());
	}

	public static <T extends Plugin> T getPlugin(String type) {
		ApkPluginLoader loader = requirePluginInfo(type).loader;
		try {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) loader.loadClass(type);
			@SuppressWarnings("unchecked")
			PluginInfo<T> p = PluginInfo.map.get(type);
			if (p.plugin == null) {
				p.plugin = clazz.newInstance();
				p.plugin.install(loader);
			}
			return p.plugin;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PluginLoader getPluginLoader(Class<? extends Plugin> type) {
		return requirePluginInfo(type.getName()).loader;
	}

	private static PluginInfo requirePluginInfo(String pluginName) {
		PluginInfo<?> p = PluginInfo.map.get(pluginName);
		if (p == null) {
			p = new PluginInfo();
			PluginInfo.map.put(pluginName, p);
			try {
				String packageName = pluginName.substring(0,pluginName.lastIndexOf('.'));
				p.loader = new ApkPluginLoader(APP, initPlugin(packageName,APP));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return p;
	}

	public static File initPlugin(String packageName,Application app) throws IOException{
		File pluginDir = app.getDir("plugin", 0);
		String fileName = packageName+".apk";
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
