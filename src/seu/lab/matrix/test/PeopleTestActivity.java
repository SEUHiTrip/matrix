package seu.lab.matrix.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;
import raft.jpct.bones.Animated3D;
import raft.jpct.bones.AnimatedGroup;
import raft.jpct.bones.BonesIO;
import raft.jpct.bones.Quaternion;
import raft.jpct.bones.SkeletonDebugger;
import raft.jpct.bones.SkeletonPose;
import seu.lab.matrix.BaseTestActivity;
import seu.lab.matrix.R;
import seu.lab.matrix.R.drawable;
import android.content.res.AssetManager;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.jbrush.ae.EditorObject;
import com.jbrush.ae.Scene;
import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

public class PeopleTestActivity extends BaseTestActivity {

	protected static String TAG = "PeopleTestActivity";

	private Vector<EditorObject> objects;

	private AnimatedGroup animatedGroup;
	private SkeletonPose currentPose;
	private SkeletonDebugger skeletonDebugger;
	private Object3D ballSphere;

	protected RGBColor back = new RGBColor(100, 100, 100);

	private Object3D ogro;
	private Object3D lion;

	private float lionInd = 0f;

	private float ogroInd = 0f;

	private long totalTime = 0;

	@Override
	public void onSurfaceChanged(int w, int h) {

		Config.maxAnimationSubSequences = 100;

		if (fb != null) {
			fb.dispose();
		}

		fb = new FrameBuffer(w, h);

		if (master == null) {

			world = new World();
			world.setAmbientLight(120, 120, 120);

			sun = new Light(world);
			sun.setIntensity(250, 250, 250);

			// Create a texture out of the icon...:-)
			Texture texture = new Texture(BitmapHelper.rescale(
					BitmapHelper.convert(getResources().getDrawable(
							R.drawable.icon)), 64, 64));
			TextureManager.getInstance().addTexture("texture", texture);

			cube = Primitives.getCube(1);
			cube.translate(0, 0, 40);
			cube.calcTextureWrapSpherical();
			cube.setTexture("texture");
			cube.strip();
			cube.build();
			world.addObject(cube);

			plane = Primitives.getPlane(1, 30);
			plane.translate(0, 32, 0);
			plane.calcTextureWrapSpherical();
			plane.setTexture("texture");
			plane.strip();
			plane.build();
			world.addObject(plane);

			AssetManager assetManager = getAssets();
			objects = Scene.loadLevelAE("people.txt", objects, world,
					assetManager);

			ogro = Scene.findObject("ogro", objects);
			lion = Scene.findObject("lion", objects);

			try {
				initSeymour();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Camera cam = world.getCamera();
			cam.lookAt(cube.getTransformedCenter());

			SimpleVector sv = new SimpleVector();
			sv.set(cube.getTransformedCenter());
			sv.y -= 100;
			sv.z -= 100;
			sun.setPosition(sv);
			MemoryHelper.compact();

			if (master == null) {
				Logger.log("Saving master Activity!");
				master = this;
			}
		}
	}

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		super.onNewFrame(headTransform);

		animate();
	}

	@Override
	public void onDrawEye(Eye eye) {
		Camera cam = world.getCamera();

		cam.lookAt(cube.getTransformedCenter());
		cam.rotateY(mAngles[1]);
		cam.rotateZ(0 - mAngles[2]);
		cam.rotateX(mAngles[0]);

		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}

	private void initSeymour() throws URISyntaxException, IOException {

		animatedGroup = BonesIO.loadGroup(getAssets().open("seymour.bone"));

		// animatedGroup.getRoot().setScale(0.1f);
		animatedGroup.getRoot().clearTranslation();
		animatedGroup.getRoot().translate(15, -2, 40);

		Texture texture = new Texture(getAssets().open("seymour.png"));
		TextureManager.getInstance().addTexture("seymour", texture);

		for (Animated3D o : animatedGroup) {
			o.setTexture("seymour");
			o.build();
			o.discardMeshData();
		}

		animatedGroup.addToWorld(world);

		// all SkinnedObject3D share the same pose
		currentPose = animatedGroup.get(0).getSkeletonPose();

		ballSphere = Primitives.getSphere(10, 0.5f);
		ballSphere.setAdditionalColor(new RGBColor(100, 100, 100));
		ballSphere.build();
		world.addObject(ballSphere);

	}

