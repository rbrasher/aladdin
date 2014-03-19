package com.comfycouch.scene;

import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.sprite.Sprite;

import com.comfycouch.manager.SceneManager;
import com.comfycouch.manager.SceneManager.SceneType;

public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener {

	private static final int MENU_PLAY = 0;
	private MenuScene menuChild;
	private Sprite menubackground;
	
	
	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
			case MENU_PLAY:
				SceneManager.getInstance().loadGameScene(engine);
				return true;
			default:
				return false;
				
		}
	}

	@Override
	public void createScene() {
		createBackGround();
		createMenuChildScene();
	}
	
	private void createMenuChildScene() {
		menuChild = MenuScene(camera);
		menuChild.setPosition(0, 0);
		final IMenuItem playMenuItem = ScaleMenuItemDecorator(new SpriteMenuItem(MENU_PLAY, resourceManager.playbutton_region, vbom), 1.2f, 1);
		menuChild.addMenuItem(playMenuItem);
		menuChild.buildAnimations();
		menuChild.setBackgroundEnabled(true);
		SpriteBackground sBg = new SpriteBackground(menubackground);
		menuChild.setBackground(sBg);
		playMenuItem.setPosition(playMenuItem.getX() + 100, playMenuItem.getY() - 300);
		menuChild.setOnMenuItemClickListener(this);
		setChildScene(menuChild);
		
	}
	
	private void createBackGround() {
		menubackground = new Sprite(250, 400, 500, 900, resourceManager.menuBackgroundRegion, vbom);
		
	}

	@Override
	public void onBackKeyPressed() {
		System.gc();
		System.exit(0);
	}

	@Override
	public SceneType getSceneType() {
		return SceneManager.SceneType.SCENE_MENU;
	}

	@Override
	public void disposeScene() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

}
