package seu.lab.matrix;

import java.util.LinkedList;
import java.util.List;

import android.app.ActionBar.Tab;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.FloatMath;
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

public class SceneHelper {

    public static void getEulerAngles(float[] mHeadView, float[] eulerAngles, int offset) {
        if (offset + 3 > eulerAngles.length) {
            throw new IllegalArgumentException("Not enough space to write the result");
        }
        float yaw;
        float roll;
        float pitch = (float) Math.asin((double) mHeadView[6]);
        if (FloatMath.sqrt(1.0f - (mHeadView[6] * mHeadView[6])) >= 0.01f) {
            yaw = (float) Math.atan2((double) (-mHeadView[2]), (double) mHeadView[10]);
            roll = (float) Math.atan2((double) (-mHeadView[4]), (double) mHeadView[5]);
        } else {
            yaw = 0.0f;
            roll = (float) Math.atan2((double) mHeadView[1], (double) mHeadView[0]);
        }
        eulerAngles[offset + 0] = -pitch;
        eulerAngles[offset + 1] = -yaw;
        eulerAngles[offset + 2] = -roll;
    }
	
	public static double isLookingAt(Camera cam, SimpleVector center) {
		SimpleVector camDir = new SimpleVector();
		cam.getDirection(camDir);
		SimpleVector camPos = cam.getPosition();

		double sum = Math.pow(center.x - camPos.x, 2d)
				+ Math.pow(center.y - camPos.y, 2d)
				+ Math.pow(center.z - camPos.z, 2d);
		sum = Math.sqrt(sum);

		double dot = (camDir.x * (center.x - camPos.x) + camDir.y
				* (center.y - camPos.y) + camDir.z * (center.z - camPos.z))
				/ sum;
		return dot;
	}

	public static double isLookingAt(Camera cam, Object3D hand,
			SimpleVector center) {
		SimpleVector handDir = hand.getTransformedCenter().calcSub(
				cam.getPosition());
		SimpleVector camPos = center.calcSub(cam.getPosition());

		handDir = handDir.normalize();
		camPos = camPos.normalize();

		double dot = handDir.calcDot(camPos);

		return dot;
	}
	
	public static double isLookingDir(Camera cam, Object3D hand,
			SimpleVector dir) {
		SimpleVector handDir = hand.getTransformedCenter().calcSub(
				cam.getPosition());
		SimpleVector camPos = new SimpleVector(dir);

		handDir = handDir.normalize();
		camPos = camPos.normalize();

		double dot = handDir.calcDot(camPos);

		return dot;
	}

	public static Bitmap RotateBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(),
				source.getHeight(), matrix, true);
	}
	
	public static Object3D[] to1DArr(Object3D[][] ori) {
		List<Object3D> list = new LinkedList<Object3D>();
		for (int i = 0; i < ori.length; i++) {
			for (int j = 0; j < ori[i].length; j++) {
				list.add(ori[i][j]);
			}
		}
		Object3D[] arr = new Object3D[list.size()];
		arr = list.toArray(arr);
		return arr;
	}
	
	public static void printTexture(String tname, Object3D object3d) {
		TextureManager tm = TextureManager.getInstance();

		if (tm.containsTexture(tname)) {
			object3d.setTexture(tname);
		} else {
			object3d.setTexture("dummy");
		}

//		object3d.calcTextureWrapSpherical();
//		object3d.strip();
//		object3d.build();
	}
	
	public static void drawText(String tex, String[] msg) {
		Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		canvas.drawColor(Color.TRANSPARENT);
		Paint p = new Paint();

		String fontType = "consolas";
		Typeface typeface = Typeface.create(fontType, Typeface.NORMAL);

		p.setAntiAlias(true);

		//p.setColor(Color.WHITE);
		p.setTypeface(typeface);
		p.setTextSize(20);
		for (int i = 0; i < msg.length; i++) {
			if(i==0){
				p.setColor(Color.rgb(255, 125, 125));
				canvas.drawText(msg[i], 0, 30 * i+30, p);
			}
			else{
				if(msg[i].contains(":")){
					p.setColor(Color.WHITE);
					String[] temp=msg[i].split(":");
					canvas.drawText(temp[0]+":", 0, 30 * i+30, p);
					p.setColor(Color.rgb(80, 144, 255));
					canvas.drawText(temp[1], 100, 30 * i+30, p);
				}
				else {
					p.setColor(Color.rgb(255, 125, 125));
					canvas.drawText(msg[i], 0, 30 * i+30, p);
				}
			}
//			else if(i%2==1)
//				p.setColor(Color.rgb(80, 144, 255));
//			else 
//				p.setColor(Color.WHITE);
			
		}

		TextureManager tm = TextureManager.getInstance();
		Texture texture = new Texture(bitmap);
		if (tm.containsTexture(tex)) {
			tm.replaceTexture(tex, texture);
		} else {
			tm.addTexture(tex, texture);
		}
		bitmap.recycle();
	}
}
