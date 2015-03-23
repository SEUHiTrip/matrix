package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.Map;

public enum AppType {
	NULL(0), MINECRAFT(1), CAR(2), VIDEO(3), PIC(4), SKYPE(5), IE(6), FILE(
			7), WORD(8), EXCEL(9), PPT(10), CAM(11), DRONE(12), FILE_OPEN(13), LAUNCHER(14);

	private int aa;

	private static Map<Integer, AppType> map = new HashMap<Integer, AppType>();

	static {
		for (AppType legEnum : AppType.values()) {
			map.put(legEnum.aa, legEnum);
		}
	}

	private AppType(final int a) {
		aa = a;
	}

	public static AppType valueOf(int a) {
		return map.get(a);
	}
}
