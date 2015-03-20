package seu.lab.matrix.animation;

import com.threed.jpct.Camera;
import com.threed.jpct.SimpleVector;

public class CamTranslationAnimation extends BaseVectorAnimation{

	public Camera camera;
	public SimpleVector ori = new SimpleVector();
	public SimpleVector position = new SimpleVector();
	public SimpleVector up = new SimpleVector(0, -1, 0);
	public SimpleVector lookat;
	public SimpleVector base;
	float rotate;

	public CamTranslationAnimation(String tag, SimpleVector base, Camera camera, SimpleVector l, float r) {
		super(tag, base);
		this.base = base;
		this.camera = camera;
		lookat = l;
		frames = 40;
		rotate = r;
		camera.getPosition(position);
		camera.getPosition(ori);
	}

	@Override
	public void onAnimateSuccess() {
		camera.setPosition(ori.calcAdd(base));
		camera.lookAt(lookat);
		camera.rotateZ(rotate);
	}

	@Override
	void onChange(SimpleVector vector) {
		position.add(vector);
		camera.setPosition(position);
		camera.lookAt(lookat);
		camera.rotateZ(rotate);
	}

}
