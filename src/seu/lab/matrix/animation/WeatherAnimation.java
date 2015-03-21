package seu.lab.matrix.animation;

import java.util.List;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class WeatherAnimation implements Animatable {

	boolean stopped = false;
	Animatable[] queue1 = new Animatable[3];
	Animatable[] queue2 = new Animatable[3];

	int frames = 480;
	int index = 0;
	int distance = 2;
	
	List<Animatable> animatables;

	public WeatherAnimation(Object3D temp, Object3D location, List<Animatable> animatables) {
		this.animatables = animatables;
		location.translate(0, 0, -distance);
		Object3D[] t = new Object3D[]{temp};
		Object3D[] l = new Object3D[]{location};
		Object3D[] group = new Object3D[]{temp,location};
		queue1[0] = new TranslationAnimation("", group, new SimpleVector(0, 0, distance), null);
		queue1[1] = new DisplayAnimation(t, "", true);
		queue1[2] = new DisplayAnimation(l, "", false);
		queue2[0] = new TranslationAnimation("", group, new SimpleVector(0, 0, -distance), null);
		queue2[1] = new DisplayAnimation(l, "", true);
		queue2[2] = new DisplayAnimation(t, "", false);
	}

	@Override
	public boolean isOver() {
		return stopped;
	}

	@Override
	public void animate() {
		index++;
		if (index > frames)
			index = 0;

		if (index == 1) {
			for (int i = 0; i < queue1.length; i++) {
				queue1[i].reset();
				animatables.add(queue1[i]);
			}
		} else if (index == 241) {
			for (int i = 0; i < queue2.length; i++) {
				queue2[i].reset();
				animatables.add(queue2[i]);
			}
		}
	}

	@Override
	public void onAnimateSuccess() {

	}

	@Override
	public void stop() {
		stopped = true;
	}

	@Override
	public void reset() {

	}
}
