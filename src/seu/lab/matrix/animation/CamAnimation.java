package seu.lab.matrix.animation;

import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

public class CamAnimation implements Animatable {

	SimpleVector up = new SimpleVector(0, -1, 0);
	
	boolean stopped = false;
	boolean clockwise = true;

	int r = 10;
	int height = 3;
	protected SimpleVector ori;
	protected SimpleVector lookat;
	double speed = 0.005f;
	double angle;
	double end;
	double x, y;

	protected Camera camera;

	public CamAnimation(Camera c, SimpleVector o, SimpleVector l, double s, double e, int r, int h) {
		camera = c;
		ori = o;
		lookat = l;
		angle = s;
		end = e;
		this.r = r;
		height = h;
		
	}
	
	public CamAnimation(Camera c, SimpleVector o, SimpleVector l, double s, double e, int r, int h, boolean clockwise) {
		camera = c;
		ori = o;
		lookat = l;
		angle = s;
		end = e;
		this.r = r;
		height = h;
		this.clockwise = clockwise;
		if(!clockwise)speed = -speed;
	}

	@Override
	public boolean isOver() {
		if(clockwise)
			return stopped || angle > end;
		else {
			Log.e("isOver", angle+" "+end);
			return stopped || angle < end;
		}
	}

	@Override
	public void animate() {
		angle += speed;
		x = Math.sin(angle) * r;
		y = Math.cos(angle) * r;
		camera.setPosition((float) (ori.x + x), (float) (ori.y + y),
				(float) (ori.z + height*angle*0.5));
		camera.lookAt(lookat);
		camera.rotateZ(3.1415926f / 2);
	}

	@Override
	public void onAnimateSuccess() {
		camera.setPosition(ori);
	}

	@Override
	public void stop() {
		stopped = true;
	}
}
