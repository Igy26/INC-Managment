package app.ui;

import app.service.ImageService;
import app.util.Constants;

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
    String currentFolder = null;
    String currentImage = null;

    ImageService service = new ImageService();

    Color PRIMARY = new Color(52,152,219);
    Color DARK = new Color(44,62,80);
    Color LIGHT = new Color(245,246,250);

    boolean showingFolders = true;

    JScrollPane imageScrollPane;

    public IncFormApp() {
        setTitle("INC Form Image Manager");
        setSize(1200,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(LIGHT);

        initUI();
        loadFolders();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ------------------------ Top toolbar ------------------------
        backButton = new JButton("← Back");
        styleButton(backButton);
        backButton.setVisible(false);
        backButton.addActionListener(e -> loadFolders());

        JPanel leftToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftToolbar.setOpaque(false);
        leftToolbar.add(backButton);

        JPanel rightToolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        rightToolbar.setOpaque(false);

        // Search field
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200,28));
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                if(showingFolders){
                    filterFolders(searchField.getText());
                } else {
                    filterImages(searchField.getText());
                }
            }
        });

        // Create Folder
        JButton createFolderButton = new JButton("Create Folder");
        styleButton(createFolderButton);
        createFolderButton.addActionListener(e -> {
            String folderName = JOptionPane.showInputDialog("Enter new professor folder name:");
            if(folderName != null && !folderName.trim().isEmpty()){
                if(service.createFolder(folderName)){
                    JOptionPane.showMessageDialog(this, "Folder created successfully!");
                    loadFolders();
                } else {
                    JOptionPane.showMessageDialog(this, "Folder already exists!");
                }
            }
        });

        // Import Image
        JButton importButton = new JButton("Import Image");
        styleButton(importButton);
        importButton.addActionListener(e -> importImages());

        // Delete Button (Red/Danger)
        JButton deleteButton = new JButton("Delete");
        styleButton(deleteButton);              
        deleteButton.setBackground(Color.RED); 
        deleteButton.setForeground(Color.WHITE); 
        deleteButton.addActionListener(e -> deleteSelected());

        // Add buttons to toolbar
        rightToolbar.add(createFolderButton);
        rightToolbar.add(importButton);
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
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent editing
            }
        };
        table = new JTable(model);
        styleTable(table);
        JScrollPane tableScroll = new JScrollPane(table);

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
            breadcrumb.setText(showingFolders ? "Folders" : "Folders > "+currentFolder);
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

        // ------------------------ Listeners ------------------------
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(evt.getClickCount() == 2){ // double click
                    int row = table.getSelectedRow();
                    if(row >= 0){
                        String name = table.getValueAt(row,0).toString();
                        if(showingFolders){
                            currentFolder = name;
                            loadImagesInFolder(currentFolder);
                            backButton.setVisible(true);
                            breadcrumb.setText("Folders > " + currentFolder);
                        } else {
                            currentImage = currentFolder + "/" + name;
                            showImage(currentImage);
                            breadcrumb.setText("Folders > " + currentFolder + " > " + name);
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

    // ------------------------ Folder methods ------------------------
    private void loadFolders(){
        model.setRowCount(0);
        showingFolders = true;
        currentFolder = null;
        backButton.setVisible(false);
        breadcrumb.setText("Folders");

        List<String> folders = service.getFolders();
        for(String f : folders) model.addRow(new Object[]{f});
    }

    private void filterFolders(String search){
        model.setRowCount(0);
        List<String> folders = service.getFolders();
        for(String f : folders){
            if(f.toLowerCase().contains(search.toLowerCase())) model.addRow(new Object[]{f});
        }
    }

    // ------------------------ Images methods ------------------------
    private void loadImagesInFolder(String folder){
        model.setRowCount(0);
        showingFolders = false;
        List<String> images = service.getImagesInFolder(folder);
        for(String f : images) model.addRow(new Object[]{f});
    }

    private void filterImages(String search){
        model.setRowCount(0);
        List<String> images = service.getImagesInFolder(currentFolder);
        for(String f : images){
            if(f.toLowerCase().contains(search.toLowerCase())) model.addRow(new Object[]{f});
        }
    }

    private void showImage(String filename){
        try{
            ImageIcon icon = new ImageIcon(Constants.IMAGE_PATH + filename);
            Image img = icon.getImage();
            int width = (int)(img.getWidth(null) * scale);
            int height = (int)(img.getHeight(null) * scale);
            Image resized = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            imageViewer.setIcon(new ImageIcon(resized));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void refreshImage(){
        if(currentImage != null) showImage(currentImage);
    }

    // ------------------------ Import Images ------------------------
    private void importImages(){
        List<String> folders = service.getFolders();
        if(folders.isEmpty()){
            JOptionPane.showMessageDialog(this,"No folders available. Please create a folder first.");
            return;
        }

        String chosenFolder = showSearchableFolderDialog(folders.toArray(new String[0]));
        if(chosenFolder == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) return true;
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") 
                        || name.endsWith(".gif") || name.endsWith(".bmp");
            }
            @Override
            public String getDescription() {
                return "Image Files (*.jpg, *.jpeg, *.png, *.gif, *.bmp)";
            }
        });

        int result = chooser.showOpenDialog(this);
        if(result != JFileChooser.APPROVE_OPTION) return;

        File[] files = chooser.getSelectedFiles();
        java.util.List<File> imageFiles = new java.util.ArrayList<>();
        for(File f : files){
            String name = f.getName().toLowerCase();
            if(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") 
               || name.endsWith(".gif") || name.endsWith(".bmp")){
                imageFiles.add(f);
            }
        }

        if(imageFiles.isEmpty()){
            JOptionPane.showMessageDialog(this, "No valid image files selected!");
            return;
        }

        service.importImagesToFolder(imageFiles.toArray(new File[0]), chosenFolder);
        loadFolders();

        JOptionPane.showMessageDialog(
                this,
                imageFiles.size() + " image" + (imageFiles.size() > 1 ? "s" : "") + " added successfully to \"" + chosenFolder + "\"!",
                "Import Successful",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ------------------------ Delete ------------------------
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.");
            return;
        }

        String name = table.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete \"" + name + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean success = false;
        if (showingFolders) {
            success = service.deleteFolder(name);
            if (success) loadFolders();
        } else {
            success = service.deleteImage(currentFolder, name);
            if (success) loadImagesInFolder(currentFolder);
        }

        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    (showingFolders ? "Folder" : "Image") + " \"" + name + "\" deleted successfully!"
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to delete " + (showingFolders ? "folder" : "image") + " \"" + name + "\"!"
            );
        }
    }

    // ------------------------ Searchable Folder Dialog ------------------------
    private String showSearchableFolderDialog(String[] options){
        JTextField searchField = new JTextField(20);
        JList<String> list = new JList<>(options);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(8);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter(){
                String text = searchField.getText().toLowerCase();
                java.util.List<String> filtered = new java.util.ArrayList<>();
                for(String s : options) if(s.toLowerCase().contains(text)) filtered.add(s);
                list.setListData(filtered.toArray(new String[0]));
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        JPanel panel = new JPanel(new BorderLayout(5,5));
        panel.add(new JLabel("Select Folder:"), BorderLayout.NORTH);
        panel.add(searchField, BorderLayout.CENTER);
        panel.add(scrollPane, BorderLayout.SOUTH);

        int result = JOptionPane.showConfirmDialog(this, panel, "Choose Folder", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if(result == JOptionPane.OK_OPTION) return list.getSelectedValue();
        return null;
    }

    // ------------------------ Main ------------------------
    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new IncFormApp().setVisible(true));
    }
}