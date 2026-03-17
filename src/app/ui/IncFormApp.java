package app.ui;

import app.controller.ImageController;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class IncFormApp extends JFrame {

    JTable table;
    JLabel imageViewer;
    JTextField searchField;
    DefaultTableModel model;
    JLabel breadcrumb;
    JButton backButton;

    double scale = 1.0;
    String currentFaculty = null; // current folder
    String currentImage = null;

    ImageController service = new ImageController();

    Color PRIMARY = new Color(52,152,219);
    Color DARK = new Color(44,62,80);
    Color LIGHT = new Color(245,246,250);

    boolean showingFolders = true;
    JScrollPane imageScrollPane;

    public IncFormApp() {
        setTitle("INC Form Image Manager");
        setSize(1200,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(LIGHT);

        initUI();
        loadFolders();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ------------------------ Top Toolbar ------------------------
        backButton = new JButton("← Back");
        styleButton(backButton);
        backButton.setVisible(false);
        backButton.addActionListener(e -> loadFolders());

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftToolbar.setOpaque(false);
        leftToolbar.add(backButton);

        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        rightToolbar.setOpaque(false);

        searchField = new JTextField("Search..",20);
        searchField.setForeground(Color.GRAY);
        searchField.setPreferredSize(new Dimension(200,28));

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if(searchField.getText().equals("Search..")){
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if(searchField.getText().isEmpty()){
                    searchField.setForeground(Color.GRAY);
                    searchField.setText("Search..");
                }
            }
        });

        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                String text = searchField.getText().trim();
                if(text.isEmpty() || text.equals("Search..")){
                    loadFolders(); // show folders again
                } else {
                    filterImagesByFileName(text);
                }
            }
        });

        // ------------------------ Buttons ------------------------
        JButton importFolderButton = new JButton("Import Folder(s)");
        styleButton(importFolderButton);
        importFolderButton.addActionListener(e -> importFolders());

        JButton importImageButton = new JButton("Import Image");
        styleButton(importImageButton);
        importImageButton.addActionListener(e -> importImages());

        JButton deleteButton = new JButton("Delete");
        styleButton(deleteButton);
        deleteButton.setBackground(Color.RED);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.addActionListener(e -> deleteSelected());

        rightToolbar.add(importFolderButton);
        rightToolbar.add(importImageButton);
        rightToolbar.add(deleteButton);
        rightToolbar.add(searchField);

        breadcrumb = new JLabel("Folders");
        breadcrumb.setForeground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(DARK);
        header.setBorder(new EmptyBorder(5,10,5,10));
        header.add(leftToolbar, BorderLayout.WEST);
        header.add(rightToolbar, BorderLayout.EAST);
        header.add(breadcrumb, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);

        // ------------------------ Table ------------------------
        model = new DefaultTableModel(new Object[]{"Name"},0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        styleTable(table);
        JScrollPane tableScroll = new JScrollPane(table);
        
        table.getSelectionModel().addListSelectionListener(e -> {
        if (!e.getValueIsAdjusting()) {
            int row = table.getSelectedRow();
            if (row >= 0 && showingFolders) {
                String selectedFolder = table.getValueAt(row, 0).toString();
                currentFaculty = selectedFolder;
            }
        }
        });

        // ------------------------ Image Viewer ------------------------
        imageViewer = new JLabel();
        imageViewer.setHorizontalAlignment(JLabel.CENTER);
        imageScrollPane = new JScrollPane(imageViewer);
        imageScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        imageScrollPane.getHorizontalScrollBar().setUnitIncrement(20);

        JPanel viewerPanel = new JPanel(new BorderLayout());
        viewerPanel.setBackground(Color.WHITE);
        viewerPanel.setBorder(new EmptyBorder(10,10,10,10));

        JPanel viewerTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewerTop.setOpaque(false);
        JButton closeViewer = new JButton("X");
        styleButton(closeViewer);
        closeViewer.setBackground(Color.RED);
        closeViewer.addActionListener(e -> {
            imageViewer.setIcon(null);
            currentImage = null;
            scale = 1.0;
            breadcrumb.setText(showingFolders ? "Folders" : "Folders > "+currentFaculty);
        });
        viewerTop.add(closeViewer);

        viewerPanel.add(viewerTop, BorderLayout.NORTH);
        viewerPanel.add(imageScrollPane, BorderLayout.CENTER);

        JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10,5));
        JButton zoomOut = new JButton("-");
        JButton resetZoom = new JButton("100%");
        JButton zoomIn = new JButton("+");
        styleButton(zoomOut);
        styleButton(resetZoom);
        styleButton(zoomIn);
        zoomPanel.add(zoomOut);
        zoomPanel.add(resetZoom);
        zoomPanel.add(zoomIn);
        viewerPanel.add(zoomPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, viewerPanel);
        splitPane.setDividerLocation(350);
        add(splitPane, BorderLayout.CENTER);

        // ------------------------ Table double-click ------------------------
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(evt.getClickCount() == 2){
                    int row = table.getSelectedRow();
                    if(row >= 0){
                        String fullName = table.getValueAt(row,0).toString();
                        if(showingFolders){
                            currentFaculty = fullName;
                            loadImagesInFaculty(currentFaculty);
                            backButton.setVisible(true);
                            breadcrumb.setText("Folders > " + currentFaculty);
                        } else {
                            String[] parts = fullName.split("/",2);
                            currentFaculty = parts[0];
                            currentImage = fullName; // folder/file
                            showImage(currentImage);
                            breadcrumb.setText("Folders > " + currentFaculty + " > " + parts[1]);
                        }
                    }
                }
            }
        });

        zoomIn.addActionListener(e -> { scale += 0.25; refreshImage(); });
        zoomOut.addActionListener(e -> { scale = Math.max(0.25, scale - 0.25); refreshImage(); });
        resetZoom.addActionListener(e -> { scale = 1.0; refreshImage(); });
    }

    // ------------------------ Styling ------------------------
    private void styleButton(JButton button){
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
    }

    private void styleTable(JTable table){
        table.setRowHeight(28);
        table.getTableHeader().setBackground(DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
    }

    // ------------------------ Folder / Image Methods ------------------------
    private void loadFolders(){
        model.setRowCount(0);
        showingFolders = true;
        currentFaculty = null;
        backButton.setVisible(false);
        breadcrumb.setText("Folders");
        List<String> folders = service.getFacultyFolders();
        for(String f : folders) model.addRow(new Object[]{f});
    }

    private void loadImagesInFaculty(String faculty){
        model.setRowCount(0);
        showingFolders = false;
        currentFaculty = faculty;
        List<String> images = service.getImagesInFaculty(faculty);
        for(String f : images) model.addRow(new Object[]{faculty + "/" + f});
    }

    private void filterImagesByFileName(String search){
        model.setRowCount(0);
        showingFolders = false;
        breadcrumb.setText("Search Results");

        List<String> folders = service.getFacultyFolders();
        for(String folder : folders){
            List<String> images = service.getImagesInFaculty(folder);
            for(String img : images){
                if(img.toLowerCase().contains(search.toLowerCase())){
                    model.addRow(new Object[]{folder + "/" + img});
                }
            }
        }
    }

    private void showImage(String filename){
        try{
            ImageIcon icon = new ImageIcon("inc_images/" + filename);
            Image img = icon.getImage();
            int width = (int)(img.getWidth(null) * scale);
            int height = (int)(img.getHeight(null) * scale);
            Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageViewer.setIcon(new ImageIcon(resized));
        }catch(Exception e){ e.printStackTrace(); }
    }

    private void refreshImage(){
        if(currentImage != null) showImage(currentImage);
    }

    // ------------------------ Import Folders ------------------------
    private void importFolders(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        int result = chooser.showOpenDialog(this);
        if(result != JFileChooser.APPROVE_OPTION) return;

        File[] folders = chooser.getSelectedFiles();
        for(File f : folders){
            File[] images = f.listFiles(file -> file.isFile() &&
                    (file.getName().toLowerCase().endsWith(".jpg") ||
                     file.getName().toLowerCase().endsWith(".png") ||
                     file.getName().toLowerCase().endsWith(".jpeg") ||
                     file.getName().toLowerCase().endsWith(".gif") ||
                     file.getName().toLowerCase().endsWith(".bmp")));
            if(images != null && images.length > 0){
                service.importImagesToFaculty(images, f.getName());
            }
        }
        loadFolders();
        JOptionPane.showMessageDialog(this,"Folder(s) imported successfully!");
    }

    // ------------------------ Import Images ------------------------
    private void importImages(){
        if(currentFaculty == null){
            JOptionPane.showMessageDialog(this,"Please select a faculty folder first!");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = chooser.showOpenDialog(this);
        if(result != JFileChooser.APPROVE_OPTION) return;

        File[] files = chooser.getSelectedFiles();
        service.importImagesToFaculty(files, currentFaculty);
        loadImagesInFaculty(currentFaculty);
        JOptionPane.showMessageDialog(this,"Images imported successfully!");
    }

    // ------------------------ Delete ------------------------
    private void deleteSelected(){
        int row = table.getSelectedRow();
        if(row < 0){
            JOptionPane.showMessageDialog(this,"Please select an item to delete!");
            return;
        }
        String name = table.getValueAt(row,0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,"Are you sure to delete \""+name+"\"?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if(confirm != JOptionPane.YES_OPTION) return;

        boolean success = false;
        if(showingFolders){
            File folder = new File("inc_images/" + name);
            success = deleteFolderRecursively(folder);
            if(success) loadFolders();
        } else {
            String[] parts = name.split("/",2);
            String folder = parts[0];
            String file = parts[1];
            success = service.deleteImageFromFaculty(folder, file);
            if(success) loadImagesInFaculty(folder);
        }

        if(success) JOptionPane.showMessageDialog(this,"Deleted successfully!");
        else JOptionPane.showMessageDialog(this,"Delete failed!");
    }

    private boolean deleteFolderRecursively(File folder){
        if(folder.isDirectory()){
            File[] files = folder.listFiles();
            if(files != null){
                for(File f : files){
                    deleteFolderRecursively(f);
                }
            }
        }
        return folder.delete();
    }

    // ------------------------ Main ------------------------
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new IncFormApp().setVisible(true));
    }
}