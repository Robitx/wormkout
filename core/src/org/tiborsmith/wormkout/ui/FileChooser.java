package org.tiborsmith.wormkout.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by tibor on 22.8.14.
 */
public abstract class FileChooser extends Table {


    public static interface Listener {

        /** called when a single file was chosen */
        public void choose(FileHandle file);

        /** Called when multiple files were chosen.
         *  The Array given into the method will be {@link com.badlogic.gdx.utils.Pools#free(Object) returned} to the pool after the call, so make a copy if you need one.
         *  @see #choose(FileHandle) */
        public void choose(Array<FileHandle> files);

        /** Called when choosing one or more files was cancelled */
        public void cancel();

    }

    /** the {@link Listener} to notify */
    private Listener listener;

    /** determines if a file should be shown taking {@link #fileFilter} and {@link #showHidden} into account */
    protected final FileFilter handlingFileFilter = new FileFilter() {

        @Override
        public boolean accept(File file) {
            return (showHidden || !file.isHidden()) && (fileFilter == null || fileFilter.accept(file));
        }

    };

    /** a personal filter to determine if certain files should be shown */
    private FileFilter fileFilter;

    /** if hidden files should be shown */
    private boolean showHidden = false;

    /** if directories can be chosen */
    private boolean directoriesChoosable;

    /** @param listener the {@link #listener} */
    public FileChooser(Listener listener) {
        this.listener = listener;
    }

    /** override this to build widgets in an implementation */
    protected abstract void build();

    /** @return the {@link #listener} */
    public Listener getListener() {
        return listener;
    }

    /** @param listener the {@link #listener} to set */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** @return the {@link #fileFilter} */
    public FileFilter getFileFilter() {
        return fileFilter;
    }

    /** @param fileFilter the {@link #fileFilter} to set */
    public void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    /** @return the {@link #showHidden} */
    public boolean isShowHidden() {
        return showHidden;
    }

    /** @param showHidden the {@link #showHidden} to set */
    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
    }

    /** @return the {@link #directoriesChoosable} */
    public boolean isDirectoriesChoosable() {
        return directoriesChoosable;
    }

    /** @param directoriesChoosable the {@link #directoriesChoosable} to set */
    public void setDirectoriesChoosable(boolean directoriesChoosable) {
        this.directoriesChoosable = directoriesChoosable;
    }

}