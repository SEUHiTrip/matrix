package seu.lab.matrix.app;

import java.util.List;

import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.controllers.AppController.app_name;

import android.os.Bundle;

import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;

public class PPTApp extends SimpleScreenApp{

	public PPTApp(List<Animatable> animatables,
			SceneCallback callback, Camera camera, Object3D ball1) {
		super(app_name.office_ppt, animatables, callback, camera, ball1);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onOpen(Bundle bundle) {
		scene.onScript("这里有我们需要编辑的文档\n当然既然是协作\n实时沟通最重要\n所以我们再打开Skype与团队进行视频");
		super.onOpen(bundle);
	}

}
