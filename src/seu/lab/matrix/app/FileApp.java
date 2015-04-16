package seu.lab.matrix.app;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opencv.core.Point;

import android.R.id;
import android.os.Bundle;
import android.util.Log;

import com.idisplay.VirtualScreenDisplay.IDisplayConnection.ConnectionMode;
import com.threed.jpct.Camera;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;

import seu.lab.matrix.SceneHelper;
import seu.lab.matrix.animation.Animatable;
import seu.lab.matrix.animation.PickGroup;
import seu.lab.matrix.animation.SeqAnimation;
import seu.lab.matrix.animation.TranslationAnimation;

public class FileApp extends AbstractApp {

	private World world;

	final static String[] fileUrl = new String[] {
	// "c:\\sample.jpg",
	// "c:\\sample.pdf",
	"c:\\sample.pptx",
	// "c:\\sample.xlsx",
	// "c:\\sample.mkv",
	// "c:\\sample.docx",
	// "c:\\sample.html",
	// "c:\\sample",
	};

	public int mFilePageIdx = 0;
	public int mPickState = 0;

	SimpleVector trashPos;

	public FileApp(List<Animatable> animatables, SceneCallback callback,
			Camera camera, Object3D ball1, World world) {
		super(animatables, callback, camera, ball1);
		this.world = world;
	}

	enum Minetype {
		JPG, PDF, PPT, EXECL, VIDEO, WORD, WEBPAGE, CODE, FOLDER_CLOUD
	}

	PickGroup[] mPickGroupFiles = new PickGroup[3];
	PickGroup[] mPickGroupFiles1 = new PickGroup[9];
	PickGroup[] mPickGroupFiles2 = new PickGroup[9];

	PickGroup[] mPickGroupDesks = new PickGroup[5];

	protected Object3D[] desks = null;
	protected Object3D[] files = null;
	protected Object3D[] files1 = null;
	protected Object3D[] files2 = null;

	List<Integer> trashs = new LinkedList<Integer>();

	private Map<String, Object3D> clickableFiles = new HashMap<String, Object3D>();
	private Map<String, Object3D> clickableDesks = new HashMap<String, Object3D>();

	private boolean pickable = true;
	private boolean[] filepickable = new boolean[9];

	private boolean isGrabingingFile = false;

	private Object3D mGrabbingFile;
	private int mGrabbingFileIdx;

	private long lastGrabTime;

	private int mCurFileIdx;

	SimpleVector getFilePos(int i, int x) {
		SimpleVector target = new SimpleVector();
		cam.getPosition(target);
		target.add(new SimpleVector(-3 - x, 0.8 * (i / 3 - 1),
				0.5 + 0.8 * (i % 3 - 1)));
		return target;
	}

	void resetFilePosition(Object3D[] files, int x) {
		for (int i = 0; i < 9; i++) {
			files[i].translate(getFilePos(i, x).calcSub(
					files[i].getTransformedCenter()));
			Log.e(TAG, "files[i]:" + files[i].getTransformedCenter());
		}
	}

	void resetFilePickable() {
		trashs.clear();
		for (int i = 0; i < filepickable.length; i++) {
			filepickable[i] = true;
		}
	}

	void file() {

		resetFilePickable();

		startFrom(desks, 0, desks.length, 5);
		startFrom(files1, 0, files1.length, 5);
		resetFilePosition(files1, 5);
		// startFrom(files2, 5);
		// resetFilePosition(files2, 5);

		for (int i = 1; i < mPickGroupDesks.length; i++) {
			mPickGroupDesks[i].group[0].setVisibility(false);
		}
		
		mAnimatables.add(new TranslationAnimation("", SceneHelper
				.to1DArr(new Object3D[][] { desks, files1 }), new SimpleVector(
				5, 0, 0), null));
	}

	void startFrom(Object3D[] object3ds, int s, int e, int distance) {
		for (int i = s; i < e; i++) {
			object3ds[i].clearTranslation();
			object3ds[i].translate(-distance, 0, 0);
			object3ds[i].setVisibility(true);
		}
	}

	class FilePickGroup extends PickGroup {

		public String oriTex;
		public String activeTex;

		public FilePickGroup(int count, String oriTex, String activeTex) {
			super(count);
			this.oriTex = oriTex;
			this.activeTex = activeTex;
		}

		@Override
		public void active() {
			group[0].setTexture(activeTex);
		}

		@Override
		public void deactive() {
			group[0].setTexture(oriTex);
		}
	}

