package seu.lab.matrix.app;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.json.JSONException;
import org.opencv.core.Point;

import seu.lab.matrix.Framework3DMatrixActivity;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.Confg;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;
import android.util.Log;

import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class MouseControlApp extends SimpleScreenApp {

	static OutputStream outStream;
	static Socket socket;
	static int PORT = 6000;
	static boolean stopped = false;
	static boolean canPlay = false;
	static float[] values = new float[3];
	static Point dummyPoint = new Point();
	static LinkedBlockingDeque<Point> queue = new LinkedBlockingDeque<Point>();
	
	static final boolean mouseDebug = true;
	
	static Runnable mouseControlRunnable = new Runnable() {
		@Override
		public void run() {

			Log.d("Connection", "Connection ready to connect");
			try {
				socket = new Socket(Confg.IP, PORT);
				outStream = socket.getOutputStream();
				Log.d("Connection", "Connection ok");
			} catch (Exception e) {
				stopped = true;
				e.printStackTrace();
			}
			Point point = new Point();
			queue.clear();

			while(!stopped) {

				try {
					point = queue.take();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if (outStream != null) {
					byte[] buf = ByteBuffer.allocate(12).putFloat(sc.getScreenIdx())
							.putFloat((float) point.x).putFloat(-(float) point.y).array();
					try {
						outStream.write(buf, 0, 12);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			queue.clear();
			
			if (outStream != null) {
				try {
					outStream.close();
					socket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.d("Connection", "Connection closed");
			
		}
		
	};
	
	static Thread controlThread = null;
	static Integer counter = 0;
	static SceneCallback sc = null;

	public MouseControlApp(app_name aName, List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(aName, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onOpen(Bundle bundle) {
		Log.e(TAG, "onOpen");
		sc = scene;
		if(mouseDebug || Framework3DMatrixActivity.isDisplayConnected()){

			synchronized (counter) {
				counter++;
			}
			
			Log.e(TAG, "onOpen "+counter);
			
			if(counter == 1){
				
				if(controlThread != null && controlThread.isAlive()){
					controlThread.interrupt();
				}
				
				stopped = false;
				controlThread = new Thread(mouseControlRunnable);
				controlThread.start();
			}
		}

		super.onOpen(bundle);
	}
	
	@Override
	public void onClose(Runnable runnable) {
		if(mouseDebug || Framework3DMatrixActivity.isDisplayConnected()){
			synchronized (counter) {
				counter--;
			}
			
			Log.e(TAG, "onClose "+counter);
			
			if(counter == 0){
				stopped = true;
				queue.add(dummyPoint);
			}
		}

		super.onClose(runnable);
	}
	
	
	@Override
	public void onMove(Point p) {
		queue.add(p);
	}

	@Override
	public void onClick() {
		if(mouseDebug || Framework3DMatrixActivity.isDisplayConnected()){
			try {
				
				if(!controlThread.isAlive()){
					stopped = false;
					controlThread = new Thread(mouseControlRunnable);
					controlThread.start();
				}
				
				windowController.click();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onPress(Point p) {
		if(mouseDebug || Framework3DMatrixActivity.isDisplayConnected()){
			try {
				windowController.press();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}

	@Override
	public void onRaise(Point p) {
		if(mouseDebug || Framework3DMatrixActivity.isDisplayConnected()){
			try {
				windowController.release();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
	
}
