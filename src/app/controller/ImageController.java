package app.controller;

import app.service.ImageService;
import java.io.File;
import java.util.List;

public class ImageController {

    private ImageService service = new ImageService();

    public List<String> getFacultyFolders(){ return service.getFacultyFolders(); }
    public boolean createFolder(String name){ return service.createFolder(name); }
    public boolean deleteFacultyFolder(String name){ return service.deleteFacultyFolder(name); }
    public List<String> getImagesInFaculty(String faculty){ return service.getImagesInFaculty(faculty); }
    public void importImagesToFaculty(File[] files, String faculty){ service.importImagesToFaculty(files, faculty); }
    public boolean deleteImageFromFaculty(String faculty, String imageName){ return service.deleteImageFromFaculty(faculty, imageName); }
}