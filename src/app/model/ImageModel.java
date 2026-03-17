package app.model;

public class ImageModel {
    private int id;          // optional, can be ignored if no DB
    private String facultyName;
    private String fileName;

    public ImageModel(int id, String facultyName, String fileName){
        this.id = id;
        this.facultyName = facultyName;
        this.fileName = fileName;
    }

    public int getId(){ return id; }
    public String getFacultyName(){ return facultyName; }
    public String getFileName(){ return fileName; }
}