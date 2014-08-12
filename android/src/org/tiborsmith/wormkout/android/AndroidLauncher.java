package org.tiborsmith.wormkout.android;

import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.tiborsmith.wormkout.Wormkout;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        //disabling them in gdx since I have to control them myself
        config.useCompass = false;
        config.useAccelerometer = false;

        //Immersive mode
        config.useImmersiveMode = true;

        //keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initialize(new Wormkout(new MyAndroidSensors(this.getContext())), config);
	}
}
