package app.service;

import app.model.ImageModel;
import app.util.Constants;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ImageService {

    // Get all images (optional, useful for search across all folders)
    public List<ImageModel> getAllImages() {
        List<ImageModel> images = new ArrayList<>();
        File baseDir = new File(Constants.IMAGE_PATH);
        if (!baseDir.exists()) baseDir.mkdirs();

        File[] folders = baseDir.listFiles(File::isDirectory);
        if (folders == null) return images;

        int id = 1;
        for (File folder : folders) {
            File[] files = folder.listFiles(File::isFile);
            if (files == null) continue;
            for (File file : files) {
                images.add(new ImageModel(id++, folder.getName(), file.getName()));
            }
        }
        return images;
    }

    // Create a new folder
    public boolean createFolder(String folderName) {
        if (folderName == null || folderName.trim().isEmpty()) return false;
        File folder = new File(Constants.IMAGE_PATH + folderName);
        if (!folder.exists()) {
            folder.mkdirs();
            return true;
        }
        return false;
    }

    // List all folders
    public List<String> getFolders() {
        List<String> foldersList = new ArrayList<>();
        File baseDir = new File(Constants.IMAGE_PATH);
        if (!baseDir.exists()) baseDir.mkdirs();

        File[] folders = baseDir.listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                foldersList.add(folder.getName());
            }
        }
        return foldersList;
    }

    // List images in a folder
    public List<String> getImagesInFolder(String folderName) {
        List<String> images = new ArrayList<>();
        File folder = new File(Constants.IMAGE_PATH + folderName);
        if (!folder.exists()) return images;

        File[] files = folder.listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                if (isImageFile(file)) images.add(file.getName());
            }
        }
        return images;
    }

    // ------------------------ DELETE METHODS ------------------------

    /**
     * Delete a folder and all its contents recursively.
     */
    public boolean deleteFolder(String folderName) {
        File folder = new File(Constants.IMAGE_PATH + folderName);
        if (!folder.exists() || !folder.isDirectory()) return false;
        return deleteRecursively(folder);
    }

    /**
     * Delete a single image file inside a folder.
     */
    public boolean deleteImage(String folderName, String fileName) {
        File file = new File(Constants.IMAGE_PATH + folderName + "/" + fileName);
        if (!file.exists() || !file.isFile()) return false;
        return file.delete();
    }

    // ------------------------ IMPORT ------------------------
    public void importImagesToFolder(File[] files, String folderName) {
        try {
            File folder = new File(Constants.IMAGE_PATH + folderName);
            if (!folder.exists()) folder.mkdirs();

            for (File file : files) {
                File dest = new File(folder, file.getName());
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------ HELPERS ------------------------
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
                || name.endsWith(".bmp");
    }

    private boolean deleteRecursively(File f) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        return f.delete();
    }
}