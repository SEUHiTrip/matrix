package seu.lab.matrix.animation;

import com.threed.jpct.SimpleVector;

public abstract class BaseVectorAnimation extends BaseAnimation{

	double x,y,z;
	
	abstract void onChange(SimpleVector vector);
		
	public BaseVectorAnimation(String tag, SimpleVector base){
		super(tag);
		x = base.x * (1 - factor) / factor;
		y = base.y * (1 - factor) / factor;
		z = base.z * (1 - factor) / factor;
	}

	@Override
	public void animate() {
		if (loop) {
			index = index + 1 > frames ? index = 1 : index + 1;
		} else {
			index++;
		}
						
		double d = Math.pow(factor, index);
		
		onChange(new SimpleVector(x*d, y*d, z*d));
	}

}
