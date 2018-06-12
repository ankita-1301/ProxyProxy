package com.java.googletiles;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;

import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.io.FileUtils;

public class DeleteFilesNdaysOlder2 extends TimerTask {

    String path;
    int days;

    public DeleteFilesNdaysOlder2(int days, String path) {
        this.days = days;
        this.path = path;
    }

    @Override
    public void run() {
        System.out.println("In DeleteFileNdays");

        File file = new File(path);

        if(!file.exists()){
            System.out.println("No File exists right now");
        }else {
            DeleteFilesNdaysOlder2 dd = new DeleteFilesNdaysOlder2(days,path);
            dd.deleteDir(file);
        }
    }

    private void deleteDir(File file) {
        Date oldestAllowedFileDate = DateUtils.addDays(new Date(), -days); //minus days from current date
        File targetDir = new File(path);
        Iterator<File> filesToDelete = FileUtils.iterateFiles(targetDir, new AgeFileFilter(oldestAllowedFileDate), TrueFileFilter.INSTANCE);
        //if deleting subdirs, replace null above with TrueFileFilter.INSTANCE
        while (filesToDelete.hasNext()) {
            FileUtils.deleteQuietly(filesToDelete.next());
        }  //I don't want an exception if a file is not deleted. Otherwise use filesToDelete.next().delete() in a try/catch
    }
}
