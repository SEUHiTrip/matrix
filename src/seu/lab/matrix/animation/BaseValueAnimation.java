package seu.lab.matrix.animation;

public abstract class BaseValueAnimation extends BaseAnimation{
	
	protected double base;
	
	abstract void onChange(double v);

	public BaseValueAnimation(String tag, double base){
		super(tag);
		this.base = base * (1-factor) / factor;
	}
	
	@Override
	public void animate() {
		if (loop) {
			index = index + 1 > frames ? index = 1 : index + 1;
		} else {
			index++;
		}
		double tmp = Math.pow(factor, index) * base;
		onChange(tmp);
	}
}
