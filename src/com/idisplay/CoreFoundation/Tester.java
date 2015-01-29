package com.idisplay.CoreFoundation;

import java.util.HashMap;

import com.idisplay.CoreFoundation.BinaryPList.PlistTopLevelInfo;
import com.idisplay.util.Logger;

public class Tester {
	
    public static CFBaseTypes __CFTryParseBinaryPlist(byte[] bArr, long j, CFStringBaseT cFStringBaseT) {
    	
    	Logger.d("barr: "+new String(bArr));
    	
        CFBinaryPlistTrailer cFBinaryPlistTrailer = new CFBinaryPlistTrailer();
        PlistTopLevelInfo __CFBinaryPlistGetTopLevelInfo = BinaryPList.__CFBinaryPlistGetTopLevelInfo(bArr, j, cFBinaryPlistTrailer);
        Logger.d("__CFBinaryPlistGetTopLevelInfo.result:"+__CFBinaryPlistGetTopLevelInfo.result);

        if (8 > j || !__CFBinaryPlistGetTopLevelInfo.result) {
            return null;
        }
        byte[] bArr2 = bArr;
        long j2 = j;
        CreateObjectReturnInfo __CFBinaryPlistCreateObject2 = BinaryPList.__CFBinaryPlistCreateObject2(bArr2, j2, __CFBinaryPlistGetTopLevelInfo.offset, cFBinaryPlistTrailer, 0, new HashMap(), null, BinaryPList.kCFPropertyListImmutable);
        
        Logger.d("__CFBinaryPlistCreateObject2.result"+__CFBinaryPlistCreateObject2.result);
        
        if (__CFBinaryPlistCreateObject2.result) {
        	
            return __CFBinaryPlistCreateObject2.plist;
        }
        cFStringBaseT.m_str = "binary data is corrupt";
        Logger.d("cFStringBaseT.m_str = binary data is corrupt;");

        return null;
    }
}
