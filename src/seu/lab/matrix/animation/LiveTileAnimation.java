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

	public LiveTileAnimation(String tag, boolean loop) {
		super(tag, Math.PI);
		this.loop = loop;
	}
	
	public Object3D tile1;
	public Object3D tile2;
	
	private SimpleVector axis1 = new SimpleVector(1, -1, 0);
	private SimpleVector axis2 = new SimpleVector(1, -1, 0);
	
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
	
	public void init(){
		if(tile1 == null || tile2 == null)return;
		frames *= 1;// (int )(Math.random() * 2 + 1);
	}
	
	public void reset(){
		tile1.clearRotation();
		tile2.clearRotation();
	}
	
	@Override
	public void onAnimateSuccess() {

	}

	@Override
	void onChange(double v) {
		tile1.rotateAxis(axis1, (float) v);
		tile2.rotateAxis(axis2, (float) v);
	}
	
	@Override
	public void stop() {
		reset();
		super.stop();
	}
}
