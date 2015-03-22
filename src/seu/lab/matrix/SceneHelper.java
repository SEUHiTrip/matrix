package seu.lab.matrix;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;

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
}
