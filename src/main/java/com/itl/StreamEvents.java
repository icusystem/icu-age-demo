package com.itl;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class StreamEvents implements MjpegStream{

    private JLabel imageLable;

    public StreamEvents(JLabel label){
        this.imageLable = label;
    }

    @Override
    public void NewFrame(BufferedImage image) {

        SwingUtilities.invokeLater(() ->{
            imageLable.setIcon(new ImageIcon(image));
            imageLable.repaint();
        });




    }
}
