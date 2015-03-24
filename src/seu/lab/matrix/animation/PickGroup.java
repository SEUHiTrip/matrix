package seu.lab.matrix.animation;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

public class PickGroup {

	public int state;
	
	public Object3D[] group;
	
	public SimpleVector[] oriPos;
	
	public Animatable animation;
	
	public PickGroup(int count){
		group = new Object3D[count];
		oriPos = new SimpleVector[count];
	}

}
