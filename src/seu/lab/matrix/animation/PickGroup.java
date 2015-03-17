package seu.lab.matrix.animation;

import com.threed.jpct.Object3D;

public class PickGroup {

	public int state;
	
	public Object3D[] group;
	
	public Animatable animation;
	
	public PickGroup(boolean single){
		if (single) {
			group = new Object3D[1];
		}else {
			group = new Object3D[2];
		}
	}
}