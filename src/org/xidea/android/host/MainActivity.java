package org.xidea.android.host;

import java.io.IOException;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		HostEnv.init(getApplication());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		long t1 = System.nanoTime();
		PluginTest p = null;
		try {
			p = new PluginTest(getApplication());
			Log.e("S","success");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("F","failed",e);
		}
		long t2= System.nanoTime();
		System.out.println("time1 used:"+(t2-t1)/100000000d);
		
		Class<?>[] cls = p.loadClasses();
		p.run(cls);

		t1=t2;t2= System.nanoTime();
		System.out.println("time2 used:"+(t2-t1)/100000000d);
		
		View[] view = p.getView(cls);

		View g = findViewById(R.id.container);
		System.out.println("test_text:"+p.a.getPluginContext().getString(0x7f040001));
		Context c = p.a.getPluginContext();
		AssetManager am = c.getAssets();
		p.a.getLayoutInflater().inflate(0x7f030000, null);
		//((TextView)view[1]).setText("Plugin 1###");
		((LinearLayout)g).addView(view[1]);
		t1=t2;t2= System.nanoTime();
		
		System.out.println("time2 used:"+(t2-t1)/100000000d);
		Plugin pa = HostEnv.getPlugin("com.example.a.A");
//		HostEnv.show(pa.index());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
