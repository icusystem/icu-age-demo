package com.itl;

import io.github.icusystem.icu_connect.Connect;
import io.github.icusystem.icu_connect.LocalAPIListener;
import io.github.icusystem.icu_connect.api_icu.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;



public class Main implements MjpegStream, LocalAPIListener {
    private Connect connect;

    /*  ICU connection parameters - set these to your ICU device settings  */
    private static final String streamURL = "http://192.168.137.8:8040/stream";
    private static final String icuIPAddress = "192.168.137.8";
    private static final boolean icuSSL = true;
    private static final String icuUserName = "apiuser";
    private static final String icuPassword = "apipassword";
    /*  --------------------------------------------------------------------- */
    private int thresholdAge = 25;

    /* UI objects for global access  */
    private JLabel labelInfo;
    private JLabel label;
    private JLabel labelResult;
    private JTable infoTable;
    private DefaultTableModel tableModel;
    private JSpinner ageSpinner;
    private ImageIcon imageIcon;
    private MJPEGClient streamClient;




    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            Main main = new Main();
            main.createAndShowGUI();
            main.startICU();

        });


    }


    /**
     * void function to initialise and start the ICU device
     */
    private void startICU(){

        /* Setup and start the ICU*/
        CameraSettings cameraSettings = new CameraSettings();
        cameraSettings.Rotation = 0;
        cameraSettings.Camera_distance = 2;
        cameraSettings.Spoof_level = 0;

        connect = new Connect(cameraSettings,icuIPAddress,icuSSL,icuUserName,icuPassword);
        /* Start the Connect, passing the event listener object */
        connect.Start("Main",this,false);
        /* Set the operation mode - AGE_ONLY in this example */
        connect.SetMode(ICURunMode.AGE_ONLY);

    }



    /**
     * void to create and format the UI and display it
     */
    private void createAndShowGUI() {


        //Create and set up the window.
        JFrame frame = new JFrame("ICU Age Threshold");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout(FlowLayout.LEFT,20,10));


        labelInfo = new JLabel("Connecting to ICU..",JLabel.LEFT);
        labelInfo.setPreferredSize(new Dimension(600,30));
        frame.getContentPane().add(labelInfo);

        label = new JLabel("Video",JLabel.CENTER);
        label.setPreferredSize(new Dimension(320,240));
        label.setBackground(Color.DARK_GRAY);
        label.setForeground(Color.WHITE);
        label.setOpaque(true);

        frame.getContentPane().add(label);


        labelResult = new JLabel("",JLabel.CENTER);
        labelResult.setOpaque(true);
        labelResult.setForeground(Color.WHITE);
        labelResult.setBackground(Color.WHITE);
        labelResult.setFont(new Font("Arial", Font.PLAIN, 48));
        labelResult.setPreferredSize(new Dimension(320,240));
        frame.getContentPane().add(labelResult);

        JPanel icuPanel = new JPanel();
        icuPanel.setPreferredSize(new Dimension(320,240));
        icuPanel.setBackground(Color.WHITE);
        icuPanel.setLayout(new BorderLayout());

        String[] columnName = {"Item","Value"};
        infoTable = new JTable();
        infoTable.setRowHeight(30);
        tableModel = new DefaultTableModel(columnName, 0);
        infoTable.setModel(tableModel);
        // Create a custom cell renderer to center-align the text in the second column (Name column)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Set text alignment to center
        infoTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Apply renderer to the second column
        icuPanel.add(infoTable);
        frame.getContentPane().add(icuPanel);

        JPanel ageThresholdPanel = new JPanel();
        ageThresholdPanel.setPreferredSize(new Dimension(320,240));
        ageThresholdPanel.setBackground(Color.WHITE);
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(thresholdAge,1,100,1);
        ageSpinner = new JSpinner(spinnerNumberModel);
        ageSpinner.setFont(new Font("Arial", Font.BOLD, 32)); // Set font
        ageSpinner.setBorder(new EmptyBorder(5, 10, 5, 10));
        ageSpinner.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Center the text in the editor component
        JComponent editor = ageSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor defaultEditor = (JSpinner.DefaultEditor) editor;
            JTextField textField = defaultEditor.getTextField();
            textField.setHorizontalAlignment(JTextField.CENTER);
            // Hide the caret
            textField.setCaret(new HiddenCaret());
        }
        JLabel ageThresholdLabel = new JLabel("Threshold Age: ");
        ageThresholdPanel.add(ageThresholdLabel);
        ageThresholdPanel.add(ageSpinner);
        frame.getContentPane().add(ageThresholdPanel);


        frame.setSize(900,600);

        // the image for the stream
        imageIcon = new ImageIcon();
        //Display the window.
        frame.setVisible(true);

    }


    class HiddenCaret extends DefaultCaret {
        @Override
        public void paint(Graphics g) {
            // Override paint method to do nothing (hide caret)
        }
    }


    /**
     *
     * Stream Listener events
     */
    @Override
    public void NewFrame(BufferedImage image) {

        SwingUtilities.invokeLater(() ->{
            imageIcon.setImage(image);
            label.setIcon(imageIcon);
            label.repaint();
        });

    }

    /**
    * ICU listener events
    */
    @Override
    public void ICUConnected(ICUDevice icuDevice){
        System.out.println("connected " + icuDevice.deviceDetail.DeviceName);

        SwingUtilities.invokeLater(()->{
            labelInfo.setText(icuDevice.deviceDetail.DeviceName + " connected.");
            Camera cam = icuDevice.deviceSettings.Cameras.get(0);

            tableModel.addRow(new Object[]{"ICU name",icuDevice.deviceDetail.DeviceName});
            tableModel.addRow(new Object[]{"ICU ID",icuDevice.deviceDetail.DeviceId});
            tableModel.addRow(new Object[]{"ICU Type",icuDevice.deviceDetail.DeviceType});
            tableModel.addRow(new Object[]{"Build version",icuDevice.deviceDetail.SWBuildVersion});
            tableModel.addRow(new Object[]{"Camera",cam.Name});
            tableModel.addRow(new Object[]{"Resolution",cam.Resolution});
            tableModel.addRow(new Object[]{"AAE",String.valueOf(!cam.Face_rec_en)});
            tableModel.addRow(new Object[]{"Spoof detection",String.valueOf(cam.Spoof_level > 0)});
            tableModel.addRow(new Object[]{"View mode",cam.View_mode});

            infoTable.repaint();

        });

    }

    /**
     * @param message
     */
    @Override
    public void ICUDisconnected(String message){
        System.out.println("dis-connected " + message);

        if(streamClient != null){
            streamClient.stopStream();
        }
        SwingUtilities.invokeLater(()->{
            labelInfo.setText(message);
        });




}
    @Override
    public void ICUReady(){

        /* we do not want to display the result box on the stream */
        connect.SetMode(ICURunMode.STREAM_FACE_BOX_OFF);


        /* Create a new steam client for the ICU camera view */
        streamClient = new MJPEGClient(streamURL,this);

        SwingUtilities.invokeLater(()->{
            labelInfo.setText("");
        });


    }


    @Override
    public void ICUInitialising(){
        System.out.println("initialising...");
        SwingUtilities.invokeLater(()->{
            labelInfo.setText("ICU initialising...");
        });

    }


    @Override
    public void ICUAge(FaceSessionData data){

        SwingUtilities.invokeLater(()->{

            if(data.age < (Integer)ageSpinner.getValue()) {
                labelResult.setBackground(new Color(180,0,0));
                labelResult.setText("FAIL");
            }else{
                labelResult.setBackground(new Color(0,180,0));
                labelResult.setText("PASS");
            }
        });

    }

    @Override
    public void ICULiveFrame(List<FaceDetect> frameCounts){

        if(frameCounts.get(0).FaceCount == 0) {
            // clear result when no faces
            SwingUtilities.invokeLater(() -> {
                labelResult.setText("");
                labelResult.setBackground(Color.WHITE);
            });
        }

        // show any reason available for face rejected
        if(!frameCounts.get(0).FaceReject.equals("")) {
            SwingUtilities.invokeLater(()->{
               labelInfo.setText("*" + frameCounts.get(0).FaceReject);
            });
        }else{
            SwingUtilities.invokeLater(()->{
                labelInfo.setText("");
            });
        }

    }





}
