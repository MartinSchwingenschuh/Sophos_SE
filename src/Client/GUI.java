package Client;

import java.awt.BorderLayout;
import java.io.File;
import java.math.BigInteger;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;


public class GUI {
    
    private Client client;
    private Dimension screenSize;
    private Dimension windowSize;


    DefaultListModel<File> model;

    //for pdf viewer
    SwingController controller;


    GUI(Client client){

        this.client = client;

        //get the screen size and calculate the window size
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        windowSize = new Dimension((int) screenSize.getWidth()/2, (int) screenSize.getHeight()/2);

        //create a frame
        JFrame frame = new JFrame("Project-Sophos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(windowSize);

        /***************************************************************************
         * MENU BAR
        ***************************************************************************/
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileUpload = new JMenuItem("upload file");
        fileMenu.add(fileUpload);
        JMenu searchMenu = new JMenu("Search");

        menuBar.add(fileMenu);
        menuBar.add(searchMenu);
        
        /***************************************************************************
         * RESULT PANEL
        ***************************************************************************/
        
        model = new DefaultListModel<File>();
        
        //result panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setSize(new Dimension((int) windowSize.getWidth()/2, (int) windowSize.getHeight()));
        // resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

        JList<File> fileList = new JList<File>(model);
        fileList.setCellRenderer(new FileRenderer(true));
        fileList.setLayoutOrientation(javax.swing.JList.VERTICAL);

        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setPreferredSize(new Dimension((int) windowSize.getWidth()/2, (int) windowSize.getHeight()));
        
        resultPanel.add(BorderLayout.WEST, scrollPane);

        fileList.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
               System.out.println("element " + fileList.getSelectedValue().getName() + "clicked");
                File selectedFile = fileList.getSelectedValue();
                System.out.println(selectedFile);
                try {
                    // solution to open file in a seperate window
                    // Desktop desktop = Desktop.getDesktop();
                    // if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
                    //     desktop.open(selectedFile);
                    // } else {
                    //     System.err.println("PDF-Datei kann nicht angezeigt werden!");
                    // }
                    controller.openDocument(selectedFile.getAbsolutePath()); 
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}            
        });

        //add the initial files stored on server
        try {
            List<BigInteger> indices = null;
            indices = this.client.serverStub.getAllFileIndices();
            for (BigInteger index : indices) {
                model.addElement(client.downloadFile(index));
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }     
        
        /***************************************************************************
         * PDF VIEWER PANEL
        ***************************************************************************/
        JPanel pdfPanel = new JPanel(new BorderLayout());

        try{
            BufferedImage myPicture = ImageIO.read(new File("./src/Client/Resources/noPDF.png"));
            JLabel picLabel = new JLabel(new ImageIcon(myPicture));
            pdfPanel.add(BorderLayout.CENTER, picLabel);
        }catch(Exception e){
            e.printStackTrace();
        }

        controller = new SwingController(); 
        SwingViewBuilder builder = new SwingViewBuilder(controller);
        JPanel viewerPanel = builder.buildViewerPanel();
        viewerPanel.setPreferredSize(
            new Dimension(
                (int)windowSize.getWidth()/2,
                (int)windowSize.getHeight())
        );
        
        /***************************************************************************
         * SOUTH/INPUT PANEL
        ***************************************************************************/
        JPanel southPanel = new JPanel();

        //upload button
        JButton uploadButton = new JButton("UPLOAD");
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //create a new file chooser and set start dir to project dir
                final JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("./"));
                int fileSuc = fileChooser.showOpenDialog(uploadButton);

                //handle the selected file
                if (fileSuc == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    System.out.println("[INFO] uploading file: " + file); 
                    client.addDocument(file.toPath());
                } else {
                    System.out.println("[ERROR] error in filechooser");
                }

            }
        });

        southPanel.add(BorderLayout.EAST, uploadButton);

        JTextField searchText = new JTextField();
        searchText.setPreferredSize(new Dimension(
            200,
            20
        ));

        southPanel.add(BorderLayout.WEST, searchText);


        JButton searchButton = new JButton("SEARCH");
        searchButton.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                String keyWord = searchText.getText();
                if(keyWord != null){
                    System.out.println("[INFO] searching for keyword: "+keyWord);
                    List<File> retVal = client.search(keyWord);

                    model.clear();

                    if(retVal != null){
                        model.addAll(retVal);
                    }else{
                        System.out.println("[ERROR] searchword not found");
                    }                
                }
            }
        });

        southPanel.add(BorderLayout.CENTER, searchButton);


        //add components to frame
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);
        frame.getContentPane().add(BorderLayout.CENTER, resultPanel);
        frame.getContentPane().add(BorderLayout.EAST, viewerPanel); 
        resultPanel.add(BorderLayout.SOUTH, southPanel);

        //set frame to visible
        frame.setVisible(true);
    }
}

class FileRenderer extends DefaultListCellRenderer {

    private boolean pad;
    private Border padBorder = new EmptyBorder(3,3,3,3);

    FileRenderer(boolean pad) {
        this.pad = pad;
    }

    @Override
    public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
        JLabel l = (JLabel)c;
        File f = (File)value;
        l.setText(f.getName());
        l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));
        if (pad) {
            l.setBorder(padBorder);
        }

        return l;
    }
}