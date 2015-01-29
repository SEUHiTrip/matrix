package com.idisplay.VirtualScreenDisplay;

import android.opengl.GLES20;
import android.util.Log;

public class Programs {
    private static String kLogTag;

    static {
        kLogTag = "GDC11";
    }

    private static int getShader(String str, int i) {
        int glCreateShader = GLES20.glCreateShader(i);
        if (glCreateShader == 0) {
            return 0;
        }
        GLES20.glShaderSource(glCreateShader, str);
        GLES20.glCompileShader(glCreateShader);
        int[] iArr = new int[]{0};
        GLES20.glGetShaderiv(glCreateShader, 35713, iArr, 0);
        if (iArr[0] != 0) {
            return glCreateShader;
        }
        Log.e(kLogTag, GLES20.glGetShaderInfoLog(glCreateShader));
        return glCreateShader;
    }

    public static int loadProgram(String str, String str2) {
        int i;
        int shader = getShader(str, 35633);
        int shader2 = getShader(str2, 35632);
        if (shader == 0 || shader2 == 0) {
            i = 0;
        } else {
            i = GLES20.glCreateProgram();
            GLES20.glAttachShader(i, shader);
            GLES20.glAttachShader(i, shader2);
            GLES20.glLinkProgram(i);
            int[] iArr = new int[]{0};
            GLES20.glGetProgramiv(i, 35714, iArr, 0);
            if (iArr[0] == 0) {
                Log.e(kLogTag, GLES20.glGetProgramInfoLog(i));
                return 0;
            }
        }
        return i;
    }
}
