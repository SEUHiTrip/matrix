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
import android.util.Log;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

public class SceneHelper {

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

		p.setColor(Color.WHITE);
		p.setTypeface(typeface);
		p.setTextSize(28);
		for (int i = 0; i < msg.length; i++) {
			canvas.drawText(msg[i], 0, 100 * i, p);
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
