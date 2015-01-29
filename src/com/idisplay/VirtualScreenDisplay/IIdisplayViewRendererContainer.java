package com.idisplay.VirtualScreenDisplay;

public interface IIdisplayViewRendererContainer {
    int getDataHeight();

    int getDataStrideX();

    int getDataStrideY();

    int getDataWidth();

    void renderDataUpdated(boolean z);

    void setDataHeight(int i);

    void setDataStrideX(int i);

    void setDataStrideY(int i);

    void setDataWidth(int i);
}
