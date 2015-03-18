package seu.lab.matrix.animation;

import com.threed.jpct.Object3D;

public class ScaleAnimation extends BaseValueAnimation{

	private Object3D[] object3ds;

	private float scale;
	private float varing;
	
	public ScaleAnimation(Object3D[] object3ds, String tag, float scale) {
		super(tag, scale-object3ds[0].getScale());
		loop = false;
		this.object3ds = object3ds;
		this.scale = scale;
		varing = object3ds[0].getScale();
		frames = 60;
	}

	@Override
	public void onAnimateSuccess() {
		for (Object3D o : object3ds) {
			o.setScale(scale);
		}
	}

	@Override
	void onChange(double v) {
		varing += v;
		for (Object3D o : object3ds) {
			o.setScale(varing);
		}
	}

}
