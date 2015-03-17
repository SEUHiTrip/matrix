package seu.lab.matrix.animation;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Vector;

import raft.jpct.bones.Animated3D;
import raft.jpct.bones.AnimatedGroup;
import raft.jpct.bones.BonesIO;
import raft.jpct.bones.Quaternion;
import raft.jpct.bones.SkeletonDebugger;
import raft.jpct.bones.SkeletonPose;

import android.content.res.AssetManager;

import com.jbrush.ae.EditorObject;
import com.jbrush.ae.Scene;
import com.threed.jpct.Matrix;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;

public class PeopleAnimation implements Animatable{

	private Object3D ogro;
	private Object3D lion;
	private Object3D seymour;

	private float lionInd = 0f;

	private float ogroInd = 0f;

	private long totalTime = 0;
	
	private int workspaceIdx = 0;
	
	private Vector<EditorObject> objects;

	private AnimatedGroup seymourGroup;
	private SkeletonPose currentPose;
	private SkeletonDebugger skeletonDebugger;
	private Object3D ballSphere;
	
	AssetManager am;
	private boolean isOver = false;
	
	public void init(AssetManager am, World world, Object3D workspace, SimpleVector position){
		objects = Scene.loadLevelAE("people.txt", objects, world,
				am);

		ogro = Scene.findObject("ogro", objects);
		lion = Scene.findObject("lion", objects);

		try {
			initSeymour(am, world);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		lion.setScale(0.02f);
		ogro.setScale(0.02f);
		seymour.setScale(0.1f);
		
		lion.rotateX((float) (Math.PI/2));
		ogro.rotateX((float) (Math.PI/2));
		seymour.rotateX((float) (Math.PI/2));
		lion.rotateZ((float) (-Math.PI/2));
		ogro.rotateZ((float) (-Math.PI/2));
		seymour.rotateZ((float) (-Math.PI/2));

		lion.translate(position.calcSub(lion.getTransformedCenter()));
		ogro.translate(position.calcSub(ogro.getTransformedCenter()));
		seymour.translate(position.calcSub(seymour.getTransformedCenter()));
		seymour.translate(0, 0, -0.5f);
		
		workspace.addChild(lion);
		workspace.addChild(ogro);
		workspace.addChild(seymour);

	}
	
	public void reset(){
		lionInd = 0f;
		ogroInd = 0f;
	}
	
	public void setWorkspace(int idx) {
		workspaceIdx = idx > -1 && idx < 3 ? idx : 0;
		switch (workspaceIdx) {
		case 0:
			lion.setVisibility(true);
			ogro.setVisibility(false);
			seymour.setVisibility(false);
			ballSphere.setVisibility(false);
			for (Animated3D o : seymourGroup) {
				o.setVisibility(false);
			}
			break;
		case 1:
			lion.setVisibility(false);
			ogro.setVisibility(true);
			seymour.setVisibility(false);
			ballSphere.setVisibility(false);
			for (Animated3D o : seymourGroup) {
				o.setVisibility(false);
			}
			break;
		case 2:
			lion.setVisibility(false);
			ogro.setVisibility(false);
			seymour.setVisibility(true);
			ballSphere.setVisibility(false);
			for (Animated3D o : seymourGroup) {
				o.setVisibility(true);
			}
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean isOver() {
		return isOver;
	}

	@Override
	public void animate() {
		switch (workspaceIdx) {
		case 0:
			lionInd += 0.03f;
			if (lionInd > 0.85) {
				lionInd = 0;
			}
			lion.animate(lionInd, 1);

			break;
		case 1:
			ogroInd += 0.01f;
			if (ogroInd > 1) {
				ogroInd = 0;
			}
			ogro.animate(ogroInd, 2);

			break;
		case 2:
			totalTime += 25;
			updateBallLocation();

			currentPose.updateTransforms();
			seymourGroup.applySkeletonPose();
			seymourGroup.applyAnimation();	
			break;
		default:
			isOver = true;
			break;
		}
	}

	@Override
	public void onAnimateSuccess() {
		reset();
	}

	@Override
	public void stop() {
		isOver  = true;
	}
	
	private void initSeymour(AssetManager am, World world) throws URISyntaxException, IOException {

		seymourGroup = BonesIO.loadGroup(am.open("seymour.bone"));

		seymour = seymourGroup.getRoot();
		// animatedGroup.getRoot().setScale(0.1f);
		seymour.clearTranslation();
		seymour.translate(15, -2, 40);

		if(!TextureManager.getInstance().containsTexture("seymour")){
			Texture texture = new Texture(am.open("seymour.png"));
			TextureManager.getInstance().addTexture("seymour", texture);
		}

		for (Animated3D o : seymourGroup) {
			o.setTexture("seymour");
			o.build();
			o.discardMeshData();
		}

		seymourGroup.addToWorld(world);

		// all SkinnedObject3D share the same pose
		currentPose = seymourGroup.get(0).getSkeletonPose();

		ballSphere = Primitives.getPlane(1, 1);
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


}
