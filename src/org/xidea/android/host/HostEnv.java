package org.xidea.android.host;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import android.app.Application;

public class HostEnv {
	private static HostImpl impl;

	public static void init(Application app) {
		if(impl == null){
			impl = new HostImpl(app);
		}
	}

	public static <T extends Plugin> T getPlugin(Class<T> type) {
		return getPlugin(type.getName());
	}

	public static <T extends Plugin> T getPlugin(String type) {
		return impl.getPlugin(type);
	}

	public static PluginPackage findPluginPackage(Class<? extends Object> type) {
		return impl.findPluginPackage(type);
	}

	public static PluginPackage requirePluginPackage(String packageName){
		return impl.requirePluginPackage(packageName);
	}

}

class HostImpl {
	private Application app;

	private WeakHashMap<String, PluginInfo> infoMap = new WeakHashMap<String, PluginInfo>();
	private WeakHashMap<String, PluginPackage> loaderMap = new WeakHashMap<String, PluginPackage>();

	private static class PluginInfo<T extends Plugin> {

		/**
		 * export. dependence. pluginPackage. pluginName.
		 */
		PluginPackage loader;
		T plugin;
	}

	public HostImpl(Application app) {
		this.app = app;
	}

	protected <T extends Plugin> T getPlugin(String type) {
		try {
			@SuppressWarnings("unchecked")
			PluginInfo<T> p = infoMap.get(type);
			if (p.plugin == null) {
				PluginPackage loader = requirePluginInfo(type).loader;
				@SuppressWarnings("unchecked")
				Class<T> clazz = (Class<T>) ((ApkPluginLoader) loader)
						.loadClass(type);
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

	protected PluginPackage findPluginPackage(Class<? extends Object> type) {
		ClassLoader cl = type.getClassLoader();
		if (cl instanceof PluginPackage) {
			return (PluginPackage) cl;
		} else {

		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private PluginInfo<?> requirePluginInfo(String pluginName) {
		PluginInfo<?> p = infoMap.get(pluginName);
		if (p == null) {
				String packageName = pluginName.substring(0,
						pluginName.lastIndexOf('.'));
				p = new PluginInfo();
				p.loader = requirePluginPackage(packageName);
				infoMap.put(pluginName, p);
		}
		return p;
	}

	protected PluginPackage requirePluginPackage(String packageName) {
		PluginPackage loader = loaderMap.get(packageName);
		if (loader == null) {
			loader = new ApkPluginLoader(app, initResource(packageName, app));
		}
		return loader;
	}

	protected File initResource(String packageName, Application app) {
		File pluginDir = app.getDir("plugin", 0);
		String fileName = packageName + ".apk";
		try {
			InputStream in = app.getResources().getAssets().open(fileName);
			File plugin = new File(pluginDir, fileName);
			FileOutputStream out = new FileOutputStream(plugin);
			byte[] buf = new byte[128];
			int c;
			while ((c = in.read(buf)) >= 0) {
				out.write(buf, 0, c);
			}

			out.close();
			in.close();

			return plugin;
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} catch (RuntimeException ex) {
			ex.printStackTrace();
			throw ex;
		} finally {

		}
	}
}
