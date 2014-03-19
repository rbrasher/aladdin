package com.comfycouch.scene;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.Entity;
import org.andengine.entity.particle.BatchedPseudoSpriteParticleSystem;
import org.andengine.entity.particle.emitter.PointParticleEmitter;
import org.andengine.entity.particle.initializer.VelocityParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ColorParticleModifier;
import org.andengine.entity.particle.modifier.ScaleParticleModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.util.adt.color.Color;

import android.opengl.GLES20;

import com.comfycouch.aladdin.GameActivity;
import com.comfycouch.manager.SceneManager.SceneType;

public class SplashScene extends BaseScene {

	private static final int CAMERA_WIDTH = 480;
	private static final int CAMERA_HEIGHT = 800;
	private static final float RATE_MIN = 400;
	private static final float RATE_MAX = 500;
	private static final int PARTICLES_MAX = 500;
	
	Sprite splash;
	private ITextureRegion mParticleTextureRegion;
	private BitmapTextureAtlas particleAtlas;
	 
	
	@Override
	public void createScene() {
		particleAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA);
		mParticleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(particleAtlas, activity, "particle_fire.png", 0, 0);
		particleAtlas.load();
		
		this.getBackground().setColor(Color.BLACK);
		
		splash = new Sprite(0, 0, resourceManager.splash_region, vbom) {
			@Override
			protected void preDraw(GLState pGLState, Camera pCamera) {
				super.preDraw(pGLState, pCamera);
				pGLState.enableDither();
			}
		};
		
		splash.setScale(1.5f);
		splash.setPosition(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
		attachChild(splash);
		
		/*
		 * Lower left to lower right Particle System
		 */
		{
			final BatchedPseudoSpriteParticleSystem particleSystem = new BatchedPseudoSpriteParticleSystem(new PointParticleEmitter(-32, 800, -32), RATE_MIN, RATE_MAX, PARTICLES_MAX, this.mParticleTextureRegion, vbom);
			particleSystem.setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);
			particleSystem.addParticleInitializer(new VelocityParticleInitializer<Entity>(200, 300, 0, -300));
			particleSystem.addParticleInitializer(new VelocityParticleInitializer<Entity>(5, -11));
			particleSystem.addParticleInitializer(new VelocityParticleInitializer<Entity>(0.0f, 360.0f));
			particleSystem.addParticleInitializer(new VelocityParticleInitializer<Entity>(1.0f, 1.0f, 0.0f));
			particleSystem.addParticleInitializer(new VelocityParticleInitializer<Entity>(6.5f));
			
			particleSystem.addParticleModifier(new ScaleParticleModifier<Entity>(0, 5, 0.5f, 2.0f));
			particleSystem.addParticleModifier(new ColorParticleModifier<Entity>(2.5f, 5.5f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f));
			particleSystem.addParticleModifier(new AlphaParticleModifier<Entity>(2.5f, 6.5f, 1.0f, 0.0f));
			
			attachChild(particleSystem);
		}
	}

	@Override
	public void onBackKeyPressed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SceneType getSceneType() {
		// TODO Auto-generated method stub
		return null;
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
