package com.idisplay.ServerInteractionManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.idisplay.CoreFoundation.CFBaseTypes;
import com.idisplay.CoreFoundation.CFCustomClass;
import com.idisplay.CoreFoundation.CFData;
import com.idisplay.CoreFoundation.CFDictionary;
import com.idisplay.CoreFoundation.CFNull;
import com.idisplay.CoreFoundation.CNSDictionary;
import com.idisplay.CoreFoundation.NSArray;
import com.idisplay.ServerInteractionManager.WindowsManager.IAppContainer;
import com.idisplay.ServerInteractionManager.WindowsManager.IGroupContainer;
import com.idisplay.ServerInteractionManager.WindowsManager.IWindowContainer;
import com.idisplay.util.Logger;
import java.util.ArrayList;

public class WindowsManager {

    public static interface IAppContainer {
        String getAppDir();

        String getCmdLine();

        Bitmap getIcon();

        String getId();

        String getName();

        IWindowContainer[] getWindows();
    }

    static final class AnonymousClass_1 implements IAppContainer {
        final /* synthetic */ String val$appid;
        final /* synthetic */ Bitmap val$bmp;
        final /* synthetic */ String val$cmdLine;
        final /* synthetic */ String val$name;
        final /* synthetic */ IWindowContainer[] val$windows;
        final /* synthetic */ String val$workDir;

        AnonymousClass_1(String str, Bitmap bitmap, IWindowContainer[] iWindowContainerArr, String str2, String str3, String str4) {
            this.val$name = str;
            this.val$bmp = bitmap;
            this.val$windows = iWindowContainerArr;
            this.val$appid = str2;
            this.val$workDir = str3;
            this.val$cmdLine = str4;
        }

        public String getAppDir() {
            return this.val$workDir;
        }

        public String getCmdLine() {
            return this.val$cmdLine;
        }

        public Bitmap getIcon() {
            return this.val$bmp;
        }

        public String getId() {
            return this.val$appid;
        }

        public String getName() {
            return this.val$name;
        }

        public IWindowContainer[] getWindows() {
            return this.val$windows;
        }
    }

    public static interface IGroupContainer {
        IAppContainer[] getApplications();

        String getName();
    }

    static final class AnonymousClass_2 implements IGroupContainer {
        final /* synthetic */ IAppContainer[] val$applications;
        final /* synthetic */ String val$name;

        AnonymousClass_2(String str, IAppContainer[] iAppContainerArr) {
            this.val$name = str;
            this.val$applications = iAppContainerArr;
        }

        public IAppContainer[] getApplications() {
            return this.val$applications;
        }

        public String getName() {
            return this.val$name;
        }
    }

    public static interface IWindowContainer {
        Bitmap getIcon();

        String getId();

        String getName();
    }

    static final class AnonymousClass_3 implements IWindowContainer {
        final /* synthetic */ Bitmap val$bmp;
        final /* synthetic */ String val$id;
        final /* synthetic */ String val$name;

        AnonymousClass_3(String str, Bitmap bitmap, String str2) {
            this.val$name = str;
            this.val$bmp = bitmap;
            this.val$id = str2;
        }

        public Bitmap getIcon() {
            return this.val$bmp;
        }

        public String getId() {
            return this.val$id;
        }

        public String getName() {
            return this.val$name;
        }
    }

    private static IAppContainer[] getApplicationsList(CFBaseTypes cFBaseTypes, boolean z) {
        NSArray nSArray = new NSArray();
        if (nSArray.initWithCustomClass(cFBaseTypes.customClass())) {
            ArrayList nativeArray = nSArray.nativeArray();
            IAppContainer[] iAppContainerArr = new IAppContainer[nativeArray.size()];
            int i = 0;
            while (i < nativeArray.size()) {
                CNSDictionary cNSDictionary = new CNSDictionary();
                if (cNSDictionary.initWithCustomClass(((CFBaseTypes) nativeArray.get(i)).customClass())) {
                    CFDictionary dict = cNSDictionary.getDict();
                    String stringForKey = dict.getStringForKey("name");
                    byte[] binaryRawData = ((CFData) dict.getObjectForKey("icon")).getBinaryRawData();
                    Bitmap decodeByteArray = BitmapFactory.decodeByteArray(binaryRawData, 0, binaryRawData.length);
                    IWindowContainer[] windows = ((dict.getObjectForKey("windows") instanceof CFNull) && z) ? new IWindowContainer[0] : getWindows(dict.getObjectForKey("windows").customClass());
                    iAppContainerArr[i] = new AnonymousClass_1(stringForKey, decodeByteArray, windows, dict.getStringForKey("appid"), dict.getStringForKey("workDir"), dict.getStringForKey("cmdLine"));
                    i++;
                } else {
                    Logger.e("error initing CNSDictionary");
                    throw new IllegalArgumentException("application list error initing CNSDictionary");
                }
            }
            return iAppContainerArr;
        }
        throw new IllegalArgumentException("application list error initing NSArray");
    }

