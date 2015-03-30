package seu.lab.matrix;

import seu.lab.matrix.app.AbstractApp;
import seu.lab.matrix.app.AppType;

import com.threed.jpct.Object3D;

public class Workspace {
	
	public int id;
	
	public Workspace(int id, AbstractApp app) {
		this.id = id;
		this.mCurrentApp = app;
	}
	
	public boolean isScrShown = false;
	public boolean isWaiting = false;
	public boolean isPeopleShown = false;
	
	public Object3D animal = null;
	
	public AppType mCurrentAppType = AppType.NULL;
	
	public AbstractApp mCurrentApp;
	
	public int mState = 0;	
}
