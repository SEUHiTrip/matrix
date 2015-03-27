package seu.lab.matrix.obj;

public class VideoInfo {
	
	public VideoInfo(String name, String desc){
		this.name = name;
		this.desc = desc;
	}
	
	public String name;
	public int width = 1440;
	public int height = 1080;
	public String type = "MKV";
	public int framerate = 30;
	public int length;
	public int size;
	public String desc;
}
