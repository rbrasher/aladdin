package com.comfycouch.scene;

import org.andengine.engine.Engine;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import com.comfycouch.aladdin.GameActivity;
import com.comfycouch.camera.FollowCamera;
import com.comfycouch.manager.ResourceManager;

public abstract class BaseScene extends Scene {
	protected Engine engine;
	protected GameActivity activity;
	protected FollowCamera camera;
	protected VertexBufferObjectManager vbom;
	protected ResourceManager resourceManager;
	
	public BaseScene() {
		resourceManager = ResourceManager.getInstance();
		activity = resourceManager.activity;
		camera = resourceManager.camera;
		vbom = resourceManager.vbom;
		engine = resourceManager.engine;
		createScene();
	}
	
	//Abstract Methods
	public abstract void createScene();
	public abstract void onBackKeyPressed();
	public abstract SceneType getSceneType();
	public abstract void disposeScene();
	public abstract void resume();
	public abstract void pause();
}
