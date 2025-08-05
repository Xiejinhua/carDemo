// IMapScreenshot.aidl
package com.desaysv.psmap.adapter;
import com.desaysv.psmap.adapter.IMapScreenshotCallback;

// Declare any non-default types here with import statements

interface IMapScreenshot {
    void addSurface(String surfaceName, in Surface aSurface, int width, int height,int x,int y);

    void removedSurface(String surfaceName);

    void addBitmapBufferCallback(String key, int width, int height,int x,int y);

    void removedBufferCallback(String key);

    void registerScreenshotCallback(in IMapScreenshotCallback callback);

    void unregisterScreenshotCallback(in IMapScreenshotCallback callback);

}