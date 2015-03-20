package seu.lab.matrix.obj;

import android.R.integer;

public class PictureInfo {
	
	PictureInfo(String name, String desc){
		this.name = name;
		this.desc = desc;
	}
	
	String name;
	int width=1536;
	int height=1024;
	String type="JPEG";
	int iso=((int) (10000*Math.random())/100);
	double duration = 0.001;
	String aperture = "1/4";
	int size = 512;
	String desc;
}
