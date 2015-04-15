package seu.lab.matrix.animation;

import java.util.Random;

import android.app.ActionBar.Tab;
import android.nfc.Tag;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class LiveTileAnimation extends BaseValueAnimation{

	public LiveTileAnimation(String tag) {
		super(tag, Math.PI);
		loop = true;
	}

	public LiveTileAnimation(String tag, boolean loop, PickGroup group) {
		super(tag, Math.PI);
		this.loop = loop;
		this.group = group;
	}
	
	public Object3D tile1;
	public Object3D tile2;
	
	private SimpleVector axis1 = new SimpleVector(1, -1, 0);
	private SimpleVector axis2 = new SimpleVector(1, -1, 0);
	
	PickGroup group;
	
	public void setTile1(Object3D tile, SimpleVector vector){
		tile1 = tile;
		axis1 = vector;
		init();
	}
	
	public void setTile2(Object3D tile, SimpleVector vector){
		tile2 = tile;
		axis2 = vector;
		init();
	}
	
	public void setFrames(int frames){
		this.frames = frames > 30 && frames < 600 ? frames : 300; 
	}
	
	public void init(){
		if(tile1 == null || tile2 == null)return;
		frames *= (int )(Math.random() * 2 + 1);
	}
	
	public void reset(){
//		tile1.clearRotation();
//		tile2.clearRotation();
		stopped = false;
		index = 0;
	}
	
	@Override
	public void onAnimateSuccess() {
		if(group != null)
			group.animations = null;
	}

	@Override
	void onChange(double v) {
		tile1.rotateAxis(axis1, (float) v);
		tile2.rotateAxis(axis2, (float) v);
	}

}
