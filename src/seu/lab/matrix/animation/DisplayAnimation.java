package seu.lab.matrix.animation;

import android.util.Log;

import com.threed.jpct.Object3D;

public class DisplayAnimation extends BaseValueAnimation{

	private Object3D[] object3ds;
	
	private boolean toHide;
	private double varing = 0;
	
	public DisplayAnimation(Object3D[] object3ds, String tag, boolean toHide) {
		super(tag, 100);
		loop = false;
		this.object3ds = object3ds;
		this.toHide = toHide;
		frames = 20;
		if (toHide) {
			varing = 100;
		}
		
		for (Object3D o : object3ds) {
			o.setVisibility(true);

			if(toHide){
				o.setTransparency(100);
			}else {
				o.setTransparency(50);
			}
		}
	}

	@Override
	public void onAnimateSuccess() {
		
		Log.e("Display", ""+varing);
		
		for (Object3D o : object3ds) {
			if(toHide){
				o.setTransparency(0);
				o.setVisibility(false);
			}else {
				o.setTransparency(100);
				o.setVisibility(true);
			}
		}
	}

	@Override
	void onChange(double v) {
		for (Object3D o : object3ds) {
			if(toHide){
				varing -= v;
			}else {
				varing += v;
			}
			varing = varing < 0 ? 0 : varing;
			varing = varing > 100 ? 100 : varing;
			o.setTransparency((int) varing);
		}
	}

}
