package seu.lab.matrix.animation;

import com.threed.jpct.SimpleVector;

public abstract class BaseVectorAnimation extends BaseAnimation{

	protected SimpleVector base;

	abstract void onChange(SimpleVector vector);
		
	public BaseVectorAnimation(String tag, SimpleVector base){
		super(tag);
		base.scalarMul((float) ((1 - factor) / factor));
		this.base = base;
	}

	@Override
	public void animate() {
		if (loop) {
			index = index + 1 > frames ? index = 1 : index + 1;
		} else {
			index++;
		}
		SimpleVector tmp = new SimpleVector(base);
		tmp.scalarMul((float) (Math.pow(factor, index)));
		
		onChange(tmp);
	}

}
