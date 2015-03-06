package com.arwave.skywriter.objects;

import android.util.Log;

import com.threed.jpct.GenericVertexController;
import com.threed.jpct.IVertexController;
import com.threed.jpct.Mesh;
import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;

/** Provides a basic rectangle plane with a specified number of quads **/
public class Rectangle extends Object3D {

	int quads = 0;
	  Object3D obj;
	  Mesh planeMesh;
	  
	public Rectangle (int quads, float scalex, float scaley) {
		
		super(quads*quads*2+8);
		this.quads = quads;
		  obj=this;
		  
	      createRect(quads, scalex, scaley);
	      	      
	    //  super.setMesh(Primitives.getPlane(quads, 1).getMesh());
	     
			obj.build();

			planeMesh = obj.getMesh();
			
	      this.setSize(1, 1);
	      
	   }



	private void createRect(int quads, float scalex, float scaley) {
		
		Log.i("text", "building for size: = "+scalex+","+scaley);		
		
		float startx=-scalex*(float) quads/2f;
	      float starty=startx;
	      float tx=0f;
	      float ty=0f;
	      float dtex=(1f/(float) quads);
	  //    Object3D obj=this;
	    
	      
	      for (int i=0; i<quads; i++) {
	         for (int p=0; p<quads; p++) {
	            float dtx=tx+dtex;
	            float dty=ty+dtex;
	            if (dtx>1f) {
	               dtx=1f;
	            }
	            if (dty>1f) {
	               dty=1f;
	            }
	            obj.addTriangle(new SimpleVector(startx, starty, 0), tx, ty, new SimpleVector(startx, starty+scaley, 0), tx, dty, new SimpleVector(startx+scalex, starty, 0),
	                            dtx, ty);
	            obj.addTriangle(new SimpleVector(startx, starty+scaley, 0), tx, dty, new SimpleVector(startx+scalex, starty+scaley, 0), dtx, dty, new SimpleVector(startx+scalex,
	                            starty, 0), dtx, ty);
	            startx+=scalex;
	            tx+=dtex;
	         }
	         starty+=scaley;
	         startx=-scalex*quads/2;
	         tx=0;
	         ty+=dtex;
	      }
	}
	
	
	/** uses a vertext controller to rescale  **/
	public void setSize(float scalex, float scaley)	
	{				
		
		 IVertexController demoControl=new ResizerMod(scalex,scaley,1);
		 
		 
		Log.i("text", "resizing to size: = "+scalex+","+scaley);
		
		
		
		planeMesh.setVertexController(demoControl, IVertexController.PRESERVE_SOURCE_MESH);
		planeMesh.applyVertexController();
		//planeMesh.removeVertexController();
		
	}
	
//	public class DemoVertexController extends GenericVertexController {
//
//	     int count;
//
//	     DemoVertexController() {
//	       count=5;
//	     }
//
//	     public void apply() {
//	    	 
//	       SimpleVector[] srcMesh=this.getSourceMesh();
//	       SimpleVector[] dstMesh=this.getDestinationMesh();
//
//	       int size=this.getMeshSize();
//
//	       for (int i=0; i<size; i++) {
//	    	   
//	         float z=srcMesh[i].z;
//	         float x=0.25f*(srcMesh[i].x+count);
//	         float sin=(float) Math.sin(x);
//
//
//	         Log.i("vertex", "vertex="+i+" x="+ dstMesh[i].x);
//	         Log.i("vertex", "vertex="+i+" y="+ dstMesh[i].y);
//	         Log.i("vertex", "vertex="+i+" z="+ dstMesh[i].z);
//	         
//	         dstMesh[i].y=(sin*30f)+srcMesh[i].y;
//	         
//	         
//	         Log.i("vertex", "vertex="+i+" y="+ dstMesh[i].y);
//	       }
//	       count++;
//	     }
//	}
	private static class ResizerMod extends GenericVertexController {
		private static final long serialVersionUID = 1L;

		float XFactor =1;
		float YFactor =1;
		float ZFactor =1;
		
		public ResizerMod(float xFactor, float yFactor, float zFactor) {
			super();
			this.XFactor = xFactor;
			this.YFactor = yFactor;
			this.ZFactor = zFactor;
			
			  Log.i("vertex", "XFactor="+XFactor+" YFactor="+YFactor);
		}


		
		
		public void apply() {
			SimpleVector[] s = getSourceMesh();
			SimpleVector[] d = getDestinationMesh();
			Log.i("vertex", "XFactor="+XFactor+" YFactor="+YFactor);
			for (int i = 0; i < s.length; i++) {
				
				//d[i].z = s[i].z	- (10f * ((float) Math.sin(s[i].x / 50f) + (float) Math.cos(s[i].y / 50f)));

		         Log.i("vertex", "old vertex="+i+" x="+ d[i].x);
		         Log.i("vertex", "old vertex="+i+" y="+ d[i].y);
		         Log.i("vertex", "old vertex="+i+" z="+ d[i].z);
			
				d[i].x = s[i].x*XFactor;
				d[i].y = s[i].y*YFactor;
				d[i].z = s[i].z*ZFactor;
					
		         Log.i("vertex", "vertex="+i+" x="+ d[i].x);
		         Log.i("vertex", "vertex="+i+" y="+ d[i].y);
		         Log.i("vertex", "vertex="+i+" z="+ d[i].z);
			}
		}
	}
}
