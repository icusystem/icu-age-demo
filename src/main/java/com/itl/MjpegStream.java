package com.itl;

import java.awt.image.BufferedImage;

/**
 * An interface to define callback events for MJPEGClient
 */
public interface MjpegStream {

    /**
     * @param image The BufferedImage object for the latest frame
     */
    void NewFrame(BufferedImage image);

}
