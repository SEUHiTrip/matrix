package seu.lab.matrix.animation;

import com.threed.jpct.Object3D;

public class PickGroup {

	public int state;
	
	public Object3D[] group;
	
	public Animatable animation;
	
	public PickGroup(int count){
		group = new Object3D[count];
	}

}
