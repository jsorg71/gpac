package com.artemis.Osmo4;

import java.io.File;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

/**
 * The renderer
 * 
 * @version $Revision$
 * 
 */
public class Osmo4Renderer implements GLSurfaceView.Renderer {

    /**
     * Default directory for GPAC configuration directory, ends with /
     */
    public final static String GPAC_CFG_DIR;

    private Runnable pendingCommand = null;

    /**
     * Post a command to execute in GPAC Renderer Thread
     * 
     * @param command The command to execute
     */
    public synchronized void postCommand(Runnable command) {
        this.pendingCommand = command;
    }

    /**
     * Default directory for cached files
     */
    public final static String GPAC_CACHE_DIR;

    static {
        File rootCfg = Environment.getExternalStorageDirectory();
        File osmo = new File(rootCfg, "osmo/"); //$NON-NLS-1$
        GPAC_CFG_DIR = osmo.getAbsolutePath() + "/"; //$NON-NLS-1$
        GPAC_CACHE_DIR = new File(osmo, "cache/").getAbsolutePath(); //$NON-NLS-1$
    };

    private final static String LOG_RENDERER = Osmo4Renderer.class.getSimpleName();

    /**
     * Default directory for GPAC modules directory, ends with /
     */
    public final static String GPAC_MODULES_DIR = "/data/data/com.artemis.Osmo4/";//$NON-NLS-1$

    /**
     * Directory of fonts
     */
    public final static String GPAC_FONT_DIR = "/system/fonts/"; //$NON-NLS-1$

    // ------------------------------------
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initGPACDir();
    }

    private boolean inited = false;

    private final String urlToLoad;

    /**
     * Constructor
     * 
     * @param callback
     * @param urlToLoad The URL to load at startup, can be null
     */
    public Osmo4Renderer(GpacCallback callback, String urlToLoad) {
        this.callback = callback;
        this.urlToLoad = urlToLoad;
    }

    private final GpacCallback callback;

    // ------------------------------------
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        // gl.glViewport(0, 0, w, h);
        if (!inited) {
            GpacObject.gpacinit(null,
                                callback,
                                w,
                                h,
                                GPAC_CFG_DIR,
                                GPAC_MODULES_DIR,
                                GPAC_CACHE_DIR,
                                GPAC_FONT_DIR,
                                urlToLoad);
            GpacObject.gpacresize(w, h);
            inited = true;
            if (callback != null)
                callback.onGPACReady();
        } else
            GpacObject.gpacresize(w, h);
    }

    // ------------------------------------
    public void onDrawFrame(GL10 gl) {
        if (inited) {
            GpacObject.gpacrender(null);
            Runnable command;
            synchronized (this) {
                command = this.pendingCommand;
                this.pendingCommand = null;
            }
            if (command != null) {
                command.run();
            }
        }
    }

    // ------------------------------------
    private void initGPACDir() {
        File osmo = new File(GPAC_CFG_DIR);
        if (!osmo.exists()) {
            if (!osmo.mkdirs()) {
                Log.e(LOG_RENDERER, "Failed to create osmo directory " + GPAC_CFG_DIR); //$NON-NLS-1$
            }
        }
        File cache = new File(GPAC_CACHE_DIR);
        if (!cache.exists()) {
            if (!cache.mkdirs()) {
                Log.e(LOG_RENDERER, "Failed to create cache directory " + GPAC_CFG_DIR); //$NON-NLS-1$

            }
        }
    }
    // ------------------------------------
}
