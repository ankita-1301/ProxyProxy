package com.java.googletiles;

import java.util.*;
import java.io.File;
import java.util.TimerTask;

public class DeleteFilesNdaysOlder extends TimerTask {

    String path;
    int days;
    private Date currentDate;
    Date lastModified;
    Calendar cal = Calendar.getInstance();
//    Date currentDateNew = new Date();

    public DeleteFilesNdaysOlder(Date currentDate, Integer days, String path) {
        this.days = days;
        this.path = path;
//        this.currentDate = currentDate;
    }

    @Override
    public void run() {
        System.out.println("In DeleteFileNdays");

        File file = new File(path);

        if(!file.exists()){
            System.out.println("No File exists right now");
        }else {
            DeleteFilesNdaysOlder dd = new DeleteFilesNdaysOlder(currentDate,days,path);
            dd.deleteDir(file);
        }
    }

    void deleteDir(File file) {
        if (file.isDirectory()){
            if (file.list().length == 0){
                //if directory is empty and last modified more than days
                System.out.println("Folder Empty");
                if (lastModified(days,file)){
                    System.out.println(file.lastModified());
                    deleteEmptyDir(file);
                }
            }else {
                File files[] = file.listFiles();
                for (File fileDelete : files){
                    //Recursive delete
                    if (lastModified(days,file)){
                        System.out.println(file.lastModified());
                        deleteDir(fileDelete);
                    }
                    //check the dir again if empty and older than days then delete
                    if (file.list().length==0){
                        if (lastModified(days,file)){
                            System.out.println(file.lastModified());
                            deleteEmptyDir(file);
                        }
                    }
                }
            }
        }else {
            //if file last modified older than days delete
            if (lastModified(days,file)){
                System.out.println(file.lastModified());
                file.delete();
                System.out.println("File is Deleted : " + file.getAbsolutePath());
            }
        }
    }

    private boolean lastModified(int days, File file) {

        long diff = new Date().getTime() - file.lastModified();

        System.out.println("difference in time : "+ diff);

        long daysInTime = days * 24 * 60 * 60 * 1000;

        System.out.println("daysInTime : "+ daysInTime);

        boolean olderThanDays;

        if (diff > days * 24 * 60 * 60 * 1000) {
            System.out.println("File is older than "+days+" days");
            olderThanDays = true;
        }else {
            System.out.println("File is not older than "+days+" days");
            olderThanDays = false;
        }
         return olderThanDays;
        }

//        Date currentDate = new Date();
//        Date dateLm = new Date(file.lastModified());
//        System.out.println("Date Last modified : "+ dateLm);
////        Date eventStartDate = event.getStartDate();
//        long daysOlder = days * 24 * 60 * 60 * 1000;
//        System.out.println("daysOlder : " + daysOlder );
//        boolean olderThanDays = currentDate.before(new Date((dateLm .getTime() + daysOlder)));
//     }

    private void deleteEmptyDir(File file) {
        file.delete();
        System.out.println("Dir is Deleted : " + file.getAbsolutePath());
    }
}