	private void postInitFile() {
		for (int i = 0; i < mPickGroupFiles.length; i++) {
			mPickGroupFiles[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupDesks.length; i++) {
			mPickGroupDesks[i] = new PickGroup(1);
		}
		for (int i = 0; i < mPickGroupFiles1.length; i++) {
			mPickGroupFiles1[i] = new FilePickGroup(1, "minetype_" + i, "info_"
					+ i);
		}
		for (int i = 0; i < mPickGroupFiles2.length; i++) {
			mPickGroupFiles2[i] = new FilePickGroup(1, "minetype_" + i, "info_"
					+ i);
		}
		Object3D tmp;
		tmp = clickableDesks.get("f_trash");
		tmp.setTexture("sw_dolphin_on");
		trashPos = tmp.getTransformedCenter();
		mPickGroupDesks[0].group[0] = tmp;

		tmp = clickableDesks.get("f_desk");
		tmp.setTexture("sw_brown");

		tmp = clickableDesks.get("f_i_open");
		tmp.setVisibility(false);
		mPickGroupDesks[1].group[0] = tmp;

		tmp = clickableDesks.get("f_i_copy");
		tmp.setVisibility(false);
		mPickGroupDesks[2].group[0] = tmp;

		tmp = clickableDesks.get("f_i_cut");
		tmp.setVisibility(false);
		mPickGroupDesks[3].group[0] = tmp;

		tmp = clickableDesks.get("f_i_delete");
		tmp.setVisibility(false);
		mPickGroupDesks[4].group[0] = tmp;

		tmp = clickableFiles.get("f_book_b");
		mPickGroupFiles[0].group[0] = tmp;

		tmp = clickableFiles.get("f_book_s");
		mPickGroupFiles[1].group[0] = tmp;

		tmp = clickableFiles.get("f_b_folder");
		mPickGroupFiles[2].group[0] = tmp;

		desks = new Object3D[clickableDesks.size()];
		files = new Object3D[clickableFiles.size()];

		files1 = new Object3D[mPickGroupFiles1.length];
		files2 = new Object3D[mPickGroupFiles2.length];

		desks = clickableDesks.values().toArray(desks);
		files = clickableFiles.values().toArray(files);

		for (int i = 0; i < 9; i++) {

			if (i != 8) {
				files1[i] = mPickGroupFiles[1].group[0].cloneObject();
			} else {
				files1[i] = mPickGroupFiles[0].group[0].cloneObject();
			}

			files1[i].rotateZ(-0.4f);
			files1[i].setTexture("minetype_" + i);
			mPickGroupFiles1[i].group[0] = files1[i];
			mPickGroupFiles1[i].oriPos[0] = getFilePos(i, 0);
			world.addObject(files1[i]);

			if (i != 8) {
				files2[i] = mPickGroupFiles[1].group[0].cloneObject();
			} else {
				files2[i] = mPickGroupFiles[0].group[0].cloneObject();
			}
			files2[i].rotateZ(-0.4f);
			files1[i].setTexture("minetype_" + i);

			mPickGroupFiles2[i].group[0] = files2[i];
			mPickGroupFiles2[i].oriPos[0] = getFilePos(i, 0);
			world.addObject(files2[i]);
		}

	}

	private void initFile(String name, Object3D object3d) {
		object3d.setVisibility(false);
		// object3d.setAdditionalColor(new RGBColor(100, 100, 100));

		int end = -1;
		end = end == -1 ? name.indexOf("_Plane") : end;
		end = end == -1 ? name.indexOf("_Cube") : end;
		end = end == -1 ? name.indexOf("_Cylinder") : end;
		end = end == -1 ? name.indexOf("_OBJ") : end;

		String tname = name.substring(2, (end == -1 ? name.length() : end));

		Log.e(TAG, "nice: " + tname);

		if (tname.startsWith("f_b")) {
			object3d.getMesh().setLocked(true);
			// printTexture(tname, object3d);
			clickableFiles.put(tname, object3d);
		} else if (tname.startsWith("f_i_")) {
			SceneHelper.printTexture(tname, object3d);
			clickableDesks.put(tname, object3d);
		} else {
			clickableDesks.put(tname, object3d);
		}
	}

	private void toggleDesk(boolean on) {
		for (int i = 0; i < desks.length; i++) {
			desks[i].setVisibility(on);
		}
		for (int i = 0; i < files.length; i++) {
			files[i].setVisibility(on);
		}
		for (int i = 0; i < files1.length; i++) {
			files1[i].setVisibility(on);
		}
		for (int i = 0; i < files2.length; i++) {
			files2[i].setVisibility(on);
		}
	}

	@Override
	public void onPick() {
		// TODO
		if (!pickable)
			return;

		// pickList(mPickGroupDesks, 1, mPickGroupDesks.length);

		boolean hasPick = false;
		for (int i = 0; i < mPickGroupDesks.length; i++) {
			if(!mPickGroupDesks[i].group[0].getVisibility())break;
			if (!hasPick && SceneHelper.isLookingAt(cam, ball1,
					mPickGroupDesks[i].group[0].getTransformedCenter()) > 0.997) {
				mPickGroupDesks[i].group[0].setScale(1.5f);
				hasPick = true;
			} else {
				mPickGroupDesks[i].group[0].setScale(1f);
			}
		}

		if(hasPick)return;
		if (mFilePageIdx == 0) {
			pickList(mPickGroupFiles1, 0, mPickGroupFiles1.length, true,
					filepickable);
		} else {
			pickList(mPickGroupFiles2, 0, mPickGroupFiles2.length, true,
					filepickable);
		}
	}

	@Override
	public void initObj(String name, Object3D object3d) {
		initFile(name, object3d);
	}

	@Override
	public void postInitObj() {
		postInitFile();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShown() {
		file();
	}

	@Override
	public void onHide() {
		toggleDesk(false);
	}

	@Override
	public void onLeft() {
		if (System.currentTimeMillis() - lastGrabTime < 100)
			return;

		// scroll file left
		slideFile(false);
	}

	@Override
	public void onRight() {
		if (System.currentTimeMillis() - lastGrabTime < 100)
			return;

		// scroll file right
		slideFile(true);
	}

	@Override
	public void onUp() {
		if (System.currentTimeMillis() - lastGrabTime < 100)
			return;

		// go to up level
		goUpFolder(8);
	}

	@Override
	public void onDown() {
		if (System.currentTimeMillis() - lastGrabTime < 100)
			return;
		// go to home folder
		goHomeFolder();
	}

	@Override
	public void onLongPress() {

	}

	@Override
	public boolean onDoubleTap() {
		// 1,2,3,4
		//
		// -1,1,-1,1
		// -1,-1,1,1

		// is looking at the trash, restore
		Log.e(TAG,
				"restore==? " + SceneHelper.isLookingAt(cam, ball1, trashPos));
		if (SceneHelper.isLookingAt(cam, ball1, trashPos) > 0.98) {
			restore();
		} else if (mPickState == 0) {

			Object3D[] files = mFilePageIdx == 0 ? files1 : files2;

			for (int i = 0; i < files.length; i++) {
				if (SceneHelper.isLookingAt(cam, ball1,
						files[i].getTransformedCenter()) > 0.99) {

					mPickState = 1;
					mCurFileIdx = i;
					PickGroup group = null;
					SimpleVector tmp;
					for (int j = 1; j < mPickGroupDesks.length; j++) {
						group = mPickGroupDesks[j];

						SimpleVector target = files[i].getTransformedCenter();
						SimpleVector origin = group.group[0]
								.getTransformedCenter();
						tmp = new SimpleVector(target.x - origin.x, target.y
								- origin.y, target.z - origin.z);
						float x = tmp.x + 0.2f;
						float y = tmp.y - 0.3f;
						float z = tmp.z - 0.5f + j * 0.2f;
						tmp.set(x, y, z);
						group.group[0].translate(tmp);
						group.group[0].setVisibility(true);
						Log.e("tag",
								i + "    j:" + j + "   "
										+ group.group[0].getTransformedCenter());
					}
					Log.e("tag",
							"Double tap! " + i + "    "
									+ files[i].getTransformedCenter());

					// for (int j = 1; j < mPickGroupDesks.length; j++) {
					// group = mPickGroupDesks[j];
					// tmp = getFilePos(i, 0);
					// group.group[0].translate(tmp);
					// group.oriPos[0] = tmp.calcAdd(new SimpleVector(1,
					// (j % 2 * 2 - 1) * 0.2, (j / 2 * 2 - 1) * 0.2));
					// }

					break;
				}
			}

		} else {
			PickGroup group = null;
//			SimpleVector tmp = new SimpleVector(0, 0, -1000);

			// show the icons
			// pick icons and do the action

			for (int j = 1; j < mPickGroupDesks.length; j++) {
				if (SceneHelper.isLookingAt(cam, ball1,
						mPickGroupDesks[j].group[0].getTransformedCenter()) > 0.997) {
					doFileActions(j, mCurFileIdx);
				}
				group = mPickGroupDesks[j];
				group.group[0].setVisibility(false);

//				group.group[0].translate(tmp);
//				group.oriPos[0] = null;
			}
			mPickState = 0;
		}

		return false;
	}

	void doFileActions(int aidx, int fidx) {
		Log.e(TAG, "doFileActions: " + aidx + " f:" + fidx);
		switch (aidx) {
		case 1:
			if(fidx == 8){
				goDownFolder(8);
			}else {
				openFileOnScene(fileUrl[0]);
			}
			break;
		case 2:
			// copy
			break;
		case 3:
			// cut
			break;
		case 4:
			deleteFile(fidx);
			break;
		default:
			break;
		}
	}

	@Override
	public void onSingleTap() {
		// for test
		// goDownFolder(8);
		// openFileOnScene(fileUrl[0]);

//		deleteFile(trashidx++);

	}

	@Override
	public void onOpen(Bundle bundle) {
		onShown();
		scene.onAppReady();
	}

	@Override
	public void onClose(Runnable runnable) {
		scene.onHideObj(
				SceneHelper.to1DArr(new Object3D[][] { desks, files1, files2 }),
				false, runnable);
		scene.onAppClosed();
	}

	void openFileOnScene(String file) {
		// can open office

		Bundle bundle = new Bundle();
		bundle.putString("file", file);

		scene.onOpenApp(AppType.FILE_OPEN.ordinal() - 1, bundle);
	}

	void slideFile(boolean slideLeft) {
		mFilePageIdx = (mFilePageIdx + 1) % 2;

		resetFilePickable();
		trashs.clear();

		resetFilePosition(files1, 0);
		resetFilePosition(files2, 0);

		Object3D[] pre, cur;

		if (mFilePageIdx == 0) {
			pre = files2;
			cur = files1;
		} else {
			pre = files1;
			cur = files2;
		}

		pickable = false;
		slideList(slideLeft, pre, cur, 4, false, true, new Runnable() {

			@Override
			public void run() {
				pickable = true;
			}
		});
	}

	void goUpFolder(final int idx) {
		resetFilePickable();

		// all files fly to idx in order
		Object3D[] files = mFilePageIdx == 0 ? files1 : files2;
		final Object3D[] f = new Object3D[files.length - 1];
		for (int i = 0; i < f.length; i++) {
			f[i] = files[i];
		}

		pickable = false;

		Animatable[] as = new Animatable[f.length];

		for (int i = 0; i < as.length; i++) {

			if (i == as.length - 1) {
				as[i] = new TranslationAnimation(
						"",
						new Object3D[] { f[i] },
						getFilePos(idx, 0).calcSub(f[i].getTransformedCenter()),
						null) {
					@Override
					public void onAnimateSuccess() {
						// all files except idx step forward

						for (int j = 0; j < f.length; j++) {
							f[j].translate(getFilePos(j, 0).calcAdd(
									new SimpleVector(-10, 0, 0)).calcSub(
									f[j].getTransformedCenter()));
						}

						mAnimatables.add(new TranslationAnimation("", f,
								new SimpleVector(10, 0, 0), null) {
							@Override
							public void onAnimateSuccess() {
								pickable = true;
								super.onAnimateSuccess();
							}
						});

						super.onAnimateSuccess();
					}
				};
			} else {
				as[i] = new TranslationAnimation(
						"",
						new Object3D[] { f[i] },
						getFilePos(idx, 0).calcSub(f[i].getTransformedCenter()),
						null);
			}
		}

		mAnimatables.add(new SeqAnimation(mAnimatables, as));

	}

	void goDownFolder(final int idx) {

		resetFilePickable();
		
		// all files except idx step back
		Object3D[] files = mFilePageIdx == 0 ? files1 : files2;
		final Object3D[] f = new Object3D[files.length - 1];
		for (int i = 0; i < f.length; i++) {
			f[i] = files[i];
		}

		pickable = false;
		mAnimatables.add(new TranslationAnimation("", f, new SimpleVector(-10,
				0, 0), null) {
			@Override
			public void onAnimateSuccess() {
				super.onAnimateSuccess();
				// all files fly from idx to it pos in order

				Animatable[] as = new Animatable[f.length];

				for (int i = 0; i < as.length; i++) {
					f[i].translate(getFilePos(idx, 0).calcSub(
							f[i].getTransformedCenter()));
					as[i] = new TranslationAnimation("",
							new Object3D[] { f[i] }, getFilePos(i, 0).calcSub(
									f[i].getTransformedCenter()), null);
				}
				mAnimatables.add(new SeqAnimation(mAnimatables, as) {
					@Override
					public void onAnimateSuccess() {
						pickable = true;
						super.onAnimateSuccess();
					}
				});
			}
		});
	}

	void goHomeFolder() {
		resetFilePickable();

		// all files step back
		final Object3D[] files = mFilePageIdx == 0 ? files1 : files2;

		// files slide from bottom to table
		pickable = false;
		mAnimatables.add(new TranslationAnimation("", files, new SimpleVector(
				-10, 0, 0), null) {
			@Override
			public void onAnimateSuccess() {
				super.onAnimateSuccess();

				Animatable[] as = new Animatable[9];

				for (int i = 0; i < as.length; i++) {
					files[i].translate(getFilePos(i, 0).calcAdd(
							new SimpleVector(0, 0, -5).calcSub(files[i]
									.getTransformedCenter())));
					as[i] = new TranslationAnimation("",
							new Object3D[] { files[i] }, new SimpleVector(0, 0,
									5), null);
				}
				mAnimatables.add(new SeqAnimation(mAnimatables, as) {
					@Override
					public void onAnimateSuccess() {
						pickable = true;
						super.onAnimateSuccess();
					}
				});
			}
		});

	}

	void deleteFile(final int idx) {
		// delete the [idx] file
		if (!filepickable[idx])
			return;

		filepickable[idx] = false;
		final Object3D[] f = new Object3D[] { mFilePageIdx == 0 ? files1[idx]
				: files2[idx] };
		// fly to the trash and stay in the trash

		// step forward
		pickable = false;
		mAnimatables.add(new TranslationAnimation("", f, new SimpleVector(0.5,
				0, 0), null) {
			@Override
			public void onAnimateSuccess() {
				// to the trash
				trashs.add(idx);
				deleteFile(f, idx);
				super.onAnimateSuccess();
			}
		});
	}

	void deleteFile(Object3D[] f, int idx) {
		mAnimatables.add(new TranslationAnimation("", f, trashPos.calcSub(f[0]
				.getTransformedCenter().calcAdd(
						new SimpleVector(0, 0.5 + 0.075 * idx, 0))), null) {
			@Override
			public void onAnimateSuccess() {
				pickable = true;
				super.onAnimateSuccess();
			}

		});
	}

	@Override
	public boolean onToggleFullscreen() {
		// openFileOnScene(fileUrl[0]);
		return false;
	}

	@Override
	public void onMove(Point p) {
		// if file if picked, rotate it with ball
		if (isGrabingingFile) {
			scene.onGrabObj(mGrabbingFile, 0.5f);
		}
	}

	@Override
	public void onClick() {
	} // on double tapped

	@Override
	public void onPress(Point p) {
		// if is looking at a file, pick it and rotate with ball

		final Object3D[] files = mFilePageIdx == 0 ? files1 : files2;

		for (int i = 0; i < files.length; i++) {
			if (filepickable[i]
					&& SceneHelper.isLookingAt(cam, ball1,
							files[i].getTransformedCenter()) > 0.99) {
				mGrabbingFileIdx = i;
				mGrabbingFile = files[i];
				pickable = false;
				isGrabingingFile = true;
				break;
			}
		}
	}

	void restore() {
		Log.e(TAG, "restore");

		final Object3D[] files = mFilePageIdx == 0 ? files1 : files2;

		pickable = false;
		for (Integer ii : trashs) {
			filepickable[ii] = true;
			mAnimatables.add(new TranslationAnimation("",
					new Object3D[] { files[ii] }, getFilePos(ii, 0).calcSub(
							files[ii].getTransformedCenter()), null) {
				@Override
				public void onAnimateSuccess() {
					pickable = true;
					super.onAnimateSuccess();
				}
			});
		}
		trashs.clear();
	}

	@Override
	public void onRaise(Point p) {
		// if is picking a file, translate it to the ori,

		if (!isGrabingingFile)
			return;

		pickable = true;
		isGrabingingFile = false;
		lastGrabTime = System.currentTimeMillis();

		// if is the trash dir, delete it
		// else back to ori
		if (SceneHelper.isLookingAt(cam, ball1, trashPos) > 0.98) {
			filepickable[mGrabbingFileIdx] = false;
			pickable = false;
			trashs.add(mGrabbingFileIdx);
			deleteFile(new Object3D[] { mGrabbingFile }, mGrabbingFileIdx);
		} else {
			pickable = false;
			// mGrabbingFile.clearRotation();
			// mGrabbingFile.clearTranslation();

			mAnimatables.add(new TranslationAnimation("",
					new Object3D[] { mGrabbingFile }, getFilePos(
							mGrabbingFileIdx, 0).calcSub(
							mGrabbingFile.getTransformedCenter()), null) {
				@Override
				public void onAnimateSuccess() {
					pickable = true;

					super.onAnimateSuccess();
				}
			});
		}

	}
}
