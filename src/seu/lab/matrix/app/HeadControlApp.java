package seu.lab.matrix.app;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.Confg;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;
import android.util.Log;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class HeadControlApp extends SimpleScreenApp{

	private CardboardView cardboardView;
	private HeadTransform head;
	private Eye leftEye;
	private Eye rightEye;
	private Eye monocular;
	private float[] sAngles;
	private int ready = 0;
	private float[] values = new float[3];
	private OutputStream outStream;
	private Socket socket;
	private int PORT;
	private final String IP = Confg.IP;
	boolean stopped = false;

	Thread curThread;
	
	Runnable getHeadTransform = new Runnable() {
		@Override
		public void run() {
			head = new HeadTransform();
	        monocular = new Eye(0);
	        leftEye = new Eye(1);
	        rightEye = new Eye(2);
			sAngles = new float[3];
			Log.d("Connection", "Connection ready to connect");
			
			try {
				socket = new Socket(IP, PORT);
				outStream = socket.getOutputStream();
				Log.d("Connection", "Connection ok");
			} catch (Exception e) {
				stopped = true;
				e.printStackTrace();
			}
			
			while(!stopped) {
				cardboardView.getCurrentEyeParams(head, leftEye, rightEye, monocular);
				head.getEulerAngles(sAngles, 0);
				values[0] = (float) Math.toDegrees(sAngles[0]); // Y
				values[1] = (float) Math.toDegrees(sAngles[1]); // X
				values[2] = (float) Math.toDegrees(sAngles[2]);

				if (outStream != null) {
					byte[] buf = ByteBuffer.allocate(12).putFloat(values[0])
							.putFloat(values[1]).putFloat(values[2]).array();
					try {
						outStream.write(buf, 0, 12);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					stopped = true;
					e.printStackTrace();
				}
			}
			
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
	
	public HeadControlApp(CardboardView cardboardView, int port, app_name aName, List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(aName, animatables, callback, camera, ball1);
		this.PORT = port;
		this.cardboardView = cardboardView;
		
		defaultListener = new DefaultListener(){
			@Override
			protected void onErr() {
				// TODO Auto-generated method stub
				super.onErr();
			}
			@Override
			protected void onOk() {
				stopped = false;
				(curThread = new Thread(getHeadTransform){
					@Override
					public void run() {
						try {
							sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						super.run();
						try {
							sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						superClose();
					}
				}).start();
				super.onOk();
			}
		};
	}
	
	@Override
	public void onOpen(Bundle bundle) {
		if(DEBUG){
			stopped = false;
			(curThread = new Thread(getHeadTransform){
				@Override
				public void run() {
					try {
						sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					super.run();
				}
			}).start();
		}
		super.onOpen(bundle);
	}
	
	@Override
	public void onClose(Runnable runnable) {
		stopped = true;
		if (curThread != null && curThread.isAlive()) {
			curThread.interrupt();
		}else {
			super.onClose(runnable);
		}
	}
	
	void superClose(){
		super.onClose(null);
	}

}
