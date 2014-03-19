package com.comfycouch.scene;

import java.util.Iterator;
import java.util.LinkedList;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.util.adt.align.HorizontalAlign;
import org.andengine.util.adt.color.Color;

import android.os.Debug;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.comfycouch.aladdin.GameActivity;
import com.comfycouch.manager.SceneManager;
import com.comfycouch.manager.SceneManager.SceneType;
import com.comfycouch.object.RomaPillar;
import com.comfycouch.object.RomaPillarFactory;

public class GameScene extends BaseScene implements IOnSceneTouchListener, ContactListener {

	private static final int TIME_TO_RESURRECTION = 200;
	private HUD gameHUD;
	private Text scoreText;
	private Text tapToPlayText;
	private Text highScoreText;
	private Text yourScoreText;
	private PhysicsWorld physicsWorld;
	LinkedList<RomaPillar> pillars = new LinkedList<RomaPillar>();
	private TiledSprite aladdin;
	protected Body aladdinBody;
	private boolean scored;
	private int score;
	
	long timestamp = 0;
	private STATE state = STATE.NEW;
	protected boolean musicIsOn = true;
	
	enum STATE {
		NEW,
		PAUSED,
		PLAY,
		DEAD,
		AFTERLIFE
	}
	
	@Override
	public void createScene() {
		resourceManager.aladdinMusic.setVolume(0.1f);
		resourceManager.aladdinMusic.setLooping(true);
		resourceManager.aladdinMusic.play();
		
		createPhysics();
		RomaPillarFactory.getInstance().create(physicsWorld);
		createBackground();
		createHUD();
		createBounds();
		createActor();
		resourceManager.camera.setChaseEntity(aladdin);
		setOnSceneTouchListener(this);
		
		try {
			activity.getHighScore();
		} catch (Exception e) {
			activity.setHighScore(0);
			Debug.e(e);
		}
	}
	
	private void createPhysics() {
		physicsWorld = new PhysicsWorld(new Vector2(0, 0), true);
		physicsWorld.setContactListener(this);
		
		//registerUpdateHandler(physicsWorld);
	}
	
	private void createBounds() {
		float bigNumber = 999999;	//a big number
		resourceManager.parallaxFrontLayerRegion.setTextureWidth(bigNumber);
		Sprite ground = new Sprite(0, -150, resourceManager.parallaxFrontLayerRegion, vbom);
		ground.setAnchorCenter(0, 0);
		ground.setZIndex(10);
		attachChild(ground);
		
		Body groundBody = PhysicsFactory.createBoxBody(physicsWorld, ground, BodyType.StaticBody, RomaPillar.WALL_FIXTURE);
		groundBody.setUserData(RomaPillar.BODY_WALL);
		
		//just to limit the movement at the top
		@SuppressWarnings("unused")
		Body ceilingBody = PhysicsFactory.createBoxBody(physicsWorld, bigNumber / 2, 820, bigNumber, 20, BodyType.StaticBody, RomaPillar.CEILING_FIXTURE);
		
	}
	
	private void createActor() {
		aladdin = new TiledSprite(200, 400, 90, 90, resourceManager.playerRegion, vbom);
		aladdin.setZIndex(999);
		
		aladdin.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if(aladdinBody.getLinearVelocity().y > -0.01) {
					aladdin.setCurrentTileIndex(1);
				} else {
					aladdin.setCurrentTileIndex(0);
				}
			}

