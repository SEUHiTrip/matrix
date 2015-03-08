package seu.lab.matrix.bullet;
import com.threed.jpct.*;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.vecmath.Vector3f;

import seu.lab.matrix.BaseTestActivity;
import seu.lab.matrix.R;

import com.bulletphysics.collision.broadphase.AxisSweep3;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.linearmath.*;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;

public class BulletTestActivity extends BaseTestActivity {

	public int numBoxes = 20;
	public int strHeight = 10;

	private World world;
	private FrameBuffer buffer;
	private Object3D box; // only something for the camera to focus on.

	public DiscreteDynamicsWorld dynamicWorld;
	public int maxSubSteps;
	public float timeStep, fixedTimeStep;
	protected Clock clock = new Clock();
	private List<CollisionShape> collisionShapes = new ArrayList<CollisionShape>();
	private BroadphaseInterface overlappingPairCache;
	private CollisionDispatcher dispatcher;
	private ConstraintSolver solver;
	private DefaultCollisionConfiguration collisionConfiguration;

	private Vector<RigidBody> boxList;

	@Override
	public void onNewFrame(HeadTransform headTransform) {
		super.onNewFrame(headTransform);
		float ms = clock.getTimeMicroseconds();
		clock.reset();
		dynamicWorld.stepSimulation(2 * ms / 1000000f);
		
		dynamicWorld.debugDrawWorld();
		
		box.rotateY(0.01f);
	}
	
	@Override
	public void onDrawEye(Eye eye) {
		Camera cam = world.getCamera();

		cam.lookAt(box.getTransformedCenter());
		cam.rotateY(mAngles[1]);
		cam.rotateZ(0 - mAngles[2]);
		cam.rotateX(mAngles[0]);
	
		fb.clear(back);
		world.renderScene(fb);
		world.draw(fb);
		fb.display();
	}
	
	public void initTestObects() {
		Transform transform;
		Object3D boxgfx;
		BoxShape shape = new BoxShape(new Vector3f(2, 2, 2));
		JPCTBulletMotionState ms;
		float mass = 5;
		Vector3f localInertia = new Vector3f(0, 0, 0);
		shape.calculateLocalInertia(mass, localInertia);
		RigidBodyConstructionInfo rbInfo;
		RigidBody body;

		for (int i = 1; i < numBoxes; i++) {
			boxgfx = Primitives.getCube(2);
			boxgfx.setAdditionalColor(new RGBColor(0,0,100));
//			boxgfx.setEnvmapped(Object3D.ENVMAP_ENABLED);
			boxgfx.translate(0, (strHeight + (i * 4)) * -1, (float) 50 - i / 3);
			boxgfx.build();
			boxgfx.rotateY((float) Math.PI / 4f);
			boxgfx.rotateMesh();
			boxgfx.getRotationMatrix().setIdentity();
			world.addObject(boxgfx);

			transform = new Transform();
			transform.setIdentity();

			ms = new JPCTBulletMotionState(boxgfx);

			rbInfo = new RigidBodyConstructionInfo(mass, ms, shape,
					localInertia);
			body = new RigidBody(rbInfo);
			body.setRestitution(0.1f);
			body.setFriction(0.50f);
			body.setDamping(0.0f, 0.0f);
			
			body.setUserPointer(boxgfx);
			boxList.add(body);

			dynamicWorld.addRigidBody(body);
		} // end for loop
	}
	
	@Override
	public void onSurfaceChanged(int w, int h) {
		if (fb != null) {
			fb.dispose();
		}

		fb = new FrameBuffer(w, h);

		if (master == null) {

			world = new World();
			boxList = new Vector();

			// World.setDefaultThread( Thread.currentThread() );
			world.setAmbientLight(120, 120, 120);

			box = Primitives.getBox(1f, 1f);
			box.translate(0, -50, 0);
			box.setAdditionalColor(new RGBColor(100, 0, 0));
//			box.setEnvmapped(Object3D.ENVMAP_ENABLED);
			box.build();
			world.addObject(box);

			Object3D ground = Primitives.getPlane(4, 25);
			ground.setAdditionalColor(new RGBColor(0,100,0));
//			ground.setEnvmapped(Object3D.ENVMAP_ENABLED);
			ground.rotateX((float) -Math.PI);
			ground.build();
			world.addObject(ground);

			cube = Primitives.getCube(1);
			cube.translate(-10, -50, 0);
			cube.calcTextureWrapSpherical();
			cube.strip();
			cube.build();
			world.addObject(cube);
			
			world.getCamera().setPosition(50, -50, -5);
			world.getCamera().lookAt(box.getTransformedCenter());

			Light light = new Light(world);
			light.setPosition(new SimpleVector(-200, -50, 80));
			light.setIntensity(150, 140, 150);

			collisionConfiguration = new DefaultCollisionConfiguration();
			dispatcher = new CollisionDispatcher(collisionConfiguration);
			Vector3f worldAabbMin = new Vector3f(-10000, -10000, -10000);
			Vector3f worldAabbMax = new Vector3f(10000, 10000, 10000);
			AxisSweep3 overlappingPairCache = new AxisSweep3(worldAabbMin,
					worldAabbMax);
			SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

			dynamicWorld = new DiscreteDynamicsWorld(dispatcher,
					overlappingPairCache, solver, collisionConfiguration);
			dynamicWorld.setGravity(new Vector3f(0, -10, 0));
			dynamicWorld.getDispatchInfo().allowedCcdPenetration = 0f;

			CollisionShape groundShape = new BoxShape(new Vector3f(100.f, 50.f,
					100.f));
			Transform groundTransform = new Transform();
			groundTransform.setIdentity();
			groundTransform.origin.set(new Vector3f(0.f, -56.f, 0.f));
			float mass = 0f;
			Vector3f localInertia = new Vector3f(0, 0, 0);
			DefaultMotionState myMotionState = new DefaultMotionState(
					groundTransform);
			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(mass,
					myMotionState, groundShape, localInertia);
			RigidBody body = new RigidBody(rbInfo);
			dynamicWorld.addRigidBody(body);

			dynamicWorld.clearForces();

			initTestObects();
			
			MemoryHelper.compact();

			if (master == null) {
				Logger.log("Saving master Activity!");
				master = this;
			}
		}
	}
	
}