    private static IGroupContainer[] getGroupsList(CFBaseTypes cFBaseTypes) {
        NSArray nSArray = new NSArray();
        if (nSArray.initWithCustomClass(cFBaseTypes.customClass())) {
            ArrayList nativeArray = nSArray.nativeArray();
            IGroupContainer[] iGroupContainerArr = new IGroupContainer[nativeArray.size()];
            int i = 0;
            while (i < nativeArray.size()) {
                CNSDictionary cNSDictionary = new CNSDictionary();
                if (cNSDictionary.initWithCustomClass(((CFBaseTypes) nativeArray.get(i)).customClass())) {
                    CFDictionary dict = cNSDictionary.getDict();
                    iGroupContainerArr[i] = new AnonymousClass_2(dict.getStringForKey("name"), getApplicationsList(dict.getObjectForKey("appList").customClass(), true));
                    i++;
                } else {
                    Logger.e("error initing CNSDictionary");
                    throw new IllegalArgumentException("groups list error initing CNSDictionary");
                }
            }
            return iGroupContainerArr;
        }
        throw new IllegalArgumentException("groups list error initing NSArray");
    }

    private static IWindowContainer getWindow(CFCustomClass cFCustomClass) {
        CNSDictionary cNSDictionary = new CNSDictionary();
        if (cNSDictionary.initWithCustomClass(cFCustomClass)) {
            CFDictionary dict = cNSDictionary.getDict();
            String stringForKey = dict.getStringForKey("name");
            byte[] binaryRawData = ((CFData) dict.getObjectForKey("icon")).getBinaryRawData();
            return new AnonymousClass_3(stringForKey, BitmapFactory.decodeByteArray(binaryRawData, 0, binaryRawData.length), dict.getStringForKey("id"));
        }
        Logger.e("error initing CNSDictionary");
        throw new IllegalArgumentException("wrong window class");
    }

    private static IWindowContainer[] getWindows(CFCustomClass cFCustomClass) {
        NSArray nSArray = new NSArray();
        if (nSArray.initWithCustomClass(cFCustomClass)) {
            ArrayList nativeArray = nSArray.nativeArray();
            IWindowContainer[] iWindowContainerArr = new IWindowContainer[nativeArray.size()];
            for (int i = 0; i < nativeArray.size(); i++) {
                iWindowContainerArr[i] = getWindow(((CFBaseTypes) nativeArray.get(i)).customClass());
            }
            return iWindowContainerArr;
        }
        Logger.e("error initing windows NSArray application without windows");
        throw new IllegalArgumentException("custom class is not windows");
    }

    public void moveWindow(String str) {
        // TODO
    	//ConnectionActivity.ccMngr.sendMoveWindowWithId(str);
    }

    public void onApplicationsLoaded(CFBaseTypes cFBaseTypes) {
        // TODO
//    	ApplicationsManagementActivity.getInstance().updateApplications(getGroupsList(cFBaseTypes));
    }

    public void onWindowsLoaded(CFBaseTypes cFBaseTypes) {
        // TODO
//    	WindowsManagementActivity.getInstance().updateWindows(getApplicationsList(cFBaseTypes, false));
    }

    public void startApplication(String str, String str2, String str3) {
        // TODO
//        ConnectionActivity.ccMngr.sendStartApplication(str, str2, str3);
    }
}