			@Override
			public void reset() {
				
			}
			
		});
		
		aladdinBody = PhysicsFactory.createCircleBody(physicsWorld, aladdin, BodyType.DynamicBody, RomaPillar.ALADDIN_FIXTURE);
		aladdinBody.setUserData(RomaPillar.BODY_ACTOR);
		physicsWorld.registerPhysicsConnector(new PhysicsConnector(aladdin, aladdinBody));
		attachChild(aladdin);
	}
	
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
		
		if(scored) {
			addPillar();
			sortChildren();
			scored = false;
			score++;
			scoreText.setText("Score: " + String.valueOf(score));
		}
		
		//if first pillar is out of screen, delete it
		if(!pillars.isEmpty()) {
			RomaPillar fp = pillars.getFirst();
			if(fp.getX() + fp.getWidth() < resourceManager.camera.getXMin()) {
				RomaPillarFactory.getInstance().recycle(fp);
				pillars.remove();
			}
		}
		
		if(state == STATE.DEAD && timestamp + TIME_TO_RESURRECTION < System.currentTimeMillis()) {
			state = STATE.AFTERLIFE;
		}
	}
	
	private void createHUD() {
		Sprite musicOn = new Sprite(420, 70, 70, 70, resourceManager.musicOnRegion, vbom) {
			@Override
			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				if(pSceneTouchEvent.isActionUp()) {
					if(resourceManager.aladdinMusic.isPlaying() == true) {
						musicIsOn = false;
						resourceManager.aladdinMusic.pause();
					} else {
						musicIsOn = true;
						resourceManager.aladdinMusic.play();
					}
				}
				return true;
			}
		};
		
		gameHUD = new HUD();
		scoreText = new Text(20, 30, resourceManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
		scoreText.setAnchorCenter(0, 0);
		scoreText.setText("Score: 0");
		
		tapToPlayText = new Text(GameActivity.CAMERA_WIDTH / 2 - 150, GameActivity.CAMERA_HEIGHT / 2, resourceManager.font, "TAP TO PLAY", new TextOptions(HorizontalAlign.LEFT), vbom);
		tapToPlayText.setAnchorCenter(0, 0);
		tapToPlayText.setText("TAP TO PLAY");
		
		yourScoreText = new Text(GameActivity.CAMERA_WIDTH / 2 - 150, GameActivity.CAMERA_HEIGHT / 2 + 50, resourceManager.font, "Your Score: ", new TextOptions(HorizontalAlign.LEFT), vbom);
		yourScoreText.setAnchorCenter(0, 0);
		yourScoreText.setText("Your Score: 0");
		
		highScoreText = new Text(GameActivity.CAMERA_WIDTH / 2 - 150, GameActivity.CAMERA_HEIGHT / 2 + 50, resourceManager.font, "High Score: ", new TextOptions(HorizontalAlign.LEFT), vbom);
		highScoreText.setAnchorCenter(0, 0);
		highScoreText.setText("High Score: 0");
		
		gameHUD.attachChild(tapToPlayText);
		gameHUD.attachChild(scoreText);
		gameHUD.attachChild(highScoreText);
		gameHUD.attachChild(yourScoreText);
		gameHUD.attachChild(musicOn);
		camera.setHUD(gameHUD);
		highScoreText.setVisible(false);
		yourScoreText.setVisible(false);
		
	}
	
	public void createBackground() {
		final ParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0f, 0f, 5f, 60);
		this.setBackground(autoParallaxBackground);
		
		final Sprite parallaxBackLayerSprite = new Sprite(0, 0, resourceManager.parallaxBackLayerRegion, vbom);
		parallaxBackLayerSprite.setOffsetCenter(0, 0);
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, parallaxBackLayerSprite));
		
		this.setBackground(autoParallaxBackground);
		this.setBackgroundEnabled(true);
	}
	
	@Override
	public void reset() {
		super.reset();
		physicsWorld.setGravity(new Vector2(0, 0));
		
		Iterator<RomaPillar> pi = pillars.iterator();
		while(pi.hasNext()) {
			RomaPillar p = pi.next();
			RomaPillarFactory.getInstance().recycle(p);
			pi.remove();
		}
		
		RomaPillarFactory.getInstance().reset();
		
		aladdinBody.setTransform(200 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 400 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		
		addPillar();
		addPillar();
		addPillar();
		
		score = 0;
		
		tapToPlayText.setText("TAP TO PLAY");
		tapToPlayText.setVisible(true);
		
		yourScoreText.setText("Your Score: 0");
		yourScoreText.setVisible(false);
		
		highScoreText.setText("High Score: " + activity.getHighScore());
		highScoreText.setVisible(false);
		
		sortChildren();
		
		unregisterUpdateHandler(physicsWorld);
		physicsWorld.onUpdate(0);
		
		if(musicIsOn == true) {
			resourceManager.aladdinMusic.play();
		}
		
		state = STATE.NEW;
			
	}
	
	private void addPillar() {
		RomaPillar p = RomaPillarFactory.getInstance().next();
		pillars.add(p);
		attachIfNotAttached(p);
	}
	
	private void attachIfNotAttached(RomaPillar p) {
		if(!p.hasParent()) {
			attachChild(p);
		}
	}
	
	@Override
	public void beginContact(Contact contact) {
		if(RomaPillar.BODY_WALL.equals(contact.getFixtureA().getBody().getUserData()) || RomaPillar.BODY_WALL.equals(contact.getFixtureB().getBody().getUserData())) {
			state = STATE.DEAD;
			
			if(score > activity.getHighScore()) {
				activity.setHighScore(score);
			}
			timestamp = System.currentTimeMillis();
			
			//play sound die
			resourceManager.dieSound.play();
			aladdinBody.setLinearVelocity(0, 0);
			
			for(RomaPillar p : pillars) {
				p.getPillarUpBody().setActive(false);
				p.getPillarDownBody().setActive(false);
			}
			
			yourScoreText.setText("Your Score: " + score);
			yourScoreText.setColor(Color.RED);
			yourScoreText.setVisible(true);
			
			highScoreText.setText("High Score: " + activity.getHighScore());
			highScoreText.setColor(Color.RED);
			highScoreText.setVisible(true);
			resourceManager.aladdinMusic.pause();
		}
	}

	@Override
	public void endContact(Contact contact) {
		if(RomaPillar.BODY_SENSOR.equals(contact.getFixtureA().getBody().getUserData()) || RomaPillar.BODY_SENSOR.equals(contact.getFixtureB().getBody().getUserData())) {
			resourceManager.scoreSound.play();
			scored = true;
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(pSceneTouchEvent.isActionDown()) {
			if(state == STATE.PAUSED) {
				if(lastState != STATE.NEW) {
					registerUpdateHandler(physicsWorld);
				}
				state = lastState;
				Log.v("GAMESCENE", " -> " + state);
			} else if (state == STATE.NEW) {
				reset();
				registerUpdateHandler(physicsWorld);
				state = STATE.PLAY;
				Log.v("SCENE MANAGER", "Play");
				physicsWorld.setGravity(new Vector2(0, RomaPillar.GRAVITY));
				aladdinBody.setLinearVelocity(new Vector2(RomaPillar.SPEED_X, 0));
				
				scoreText.setText("Score: 0");
				tapToPlayText.setVisible(false);
				yourScoreText.setVisible(false);
				highScoreText.setVisible(false);
			} else if (state == STATE.DEAD) {
				//don't mess with the dead buddy
			} else if (state == STATE.AFTERLIFE) {
				reset();
				state = STATE.NEW;
				Log.v("GAME SCENE", "NEW");
			} else {
				resourceManager.jumpSound.play();
				Vector2 v = aladdin.getLinearVelocity();
				v.x = RomaPillar.SPEED_X;
				v.y = RomaPillar.SPEED_Y;
				aladdin.setLinearVelocity(v);
			}
		}
		return false;
	}

	@Override
	public void onBackKeyPressed() {
		gameHUD.setVisible(false);
		resourceManager.aladdinMusic.pause();
		camera.clearUpdateHandlers();
		SceneManager.getInstance().loadMenuScene(engine);
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_GAME;
	}

	@Override
	public void disposeScene() {
		
	}

	@Override
	public void resume() {
		if(musicIsOn == true) {
			resourceManager.aladdinMusic.play();
		}
	}

	@Override
	public void pause() {
		resourceManager.aladdinMusic.pause();
	}

}
