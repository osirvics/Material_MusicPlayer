package com.audio.effiong.musicplayer.filters;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by Victor on 10/3/2016.
 */

public class DirectoryFilter implements FileFilter{
    public boolean accept(File pathname) {
        return pathname.isDirectory();
    }
}
