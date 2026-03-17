package app.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import app.model.ImageModel;

public class ImageService {

    private final String BASE_DIR = "inc_images";

    public List<String> getFacultyFolders() {
        List<String> folders = new ArrayList<>();
        File base = new File(BASE_DIR);
        if(!base.exists()) base.mkdirs();
        for(File f : base.listFiles()){
            if(f.isDirectory()) folders.add(f.getName());
        }
        return folders;
    }

    public boolean createFolder(String folderName){
        File f = new File(BASE_DIR + "/" + folderName);
        if(f.exists()) return false;
        return f.mkdirs();
    }

    public boolean deleteFacultyFolder(String folderName){
        File f = new File(BASE_DIR + "/" + folderName);
        if(!f.exists()) return false;
        // delete all files inside
        for(File file : f.listFiles()) file.delete();
        return f.delete();
    }

    public List<String> getImagesInFaculty(String faculty){
        List<String> images = new ArrayList<>();
        File folder = new File(BASE_DIR + "/" + faculty);
        if(folder.exists()){
            for(File f : folder.listFiles()){
                if(f.isFile() && (f.getName().toLowerCase().endsWith(".jpg")
                        || f.getName().toLowerCase().endsWith(".png"))){
                    images.add(f.getName());
                }
            }
        }
        return images;
    }

    public void importImagesToFaculty(File[] files, String faculty){
        File folder = new File(BASE_DIR + "/" + faculty);
        if(!folder.exists()) folder.mkdirs();
        for(File f : files){
            try{
                Files.copy(f.toPath(), new File(folder, f.getName()).toPath());
            } catch(IOException e){ e.printStackTrace(); }
        }
    }

    public boolean deleteImageFromFaculty(String faculty, String imageName){
        File f = new File(BASE_DIR + "/" + faculty + "/" + imageName);
        if(!f.exists()) return false;
        return f.delete();
    }
}