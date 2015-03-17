package seu.lab.matrix.animation;

import java.util.List;

public class SeqAnimation implements Animatable{

	int frames;
	int index = -1;

	final static int WAIT_FRAMES = 5;
	List<Animatable> mAnimatables;
	Animatable[] queue;
	
	public SeqAnimation(List<Animatable> list, Animatable[] queue){
		this.queue = queue;
		mAnimatables = list;
		
		frames = queue.length * WAIT_FRAMES - 1;
	}
	
	@Override
	public boolean isOver() {
		return frames == index;
	}

	@Override
	public void animate() {
		index++;
		if(index % WAIT_FRAMES == 0){
			mAnimatables.add(queue[index / WAIT_FRAMES]);
		}
	}

	@Override
	public void onAnimateSuccess() {

	}

	@Override
	public void stop() {

	}

}
