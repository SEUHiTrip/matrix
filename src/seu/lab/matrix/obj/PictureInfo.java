package seu.lab.matrix.obj;

import android.R.integer;

public class PictureInfo {
	
	public PictureInfo(String name, String desc){
		this.name = name;
		this.desc = desc;
	}
	
	public String name;
	public int width=1536;
	public int height=1024;
	public String type="JPEG";
	public int iso=((int) (10000*Math.random())/100);
	public double duration = 0.001;
	public String aperture = "1/4";
	public int size = 512;
	public String desc;
}
