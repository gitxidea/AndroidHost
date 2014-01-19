package org.xidea.android.host;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.XmlResourceParser;
import android.view.LayoutInflater;

import dalvik.system.DexClassLoader;

public class ApkPluginLoader extends ApkClassLoader implements PluginPackage {

	private List<String> plugins = new ArrayList<String>(); 


	public ApkPluginLoader(Application app, File source) {
		super(new ApkContext(app, source), new ArrayList<ClassLoader>());
		init();
		
	}

	protected void init() {
		InputStream in = null;
		//XmlResourceParser xml = null;
		try {
//			xml = context.getAssets().openXmlResourceParser("res/xml/package.xml");
			 in = context.getAssets().open("package.xml");
			  XmlPullParserFactory factory = XmlPullParserFactory.newInstance(); 
			  factory.setNamespaceAware(true); 
			  XmlPullParser xml = factory.newPullParser(); 
			  xml.setInput(in, "UTF-8");  
			outer:
			while(true){
				switch(xml.next()){
				case XmlPullParser.START_TAG:
					String tagName = xml.getName();
					if("package".equals(tagName)){
						String packageName = xml.getAttributeValue(0);
					}else if("plugin".equals(tagName)){
						String pluginName = xml.getAttributeValue(0);
						this.plugins.add(pluginName);
					}else if("dependence".equals(tagName)){
						String dependence = xml.getAttributeValue(0);
						this.dependences.add(HostEnv.requirePluginPackage(dependence).getClassLoader());
					}else if("export".equals(tagName)){
						String export = xml.getAttributeValue(0);
						super.addPublicPackage(export);
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break outer;
				}
				
			}

			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			//xml.close();
		}
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

	protected void addDependence(ClassLoader loader) {
		dependences.add(loader);
	}

	@Override
	public ClassLoader getClassLoader() {
		return this;
	}


	@Override
	public Plugin getDefaultPlugin() {
		return HostEnv.getPlugin(plugins.get(0));
	}

}

class ApkClassLoader extends DexClassLoader {

	private PublishConfig publishConfig = new PublishConfig();
	protected List<ClassLoader> dependences;
	protected ApkContext context;

	ApkClassLoader(ApkContext context, List<ClassLoader> dependences) {
		super(context.dexPath, context.optimizedDirectory, context.libraryPath,ApkClassLoader.class.getClassLoader());
		this.dependences = dependences;
		context.classLoader = this;
		this.context = context;
	}

	public Class<?> loadClass(String className) throws ClassNotFoundException {
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
		File pluginDir = source.getParentFile();
		this.dexPath = source.getAbsolutePath();
		this.optimizedDirectory = pluginDir.getAbsolutePath();
		this.libraryPath = optimizedDirectory;
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
