package seu.lab.matrix.bullet;
import javax.vecmath.Vector3f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;

import com.threed.jpct.SimpleVector;
import com.threed.jpct.Object3D;
import com.threed.jpct.Matrix;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.linearmath.MatrixUtil;

public class JPCTBulletMotionState extends MotionState {
	public final Transform centerOfMassOffset = new Transform();
	private Object3D obj3d;

	public JPCTBulletMotionState(Object3D obj) {
		obj3d = obj;
		centerOfMassOffset.setIdentity();
	}

	public JPCTBulletMotionState(Object3D obj, Transform startTrans) {
		obj3d = obj;
		setGraphicFromTransform(startTrans);
		centerOfMassOffset.setIdentity();
	}

	public JPCTBulletMotionState(Object3D obj, Transform startTrans,
			Transform centerOfMassOffset) {
		obj3d = obj;
		setGraphicFromTransform(startTrans);
		this.centerOfMassOffset.set(centerOfMassOffset);
	}

	public Transform getWorldTransform(Transform worldTrans) {
		setTransformFromGraphic(worldTrans);
		return worldTrans;
	}

	public void setWorldTransform(Transform worldTrans) {
		setGraphicFromTransform(worldTrans);
	}

	private void setTransformFromGraphic(Transform tran) {
		SimpleVector p = obj3d.getTransformedCenter();
		tran.origin.set(p.x, -p.y, -p.z); // not sure if translation or position

		Matrix matrixGfx = obj3d.getRotationMatrix();
		// matrixGfx.rotateX((float)Math.PI);
		MatrixUtil.getOpenGLSubMatrix(tran.basis, matrixGfx.getDump());
	}

	private void setGraphicFromTransform(Transform tran) {
		SimpleVector pos = obj3d.getTransformedCenter();
		obj3d.translate(tran.origin.x - pos.x, (-tran.origin.y) - pos.y,
				(-tran.origin.z) - pos.z);

		float[] ma = new float[4];
		float[] dump = obj3d.getRotationMatrix().getDump(); // new float[16];
		Matrix matrixGfx = new Matrix();
		MatrixUtil.getOpenGLSubMatrix(tran.basis, dump);

		matrixGfx.setDump(dump);
		matrixGfx.rotateX((float) Math.PI);

		obj3d.setRotationMatrix(matrixGfx);
	}

}