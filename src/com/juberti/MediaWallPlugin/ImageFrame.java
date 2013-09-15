package com.juberti.MediaWallPlugin;

import java.awt.image.BufferedImage;

public class ImageFrame {
	private BufferedImage image;
    private final int delay;
    private final String disposal;

    public ImageFrame (BufferedImage image, int delay, String disposal) {
        this.image = image;
        this.delay = delay;
        this.disposal = disposal;
    }

    public ImageFrame(BufferedImage image) {
        this.image = image;
        this.delay = -1;
        this.disposal = null;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getDelay() {
        return delay;
    }

    public String getDisposal() {
        return disposal;
    }
    
    public void setImage(BufferedImage newImage) {
    	image = newImage;
    }
    
}