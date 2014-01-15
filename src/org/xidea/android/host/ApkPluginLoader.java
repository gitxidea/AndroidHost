package org.xidea.android.host;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.view.LayoutInflater;

import dalvik.system.DexClassLoader;

public class ApkPluginLoader implements PluginLoader {
	private ArrayList<ClassLoader> dependences = new ArrayList<ClassLoader>();
	private ApkContext context;
	ApkClassLoader classLoader;

	public ApkPluginLoader(Application app, File source) {
		this.context = new ApkContext(app, source);
		this.classLoader = new ApkClassLoader(context, dependences);
		context.classLoader = classLoader;
	}

	public List<ClassLoader> getDependences() {
		return dependences;
	}

	public LayoutInflater getLayoutInflater() {
		return (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public Context getPluginContext() {
		return context;
	}

	public void addDependence(ClassLoader loader) {
		dependences.add(loader);
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return ((ApkClassLoader)this.classLoader).loadClass0(className);
	}
	@Override
	public Plugin getPlugin() {
		return null;
	}

}

class ApkClassLoader extends DexClassLoader {

	private PublishConfig publishConfig = new PublishConfig();
	private List<ClassLoader> dependences;

	ApkClassLoader(ApkContext config, List<ClassLoader> dependences) {
		super(config.dexPath, config.optimizedDirectory, config.libraryPath,ApkClassLoader.class.getClassLoader());
		this.dependences = dependences;
	}

	Class<?> loadClass0(String className) throws ClassNotFoundException {
		return super.loadClass(className, false);
	}

	protected Class<?> findClass(String name) throws ClassNotFoundException {
		for (ClassLoader loader : dependences) {
			Class<?> clazz;
			if (loader instanceof ApkClassLoader) {
				clazz = ((ApkClassLoader) loader).getPublicClass(name);

			} else {
				clazz = loader.loadClass(name);
			}
			if (clazz != null) {
				return clazz;
			}
		}
		return super.findClass(name);
	}

	protected Class<?> getPublicClass(String name) {
		boolean contains = publishConfig.containsClass(name);
		if (contains) {
			try {
				return super.loadClass(name, false);
			} catch (ClassNotFoundException cnf) {
			}
		}
		return null;
	}

	public void addPublicPackage(String name) {
		publishConfig.addPublicClass(name + ".[^.]+");
	}

	private static class PublishConfig {
		private StringBuilder buf = new StringBuilder();
		private Pattern publicPattern;

		public void addPublicClass(String name) {
			if (buf.length() > 0) {
				buf.append('|');
			}
			buf.append("^" + name.replaceAll("[\\.]", "\\.") + "$");
			publicPattern = null;
		}

		private boolean containsClass(String name) {
			Pattern p = publicPattern;
			if (p == null) {
				publicPattern = p = Pattern.compile(buf.length() == 0 ? "."
						: buf.toString());
			}
			boolean contains = p.matcher(name).find();
			return contains;
		}

	}
}

class ApkContext extends ContextWrapper {
	private AssetManager assetManager;
	private Resources resources;
	static Method addAssetPath;
	static {
		try {
			addAssetPath = AssetManager.class.getDeclaredMethod("addAssetPath",
					String.class);
			addAssetPath.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Plugin init failed", e);
		}
	}
	final String dexPath;
	final String optimizedDirectory;
	final String libraryPath;
	ClassLoader classLoader;
	LayoutInflater layoutInflater;
	private Theme theme;

	public ApkContext(Application app, File source) {
		super(app);
		File pluginDir = app.getDir("plugin", 0);
		if (!pluginDir.exists()) {
			pluginDir.mkdir();
		}
		this.dexPath = source.getAbsolutePath();
		this.optimizedDirectory = pluginDir.getAbsolutePath();
		this.libraryPath = null;
		try {
			this.assetManager = AssetManager.class.newInstance();
			addAssetPath.invoke(assetManager, dexPath);
		} catch (Exception e) {
			throw new RuntimeException("Plugin init failed", e);
		}
	}

	public Resources.Theme getTheme() {
		if (this.theme == null) {
			Theme oldTheme = super.getTheme();
			this.theme = this.getResources().newTheme();
			this.theme.setTo(oldTheme);
		}
		return this.theme;
	}

	public Object getSystemService(String name) {
		if (Context.LAYOUT_INFLATER_SERVICE.equals(name)) {
			if (layoutInflater == null) {
				LayoutInflater service = (LayoutInflater) super.getSystemService(name);
				this.layoutInflater = service.cloneInContext(this);
			}
			return layoutInflater;
		}
		return  super.getSystemService(name);
	}

	public AssetManager getAssets() {
		return assetManager;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public Resources getResources() {
		if (resources == null) {
			Resources parent = super.getResources();
			this.resources = new Resources(assetManager,
					parent.getDisplayMetrics(), parent.getConfiguration());
		}
		return resources;
	}

}
