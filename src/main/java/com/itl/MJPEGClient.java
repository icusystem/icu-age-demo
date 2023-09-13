package com.itl;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;

public class MJPEGClient {

    private BufferedImage currentImage;
    private final String streamURL;
    private final MjpegStream mjpegStreamListener;
    private boolean _run = false;

    /**
     * @param streamURL
     * @param streamEvents
     */
    public MJPEGClient(String streamURL, Main streamEvents) {
        this.streamURL = streamURL;
        this.mjpegStreamListener = streamEvents;
        // Run the stream fetching logic in a background thread
        Thread streamThread = new Thread(() -> fetchAndDisplayMJPEGStream(streamURL));
        streamThread.start();
    }

    /**
     * @param streamURL
     */
    private void fetchAndDisplayMJPEGStream(String streamURL) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(streamURL);

            int buffer_len = 1024;
            _run = true;
            while (_run) {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    byte[] buffer = new byte[buffer_len];

                    while(_run){
                        int bufferRead = inputStream.read(buffer,0,buffer.length);
                        if(bufferRead == buffer_len) {
                            ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();
                            frameBuffer.write(buffer, 0, buffer_len);
                            byte[] frameData = frameBuffer.toByteArray();

                            /* look for the boundary marker */
                            String frameBoundary = "--jpgboundary\r\n";
                            int markerIndex = indexOf(frameData, frameBoundary);
                            if (markerIndex != -1) {
                                /* marker found, parse for header info */
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

                                if (contentType != null && contentType.equals("image/jpeg")) {
                                    /* headers found, read jpeg binary data */
                                    byte[] jpegData = new byte[contentLength];
                                    // copy initial data
                                    System.arraycopy(buffer, headerMarker, jpegData, 0, buffer_len - headerMarker);
                                    // Total bytes which make up this frame
                                    int bytesToRead = contentLength - buffer_len + headerMarker;

                                    int destPtr = buffer_len - headerMarker;
                                    while (bytesToRead > 0) {
                                        int bytes_read;
                                        if (bytesToRead >= buffer_len) {
                                            bytes_read = inputStream.read(buffer, 0, buffer_len);
                                        } else {
                                            bytes_read = inputStream.read(buffer, 0, bytesToRead);

                                        }
                                        System.arraycopy(buffer, 0, jpegData, destPtr, bytes_read);
                                        bytesToRead -= bytes_read;
                                        destPtr += bytes_read;
                                    }

                                    try {
                                        // Update the current image
                                        currentImage = ImageIO.read(new ByteArrayInputStream(jpegData));
                                        // dispatch image to listener callback
                                        if (currentImage != null) {
                                            mjpegStreamListener.NewFrame(resize(currentImage, 320,240));
                                        }
                                        frameBuffer.reset();
                                    } catch (Exception ex) {
                                        System.out.println(ex.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Stream stopped");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param array
     * @param target
     * @return
     */
    // Custom indexOf method for byte arrays
    private int indexOf(byte[] array, String target) {
        return new String(array).indexOf(target);
    }


    public void stopStream(){
        _run = false;
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }


}
