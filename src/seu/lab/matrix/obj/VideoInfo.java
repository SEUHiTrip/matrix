package seu.lab.matrix.obj;

public class VideoInfo {
	
	VideoInfo(String name, String desc){
		this.name = name;
		this.desc = desc;
	}
	
	String name;
	int width = 1440;
	int height = 1080;
	String type = "MKV";
	int framerate = 30;
	int length;
	int size;
	String desc;
}
