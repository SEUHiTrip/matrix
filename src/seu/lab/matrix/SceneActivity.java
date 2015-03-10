package seu.lab.matrix;

import java.io.IOException;
import java.util.Vector;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.idisplay.VirtualScreenDisplay.IDisplayConnection.IDisplayConnectionCallback;
import com.idisplay.util.ServerItem;
import com.jbrush.ae.*;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.GLSLShader;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

public class SceneActivity extends Framework3DMatrixActivity {
	
    private static final String TAG = "SceneActivity";
    
	protected SimpleVector forward = new SimpleVector(0, -1, 0);

	@Override
	public void onSurfaceChanged(int w, int h) {
		// TODO Auto-generated method stub
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(w, h);
		world=new World();
		
		world.getCamera().setPosition(0, -8, 0.2f);
		
		Object3D[] object3ds = null;
		try {
			object3ds = Loader.load3DS(getAssets().open("mat.3ds"), 1);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < object3ds.length; i++) {
			Log.e(TAG, "name: "+ object3ds[i].getName()+ " id: "+object3ds[i].getID());
		}
		
		world.addObjects(object3ds);
		
		if (buffer == null) {
			// GLES20.glDeleteTextures(buffer.length, buffer, 0);

			int numOfTextures = 3 + 4 + 3 + 3;
			buffer = new int[numOfTextures];
			GLES20.glGenTextures(numOfTextures, buffer, 0);
			for (int i = 0; i < numOfTextures; i++) {
				int i2 = buffer[i];
				Log.d("Texture", "Texture is at " + i2);
				GLES20.glBindTexture(3553, i2);
				GLES20.glTexParameteri(3553, 10242, 33071);
				GLES20.glTexParameteri(3553, 10243, 33071);
				GLES20.glTexParameteri(3553, 10240, 9729);
				GLES20.glTexParameteri(3553, 10241, 9729);
			}
		}
	}
	
	@Override
	public void onNewFrame(HeadTransform headTransform) {
		// TODO Auto-generated method stub
		headTransform.getEulerAngles(mAngles, 0);

		Camera cam = world.getCamera();
		cam.lookAt(forward);
		if (canCamRotate) {
			cam.rotateY(- mAngles[1]);
			cam.rotateZ(- mAngles[2]+3.1415926f);
			cam.rotateX(mAngles[0]);
		}
	}
	
	@Override
	public void onDrawEye(Eye eye) {

		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
}