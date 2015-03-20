package seu.lab.matrix.animation;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

public class CamAnimation implements Animatable {

	SimpleVector up = new SimpleVector(0, -1, 0);
	
	boolean stopped = false;
	int r = 10;
	int height = 3;
	SimpleVector origin;
	SimpleVector lookat;
	double speed = 0.005f;
	double angle;
	double end;
	double x, y;

	Camera camera;

	public CamAnimation(Camera c, SimpleVector o, SimpleVector l, double s, double e) {
		camera = c;
		origin = o;
		lookat = l;
		angle = s;
		end = e;
	}

	@Override
	public boolean isOver() {
		return stopped || angle > 3;
	}

	@Override
	public void animate() {
		angle += speed;
		x = Math.sin(angle) * r;
		y = Math.cos(angle) * r;
		camera.setPosition((float) (origin.x + x), (float) (origin.y + y),
				(float) (origin.z + angle*0.25));
		camera.lookAt(lookat);
		camera.rotateZ(3.1415926f / 2);
	}

	@Override
	public void onAnimateSuccess() {
		camera.setPosition(origin);
	}

	@Override
	public void stop() {
		stopped = true;
	}
}
