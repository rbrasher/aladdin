package com.comfycouch.aladdin;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.comfycouch.camera.FollowCamera;
import com.comfycouch.manager.ResourceManager;
import com.comfycouch.manager.SceneManager;

public class GameActivity extends BaseGameActivity {

	public static final float CAMERA_WIDTH = 480;
	public static final float CAMERA_HEIGHT = 800;
	private FollowCamera mCamera;
	
	SharedPreferences mPrefs;
	
	public void setHighScore(int pScore) {
		SharedPreferences.Editor settingsEditor = mPrefs.edit();
		settingsEditor.putInt(Constants.KEY_HIGHSCORE, pScore);
		settingsEditor.commit();
	}
	
	public int getHighScore() {
		return mPrefs.getInt(Constants.KEY_HIGHSCORE, 0);
	}
	
	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		return new LimitedFPSEngine(pEngineOptions, 60);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
		}
		return false;
	}
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mCamera = new FollowCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_SENSOR, new FillResolutionPolicy(), mCamera);
		engineOptions.getAudioOptions().setNeedsMusic(true);
		engineOptions.getAudioOptions().setNeedsSound(true);
		
		engineOptions.getRenderOptions().getConfigChooserOptions().setRequestedMultiSampling(true);
		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
	
		return engineOptions;
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws IOException {
		ResourceManager.prepareManager(mEngine, this, mCamera, getVertexBufferObjectManager());
		
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws IOException {
		// TODO Auto-generated method stub
		SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
		
		//pOnCreateSceneCallback.onCreateSceneFinished(mScene);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws IOException {
		mEngine.registerUpdateHandler(new TimerHandler(3f, new ITimerCallback() {

			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				mEngine.unregisterUpdateHandler(pTimerHandler);
				SceneManager.getInstance().createMenuScene();
			}
			
		}));
		
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	@Override
	public synchronized void onResumeGame() {
		super.onResumeGame();
		
		if(SceneManager.getInstance().getCurrentScene() == SceneManager.getInstance().gameScene) {
			SceneManager.getInstance().gameScene.resume();
		}
	}
	
	@Override
	public synchronized void onPauseGame() {
		super.onPauseGame();
		SceneManager.getInstance().gameScene.destroy();
	}
	
	//@Override
	//protected void onDestroy() {
		//super.onDestroy();
		//System.gc();
		//System.exit(0);
	//}
	


}
