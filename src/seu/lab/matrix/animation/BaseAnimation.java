package seu.lab.matrix.animation;

public abstract class BaseAnimation implements Animatable {

	protected String tag;
	protected int index = 0;
	protected int frames = 300;
	protected double factor = 0.75;
	protected boolean loop = false;
	
	public BaseAnimation(String tag){
		this.tag = tag;
	}
	
	@Override
	public boolean isOver() {
		return (!loop) && index == frames;
	}

	@Override
	public void stop() {
		index = 0;
	}

}
