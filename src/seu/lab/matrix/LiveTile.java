package seu.lab.matrix;

import java.util.Random;

import android.app.ActionBar.Tab;
import android.nfc.Tag;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class LiveTile implements Animatable{

	private Object3D tile1;
	private Object3D tile2;
	
	private int index = 0;
	private int duration = 300;
	private static final double factor = 0.75;
	private double base;

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
		duration *= (int )(Math.random() * 3 + 1);
		base = 3.1415926 * (1-factor) / factor;
	}
	
	public void reset(){
		tile1.clearRotation();
		tile2.clearRotation();
	}
	
	public void animate() {
		index = index + 1 > duration ? index = 1 : index + 1;
		
		tile1.rotateAxis(axis1, (float)(Math.pow(factor, index) * base));
		tile2.rotateAxis(axis2, (float)(Math.pow(factor, index) * base));
	}

	public boolean isOver() {
		return false;
	}
}
