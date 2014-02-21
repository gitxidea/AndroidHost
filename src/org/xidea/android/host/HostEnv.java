package org.xidea.android.host;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

	public static PluginPackage findClassPackage(Class<? extends Object> type) {
		return impl.findClassPackage(type);
	}

	public static PluginPackage requirePluginPackage(String packageName){
		return impl.requirePluginPackage(packageName);
	}

	public static void show(String pluginPackage,String className) {
		impl.app.startActivity(HostActivity.create(pluginPackage,className,null));
	}

}

class HostImpl {
	Application app;

	private WeakHashMap<String, PluginInfo<?>> infoMap = new WeakHashMap<String, PluginInfo<?>>();
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

	@SuppressWarnings({"unchecked","rawtypes"})
	protected <T extends Plugin> T getPlugin(String type) {
		try {
			PluginInfo p = requirePluginInfo(type);
			if (p.plugin == null) {
				PluginPackage loader = p.loader;
				Class<T> clazz = (Class<T>) ((ApkPluginLoader) loader)
						.loadClass(type);
				p.plugin = clazz.newInstance();
				p.plugin.install(loader);
			}
			return (T)p.plugin;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected PluginPackage findClassPackage(Class<? extends Object> type) {
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
			File plugin = ApkPluginLoader.getPluginDest(app, packageName);
			initResource( app,packageName,plugin);
			loader = new ApkPluginLoader(app,packageName);
		}
		return loader;
	}

	protected File initResource(Application app,String packageName, File plugin) {
		String fileName = packageName + ".apk";
		try {
			InputStream in = app.getResources().getAssets().open(fileName);
			File pluginDir = plugin.getParentFile();
			copy(in, plugin);
			ZipFile file = new ZipFile(plugin);
			Enumeration<? extends ZipEntry> e = file.entries();
			while(e.hasMoreElements()){
				ZipEntry entry = e.nextElement();
				String name = entry.getName();
				if(!entry.isDirectory() && name.endsWith(".so")){
					//name = name.replace('\\', '/');
					//TODO:平台差异优化
					if(name.startsWith("lib/armeabi/")){
						InputStream ein = file.getInputStream(entry);
						//System.err.println(name);
						copy(ein,new File(pluginDir,name.substring(name.lastIndexOf('/')+1)));
					}
				}
			}
			file.close();
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

	private void copy(InputStream in, File plugin)
			throws FileNotFoundException, IOException {
		FileOutputStream out = new FileOutputStream(plugin);
		byte[] buf = new byte[128];
		int c;
		while ((c = in.read(buf)) >= 0) {
			out.write(buf, 0, c);
		}
		out.close();
		in.close();
	}
}
