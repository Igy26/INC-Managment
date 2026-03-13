package app.controller;

import app.service.ImageService;

import java.io.File;
import java.util.List;

public class ImageController {

    private ImageService service = new ImageService();

    // ---------------- Folder Methods ----------------

    public List<String> getFolders() {
        return service.getFolders();
    }

    public boolean createFolder(String folderName) {
        return service.createFolder(folderName);
    }

    public boolean deleteFolder(String folderName) {
        return service.deleteFolder(folderName);
    }

    // ---------------- Image Methods ----------------

    public List<String> getImagesInFolder(String folder) {
        return service.getImagesInFolder(folder);
    }

    public void importImagesToFolder(File[] files, String folder) {
        service.importImagesToFolder(files, folder);
    }

    public boolean deleteImage(String folder, String imageName) {
        return service.deleteImage(folder, imageName);
    }
}