package seu.lab.matrix;

import java.io.IOException;
import java.util.Vector;

import android.R.integer;
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
import com.threed.jpct.Light;
import com.threed.jpct.Loader;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

public class SceneActivity extends Framework3DMatrixActivity {
	
    private static final String TAG = "SceneActivity";
    
	protected SimpleVector forward = new SimpleVector(-1, 0, 0);
	protected SimpleVector up = new SimpleVector(0, -1, 0);

	protected Object3D[] mCamViewspots = null;
	protected int mCamViewIndex = 0;

	protected Object3D[] mObjects = null;

	private int frameCounter = 0;

	private Camera cam;
	
	@Override
	public void onSurfaceChanged(int w, int h) {
		// TODO Auto-generated method stub
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(w, h);
		world=new World();
		
		world.getCamera().setPosition(0, 0, 0);
		
		try {
			// mObjects = Loader.load3DS(getAssets().open("tex.3ds"), 1);
			
			mObjects = Loader.loadOBJ(getAssets().open("workspace.obj"), getAssets().open("workspace.mtl"), 1);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Object3D plane = Primitives.getPlane(1, 1);

		world.addObjects(mObjects);
		world.setAmbientLight(120, 120, 120);

		sun = new Light(world);
		sun.setIntensity(250, 250, 250);
		
		spot = new Light(world);
		spot.setIntensity(150, 150, 150);
		
//		notice = Primitives.getPlane(1, 2);
//		notice.rotateY(4.71f);
//		notice.translate(-5, 0, 0);
//		notice.setTexture("dummy");
//		notice.build();
//		notice.strip();
//		world.addObject(notice);
		
		MemoryHelper.compact();
		
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
		
		if(frameCounter == 0){
			frameCounter++;

			cam = world.getCamera();

		}else if(frameCounter == 1) {
			frameCounter++;

			mCamViewspots = new Object3D[5];
			
			String name = null;
			for (int i = 0; i < mObjects.length; i++) {
				name = mObjects[i].getName();
				Log.e(TAG, "name: " + name);
				saveObject(name, mObjects[i]);
			}			
		}
		
		headTransform.getEulerAngles(mAngles, 0);

//		if(mCamViewspots != null){
//			cam.setOrientation(forward, up);
//		}else {
//			cam.setOrientation(forward, up);
//		}
		cam.setOrientation(forward, up);
		
		if (canCamRotate) {
			cam.rotateZ(3.1415926f/2);
			
			cam.rotateY(mAngles[1]);
			cam.rotateZ(-mAngles[2]);
			cam.rotateX(mAngles[0]);
			
		}
	}
	
	private void saveObject(String name, Object3D object3d) {
		
		if (name.startsWith("c_green")) {
			object3d.setVisibility(false);
			mCamViewspots[1] = object3d;
			return;
		}else if(name.startsWith("c_ship")){
			object3d.setVisibility(false);
			mCamViewspots[3] = object3d;
			return;
		}else if(name.startsWith("c_volcano")) {
			object3d.setVisibility(false);
			mCamViewspots[2] = object3d;
			return;
		}else if(name.startsWith("c_treasure")) {
			object3d.setVisibility(false);
			mCamViewspots[0] = object3d;
			return;
		}else if(name.startsWith("x_c_works")) {
			object3d.setVisibility(false);
			mCamViewspots[4] = object3d;
			return;
		}else if(name.startsWith("weather")) {
			SimpleVector sv = new SimpleVector(object3d.getTransformedCenter());
			sv.y -= 10;
			sun.setPosition(sv);
			return;
		}else if(name.startsWith("x_b")) {
			
			Log.e(TAG, "x_b");
			
			object3d.clearAdditionalColor();
			object3d.setTexture("dummy");
			object3d.calcTextureWrapSpherical();
			object3d.strip();
			object3d.build();
			
			return;
		}else if(name.startsWith("x_scr")) {
			
			Log.e(TAG, "x_scr");
			
			object3d.clearAdditionalColor();
			object3d.setShader(screenShaders[name.charAt(5)-'1']);
			object3d.calcTextureWrapSpherical();
			object3d.build();
			object3d.strip();
			
			return;
		}
	}

	@Override
	public void onDrawEye(Eye eye) {
		fillTexturesWithEye(buffer, eye);

		screenShaders[0].setUniform("videoFrame", 4);
		screenShaders[0].setUniform("videoFrame2", 5);
		screenShaders[0].setUniform("videoFrame3", 6);

		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (!(event.getAction() == MotionEvent.ACTION_UP))
			return false;
		
		mCamViewIndex = mCamViewIndex + 1 > 4 ? 0 : mCamViewIndex + 1;
		
		if(mCamViewspots == null) return false;
		
		if(mCamViewspots[mCamViewIndex] != null){
			cam.setPosition(mCamViewspots[mCamViewIndex].getTransformedCenter());
			spot.setPosition(mCamViewspots[mCamViewIndex].getTransformedCenter());
		}

		return false;
	}
}
