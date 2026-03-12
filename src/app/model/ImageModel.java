package app.model;

public class ImageModel {

    private int id;
    private String folder;
    private String fileName;

    public ImageModel(int id, String folder, String fileName){
        this.id = id;
        this.folder = folder;
        this.fileName = fileName;
    }

    public int getId() { return id; }
    public String getFolder() { return folder; }
    public String getFileName() { return fileName; }
}