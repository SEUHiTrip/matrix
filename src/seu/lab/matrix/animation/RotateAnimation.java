package seu.lab.matrix.animation;

public class RotateAnimation extends BaseValueAnimation{
	
	PickGroup group;

	
	public RotateAnimation(String tag, double base, PickGroup group) {
		super(tag, base);
		this.group = group;
		this.frames = 240;
		for (int i = 0; i < group.group.length; i++) {
			group.group[i].clearRotation();
			group.group[i].rotateZ(-0.4f);
		}
	}

	@Override
	public void onAnimateSuccess() {
		if(group != null)
			group.animations = null;
	}

	@Override
	void onChange(double v) {
		for (int i = 0; i < group.group.length; i++) {
			group.group[i].rotateZ((float) v);
		}
	}

}
