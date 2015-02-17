package seu.lab.matrix;

import com.idisplay.util.ArrayImageContainer;
import com.idisplay.util.ImageContainer;

public interface IDisplayProgram {
	void onInstanceCursorPositionChange(int i, int i2);
	void onInstanceCursorImgChange(ImageContainer imageContainer);
	void setPixels(ArrayImageContainer arrayImageContainer);
}
