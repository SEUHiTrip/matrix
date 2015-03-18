package seu.lab.matrix;

import seu.lab.matrix.SceneActivity.App;

public class Workspace {
	public boolean isScrShown = false;
	public boolean isPicScrShown = false;
	public boolean isWaiting = false;
	public boolean isPeopleShown = false;
	
	public int mVideoPageIdx;
	public int mPicPageIdx;
	public int mCurrentPic;
	public App mCurrentApp = App.NULL;
	public int mState = 0;	
}