	private void updateBallLocation() {
		float seconds = totalTime / 1000f;	
		// a circular path
		SimpleVector ballPos = new SimpleVector(Math.sin(seconds) * 5,
				-Math.cos(seconds) * 5 - 10 , -5 );

		// Neck
		targetJoint(currentPose, 13, new SimpleVector(0, 0, -1), ballPos, 1.0f);

		// Right arm
		targetJoint(currentPose, 10, new SimpleVector(-1, 0, 0), ballPos, 0.4f);
		targetJoint(currentPose, 11, new SimpleVector(-1, 0, 0), ballPos, 0.6f);
		targetJoint(currentPose, 12, new SimpleVector(-1, 0, 0), ballPos, 0.5f);

		// Left arm
		targetJoint(currentPose, 7, new SimpleVector(1, 0, 0), ballPos, 0.15f);
		targetJoint(currentPose, 8, new SimpleVector(1, 0, 0), ballPos, 0.15f);

		// Waist
		targetJoint(currentPose, 5, new SimpleVector(0, -1, 0), ballPos, 0.1f);

		
		ballSphere.translate(ballPos.calcSub(ballSphere.getTranslation()));
	}

	private void targetJoint(SkeletonPose pose, int jointIndex,
			SimpleVector bindPoseDirection, SimpleVector targetPos,
			final float targetStrength) {

		final int parentIndex = pose.getSkeleton().getJoint(jointIndex)
				.getParentIndex();

		// neckBindGlobalTransform is the neck bone -> model space transform.
		// essentially, it is the world transform of
		// the neck bone in bind pose.
		final Matrix jointInverseBindPose = pose.getSkeleton()
				.getJoint(jointIndex).getInverseBindPose();
		final Matrix jointBindPose = jointInverseBindPose.invert();

		// Get a vector representing forward direction in neck space, use
		// inverse to take from world -> neck space.
		SimpleVector forwardDirection = new SimpleVector(bindPoseDirection);
		forwardDirection.rotate(jointInverseBindPose);

		// Get a vector representing a direction to target point in neck space.
		SimpleVector targetDirection = targetPos.calcSub(
				pose.getGlobal(jointIndex).getTranslation()).normalize();
		targetDirection.rotate(jointInverseBindPose);

		// Calculate a rotation to go from one direction to the other and set
		// that rotation on a blank transform.
		Quaternion quat = new Quaternion();
		quat.fromVectorToVector(forwardDirection, targetDirection);
		quat.slerp(Quaternion.IDENTITY, quat, targetStrength);

		final Matrix subGlobal = quat.getRotationMatrix();

		// now remove the global/world transform of the neck's parent bone,
		// leaving us with just the local transform of
		// neck + rotation.
		subGlobal.matMul(jointBindPose);
		subGlobal.matMul(pose.getSkeleton().getJoint(parentIndex)
				.getInverseBindPose());

		// set that as the neck's transform
		pose.getLocal(jointIndex).setTo(subGlobal);
	}

	private void animate() {
		lionInd += 0.03f;
		ogroInd += 0.01f;
		if (lionInd > 0.9) {
			lionInd = 0;
		}
		if (ogroInd > 1) {
			ogroInd = 0;
		}
		lion.animate(lionInd, 1);
		ogro.animate(ogroInd, 2);

		totalTime += 25;
		updateBallLocation();

		currentPose.updateTransforms();
		animatedGroup.applySkeletonPose();
		animatedGroup.applyAnimation();
	}
}
