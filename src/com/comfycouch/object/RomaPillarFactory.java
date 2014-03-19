package com.comfycouch.object;

import java.util.Random;

import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.util.adt.pool.GenericPool;

import com.comfycouch.manager.ResourceManager;

public class RomaPillarFactory {

	private static final RomaPillarFactory INSTANCE = new RomaPillarFactory();
	
	GenericPool<RomaPillar> pool;
	int nextX, nextY, dy;
	int dx = 400;
	
	final int maxY = 550;
	final int minY = 350;
	
	private Random randomVal;
	
	private RomaPillarFactory() {
		
	}
	
	public static final RomaPillarFactory getInstance() {
		return INSTANCE;
	}
	
	public void create(final PhysicsWorld pPhysicsWorld) {
		reset();
		pool = new GenericPool<RomaPillar>(4) {

			@Override
			protected RomaPillar onAllocatePoolItem() {
				RomaPillar p = new RomaPillar(0, 0, ResourceManager.getInstance().pillarRegion, ResourceManager.getInstance().pillarRegionUp, ResourceManager.getInstance().vbom, pPhysicsWorld);
				return p;
			}
			
		};
	}
	
	public RomaPillar next() {
		RomaPillar p = pool.obtainPoolItem();
		p.setPosition(nextX, nextY);
		p.getScoreSensor().setTransform(nextX / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, nextY / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		
		p.getPillarUpBody().setTransform(nextX / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, (nextY + p.getPillarShift()) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		p.getPillarDownBody().setTransform(nextX / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, (nextY - p.getPillarShift()) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		
		p.getScoreSensor().setActive(true);
		p.getPillarUpBody().setActive(true);
		p.getPillarDownBody().setActive(true);
		
		randomVal = new Random();
		
		dx = randomVal.nextInt(465 - 345) + 345;
		nextX += dx;
		
		dy = randomVal.nextInt(440 - 280) + 280;
		if(dy > maxY || dy < minY) {
			dy = maxY;
		}
		nextY = dy;
		
		return p;
	}
	
	public void recycle(RomaPillar pPillar) {
		pPillar.detachSelf();
		pPillar.getScoreSensor().setActive(false);
		pPillar.getPillarUpBody().setActive(false);
		pPillar.getPillarDownBody().setActive(false);
		pPillar.getScoreSensor().setTransform(-1000, -1000, 0);
		pPillar.getPillarUpBody().setTransform(-1000, -1000, 0);
		pPillar.getPillarDownBody().setTransform(-1000, -1000, 0);
		pool.recyclePoolItem(pPillar);
	}
	
	public void reset() {
		nextX = 1500;
		nextY = 350;
		dy = 50;
	}
}
