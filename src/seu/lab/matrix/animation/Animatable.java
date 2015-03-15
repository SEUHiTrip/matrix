package seu.lab.matrix.animation;

public interface Animatable {
	boolean isOver();
	void animate();
	void onAnimateSuccess();
	void stop();
}
