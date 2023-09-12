package com.itl;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

public class MJPEGClient extends JFrame {
    private JLabel videoLabel;
    private BufferedImage currentImage;
    private static String frameBoundary = "--jpgboundary\r\n";

    public MJPEGClient(String streamURL) {
        setTitle("MJPEG Client");
        setSize(1200, 960);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        videoLabel = new JLabel();
        add(videoLabel);

        setVisible(true); // Make the frame visible

        // Run the stream fetching logic in a background thread
        Thread streamThread = new Thread(() -> fetchAndDisplayMJPEGStream(streamURL));
        streamThread.start();
    }

    private void fetchAndDisplayMJPEGStream(String streamURL) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(streamURL);

            int buffer_len = 2048;

            while (true) {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    byte[] buffer = new byte[buffer_len];

                    while(true){
                        // look for boundary
                        int bufferRead = inputStream.read(buffer,0,buffer.length);
                        ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();
                        frameBuffer.write(buffer, 0, buffer_len);
                        byte[] frameData = frameBuffer.toByteArray();

                        int markerIndex = indexOf(frameData, frameBoundary);
                        if(markerIndex != -1) {
                            String header = new String(frameData);
                            String[] lines = header.split("\r\n");
                            String contentType = null;
                            int contentLength = -1;
                            int headerMarker = markerIndex + frameBoundary.length();
                            for (String line : lines) {
                                if (line.startsWith("Content-type:")) {
                                    contentType = line.substring("Content-type:".length()).trim();
                                    headerMarker += line.length() + "\r\n".length();
                                } else if (line.startsWith("Content-length:")) {
                                    contentLength = Integer.parseInt(line.substring("Content-length:".length()).trim());
                                    headerMarker += line.length() + "\r\n\r\n".length();
                                }
                            }

                            if(contentType != null){

                                byte[] jpegData = new byte[contentLength];
                                // copy initial data
                                System.arraycopy(buffer,headerMarker,jpegData,0,buffer_len - headerMarker);

                                int bytesToRead = contentLength - buffer_len - headerMarker;
                                bytesToRead += 132;

                                int destPtr = buffer_len - headerMarker;
                                while(bytesToRead > 0){
                                    int bytes_read;
                                    if(bytesToRead >= buffer_len){
                                        bytes_read = inputStream.read(buffer, 0, buffer_len);
                                    }else{
                                        bytes_read = inputStream.read(buffer, 0, bytesToRead);

                                    }
                                    System.arraycopy(buffer,0,jpegData,destPtr,bytes_read);
                                    bytesToRead -= bytes_read;
                                    destPtr += bytes_read;

                                }

                                try {
                                    // Update the current image
                                    currentImage = ImageIO.read(new ByteArrayInputStream(jpegData));
                                    // Repaint the label
                                    SwingUtilities.invokeLater(this::repaintLabel);
                                    frameBuffer.reset();
                                }catch (Exception ex){
                                    System.out.println(ex.getMessage());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void repaintLabel() {
        if (currentImage != null) {
            videoLabel.setIcon(new ImageIcon(currentImage));
            videoLabel.repaint();
            revalidate();
        }
    }

    // Custom indexOf method for byte arrays
    private int indexOf(byte[] array, String target) {
        return new String(array).indexOf(target);
    }

}
