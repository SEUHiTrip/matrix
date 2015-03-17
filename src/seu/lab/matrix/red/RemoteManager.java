package seu.lab.matrix.red;

import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Point;
import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("Assert")
public class RemoteManager {

	public interface OnRemoteChangeListener {
		public void onMove(Point p);

		public void onClick();

		public void onPress();

		public void onRaise();
	}

	private static OnRemoteChangeListener mListener;
	private boolean onPress = false;
	private boolean inPress = false;
	private long startMillis;
	private long durMillis;

	public void registerListener(OnRemoteChangeListener listener) {
		mListener = listener;
	}

	protected void processData(List<Point> array) {

		 for (int i=0; i<array.size(); i++) {
		 Log.i("Point", "" + array.get(i));
		 }

		// 去除附近干扰点
		for (int i = 1; i < array.size(); i++) {
			if ((array.get(i).x < (array.get(i - 1).x + 50) && array.get(i).x > (array
					.get(i - 1).x - 50))
					&& (array.get(i).y < (array.get(i - 1).y + 50) && array
							.get(i).y > (array.get(i - 1).y - 50))) {
				array.get(i - 1).x = (array.get(i - 1).x + array.get(i).x) / 2;
				array.get(i - 1).y = (array.get(i - 1).y + array.get(i).y) / 2;
				array.remove(i);
			}
		}

		 for (int i=0; i<array.size(); i++) {
			 Log.i("!Point", "" + array.get(i));
		 }

		assert (array.size() == 2) || (array.size() == 3);
		Point point = new Point();
		if (array.size() >= 2) {
			if (array.size() == 3) {
				if (!onPress) {
					onPress = true;
					startMillis = System.currentTimeMillis();
				} else {
					durMillis = System.currentTimeMillis() - startMillis;
					if (durMillis >= 500 && !inPress) {
						mListener.onPress();
						inPress = true;
					}
				}
				point.x = (array.get(0).x + array.get(2).x) / 2;
				point.y = (array.get(0).y + array.get(2).y) / 2;
			} else {
				if (onPress) {
					if (durMillis < 500) {
						mListener.onClick();
					} else {
						mListener.onRaise();
						inPress = false;
					}
					onPress = false;
				}
				point.x = (array.get(0).x + array.get(1).x) / 2;
				point.y = (array.get(0).y + array.get(1).y) / 2;
			}
			// Log.i("Remote", "X:" + point.x + "/tY:" + point.y);

			mListener.onMove(new Point((point.x - 50) / 300 - 1,
					point.y / 240 - 1));

		}
	}

}
