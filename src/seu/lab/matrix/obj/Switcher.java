package seu.lab.matrix.obj;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.World;

public abstract class Switcher {

	public Object3D object3d;
	public int status;
	String texture;

	public Switcher(World world, String texture){
		this.texture = texture;

		object3d = Primitives.getPlane(1, 0.25f);
		object3d.translate(-2, 0, 3f);
		object3d.calcTextureWrapSpherical();
		object3d.strip();
		object3d.build();
		object3d.rotateY((float) (3*Math.PI/2+Math.PI/4));
		object3d.setVisibility(false);
		world.addObject(object3d);
		
		updateStatus();
	}
	
	public void toggleStatus(){
		status = status == 0 ? 1 : 0;
		if(status == 0){
			close();
		}else {
			open();
		}
		setTexture();
	}
	
	public void setTexture() {
		if(status == 0){
			this.object3d.setTexture(texture+"_off");
		}else {
			this.object3d.setTexture(texture+"_on");
		}
	}
	
	public abstract void open();
	public abstract void close();

	public void updateStatus(){
		setTexture();
	}

}
