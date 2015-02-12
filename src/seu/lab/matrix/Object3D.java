package seu.lab.matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import android.opengl.GLES20;

public abstract class Object3D {

    protected static String TAG = "MatrixActivity";
	
	protected static final int COORDS_PER_VERTEX = 3;
    
    protected int mProgram;
    protected int mPositionParam;
    protected int mNormalParam;
    protected int mColorParam;
	protected int mModelParam;
	protected int mModelViewParam;
	protected int mModelViewProjectionParam;
	protected int mLightPosParam;
	
	protected int vertexShader;
	protected int fragShader;

	protected FloatBuffer mVertices;
	protected FloatBuffer mColors;
	protected FloatBuffer mNormals;

	protected float[] COORDS;
	protected float[] COLORS;
	protected float[] NORMALS;
	
	public Object3D(float[] _COORDS, float[] _COLORS, float[] _NORMALS,
			int _vertexShader, int _fragShader) {

		vertexShader = _vertexShader;
		fragShader = _fragShader;
		
		COORDS = _COORDS;
		COLORS = _COLORS;
		NORMALS = _NORMALS;
		
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        mVertices = bbVertices.asFloatBuffer();
        mVertices.put(COORDS);
        mVertices.position(0);

        ByteBuffer bbColors = ByteBuffer.allocateDirect(COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        mColors = bbColors.asFloatBuffer();
        mColors.put(COLORS);
        mColors.position(0);
        
        ByteBuffer bbNormals = ByteBuffer.allocateDirect(NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        mNormals = bbNormals.asFloatBuffer();
        mNormals.put(NORMALS);
        mNormals.position(0);
		
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragShader);
        GLES20.glLinkProgram(mProgram);
        GLES20.glUseProgram(mProgram);
        
	}
	
	public abstract void initParams();
	public abstract void draw(float[] mMVP, float[] mLightPosInEyeSpace, float[] mModelView);
}
