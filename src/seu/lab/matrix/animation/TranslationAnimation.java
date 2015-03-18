package seu.lab.matrix.animation;

import raft.jpct.bones.Animated3D;

import android.util.Log;

import com.jbrush.ae.EditorObject;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class TranslationAnimation extends BaseVectorAnimation{

	public boolean ignoreStop = false;

	private Object3D[] object3ds;
	private SimpleVector[] targets;
	private SimpleVector translation;
	private PickGroup group;
	
	public TranslationAnimation(String tag, Object3D[] object3ds, SimpleVector translation, PickGroup group) {
		super(tag, translation);
		loop = false;
		frames = 60;
		this.object3ds = object3ds;
		this.translation = translation;
		this.group = group;
		targets = new SimpleVector[object3ds.length];
		for (int i = 0; i < object3ds.length; i++) {
			targets[i] = object3ds[i].getTransformedCenter().calcAdd(
					translation);
		}
	}

	@Override
	void onChange(SimpleVector vector) {
		for (Object3D o : object3ds) {
			o.translate(vector);
		}
	}

	@Override
	public void onAnimateSuccess() {
		Log.e("Animatable", "onAnimateSuccess for "+object3ds[0].getTranslation());
		
//		for (int i = 0; i < object3ds.length; i++) {
//			object3ds[i].translate(targets[i].calcSub(object3ds[i]
//					.getTransformedCenter()));
//		}
		
		if (group != null) {
			group.animation = null;
		}
	}

	@Override
	public void stop() {
		super.stop();
		if (ignoreStop) {
			onAnimateSuccess();
		} else {
			if (group != null) {
				group.animation = null;
			}
		}
	}

}
