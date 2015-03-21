package seu.lab.matrix.animation;

public abstract class BaseAnimation implements Animatable {

	protected String tag;
	protected int index = 0;
	protected int frames = 300;
	protected double factor = 0.75;
	protected boolean loop = false;
	public boolean stopped = false;
	
	public BaseAnimation(String tag){
		this.tag = tag;
	}
	
	@Override
	public boolean isOver() {
		if(stopped ) return true;
		return (!loop) && index == frames;
	}

	@Override
	public void stop() {
		stopped = true;
		index = 0;
	}

	@Override
	public void reset() {
		stopped = false;
		index = 0;
	}
	
}
