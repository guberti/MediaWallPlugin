package com.juberti.MediaWallPlugin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class MediaWallPlugin extends JavaPlugin implements Listener {
	class Wool {
		Wool(int inData, int r, int g, int b) {
			data = inData;
			color = new Color(r, g, b);					
		}
		public int getData() { return data; }
		public Color getColor() {
			return color;
		}
		int data;
		Color color;
	};
	final Wool[] woolTypes = {
		new Wool(0, 221, 221, 221),
		new Wool(1, 219, 125, 62),
		new Wool(2, 179, 80, 188),
		new Wool(3, 107, 138, 201),
		new Wool(4, 177, 166, 39),
		new Wool(5, 65, 174, 56),
		new Wool(6, 208, 132, 153),
		new Wool(7, 64, 64, 64),
		new Wool(8, 154, 161, 161),
		new Wool(9, 46, 110, 137),
		new Wool(10, 126, 61, 181),
		new Wool(11, 46, 56, 141),
		new Wool(12, 79, 50, 31),
		new Wool(13, 53, 70, 27),
		new Wool(14, 150, 52, 48),
		new Wool(15, 25, 22, 22)
	};
	
	@Override
	public void onEnable(){
		getLogger().info("onEnable has been invoked!");
		getServer().getPluginManager().registerEvents(this, this);
	}
	@Override
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");		
	}
	void maybeBuildImage(final Location location, final String text) {
		if (text.contains("goo.gl")) {
			try {
				URL url;
				url = new URL("http://" + text);
	            asyncBuildImage(location, url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		if (text.contains("statue")) {
			buildStatue("Gavin_U", location);
		}
	}
	void asyncBuildImage(final Location location, final URL url) {
		Thread t = new Thread(new Runnable() {
		    @Override
			public void run() {
		    	buildImage(location, url);		        		    
		    }
		});
		t.start();
	}
    void buildImage(Location location, URL url) {
      final int MAX_HEIGHT = 32;
      final int MAX_PLAYS = 100;
      try {
			List<ImageFrame> frames = loadImage(url);			
			getLogger().info("Image loaded: " + url);
			resizeImage(frames, MAX_HEIGHT);
			if (frames.size() == 1) {
			    buildImage(location, frames);
			} else {
				for (int i = 0; i < MAX_PLAYS; ++i) { 
				    for (int j = 0; j < frames.size(); ++j) {
					    buildImage(location, frames.get(j));
					    try {
						    Thread.sleep(frames.get(j).getDelay());
					    } catch (InterruptedException e) {						
					    }
				    }
				}
			}
		} catch (IOException e) {
			getLogger().info("Image failed to load: " + url);
		}	
    }
    List<ImageFrame> loadImage(URL url) throws IOException {
        InputStream stream = url.openStream();
        ImageInputStream imageStream = ImageIO.createImageInputStream(stream);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
        if (!readers.hasNext())
            throw new IOException("Unknown image format");
        ImageReader reader = readers.next();
        reader.setInput(imageStream);
        
        List<ImageFrame> frames = new ArrayList<ImageFrame>();
        int n = reader.getNumImages(true);
        int firstW = 0, firstH = 0;
        //IIOMetadata metadata = reader.getStreamMetadata();
        System.out.println("Image contains " + n + " frames");
        for (int i = 0; i < n; i++) {
            BufferedImage image = reader.read(i);
            int delay = 0, w = image.getWidth(), h = image.getHeight();
            if (i == 0) {
            	firstW = w;
            	firstH = h;
            }
            //System.out.println(reader.getFormatName());
            if (reader.getFormatName().equals("gif")) {
                IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
                delay = Integer.valueOf(gce.getAttribute("delayTime")) * 10;
                System.out.println("Image " + i + ": delay=" + delay);
                NodeList children = root.getChildNodes();
                for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
              		Node nodeItem = children.item(nodeIndex);

              		if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                  		NamedNodeMap map = nodeItem.getAttributes();
                  		int x, y;
                  		x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                  	    y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                  	    System.out.println("W: " + w + " H: " + h + " X: " + x + ", Y: " + y);
                  	    if (i != 0 && x != 0 && y != 0) {
                  	        BufferedImage newImage = new BufferedImage(firstW, firstH, BufferedImage.TYPE_INT_RGB);
                	        Graphics2D g = (Graphics2D)newImage.getGraphics();
                	        g.drawImage(frames.get(i - 1).getImage(), 0, 0, firstW, firstH, null);
                	        g.drawImage(image, x, y, w, h, null);
                	        image = newImage;
                  	    }
              		}
                }
            }
            
            frames.add(new ImageFrame(image, delay, null));          
        }
        stream.close();  
        return frames;
    }
    void resizeImage(List<ImageFrame> frames, int height) {
      for (int i = 0; i < frames.size(); ++i) {
    	  BufferedImage image = frames.get(i).getImage();
		  int w = image.getWidth(), h = image.getHeight();
		  int newW = (int)((float)w * height / h), newH = height;			
		  BufferedImage newImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
	      Graphics2D g = (Graphics2D)newImage.getGraphics();
	      g.drawImage(image, 0, 0, newW, newH, null);
	      frames.get(i).setImage(newImage);
      }
    }
    void buildImage(Location location, List<ImageFrame> frames) {
    	buildImage(location, frames.get(0));
    }
    void buildImage(Location location, ImageFrame frame) {
	    BufferedImage image = frame.getImage();
    	for (int i = 0; i < image.getWidth(); ++i) {
	    	for (int j = 0; j < image.getHeight(); ++j) {
	    		byte t = getWoolType(image, i, j);
	    		Location blockLoc = location.clone();
	    		blockLoc.add(i + 1, image.getHeight() - j, 0);
	    		blockLoc.getBlock().setTypeIdAndData(
	    		    Material.WOOL.getId(), t, false);
	    	}
	    }
    }
    byte getWoolType(BufferedImage image, int x, int y) {
    	int pixel = image.getRGB(x, y);
    	return mapPixelToWool(pixel);
    }   
    byte mapPixelToWool(int pixel) {
    	Color color = new Color(pixel);
    	return mapColorToWool(color);
    }
    // TODO: figure out @Visible annotation
    public byte mapColorToWool(Color color) {
    	Wool bestWool = null;
    	float bestDist = 999;
    	for (Wool wool : woolTypes) {
    		float dist = getDistance(color, wool.getColor());
    		if (dist < bestDist) {
    			bestDist = dist;
    			bestWool = wool;
    		}
    	}
    	return (byte)bestWool.getData();
    }
    static float getDistance(Color color1, Color color2) {
    	float d0 = (float)((color1.getRed() - color2.getRed()) / 255.0); 
    	float d1 = (float)((color1.getGreen() - color2.getGreen()) / 255.0);
    	float d2 = (float)((color1.getBlue() - color2.getBlue()) / 255.0);
    	return d0 * d0 + d1 * d1 + d2 * d2;
    }
    void burnUpWool(Location bottomLeftWool) {
	    Location loc = bottomLeftWool.clone();
	    while (loc.getBlock().getType() == Material.WOOL) {
		    while (loc.getBlock().getType() == Material.WOOL) {
		    	loc.getBlock().setType(Material.AIR);
		    	loc.add(0, 1, 0);
		    }
            loc.setY(bottomLeftWool.getY());
            loc.add(1, 0, 0);
	    }
    }
    void buildStatue(String playerName, Location Signlocation) {
    	buildPrism(Signlocation.add(0, 0, 0), 8, 11, 4);
    	buildPrism(Signlocation.add(-4, 11, 0), 16, 12, 4);
    	buildPrism(Signlocation.add(4, 12, -2), 8, 8, 8);
    }
    void buildPrism(Location loc, int length, int height, int width) {
    	for(int x = 0; x < length; x++) {
        	for(int y = 0; y < height; y++) {
            	for(int z = 0; z < width; z++) {
    				Location here = loc.clone();
    				here.add(x, y, z);
    				here.getBlock().setType(Material.STONE);
    			}
    		}
    	}
    		
    }
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
	    maybeBuildImage(event.getBlock().getLocation(), event.getLine(0));
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.WALL_SIGN) {
		    Sign sign = (Sign)event.getBlock().getState();
			String text = (sign.getLine(1));
			if (text.contains("burn")) {				
			    Location bottomLeft = sign.getLocation();
			    bottomLeft.add(1, 0, 0);
			    burnUpWool(bottomLeft);
		    }
		}
	}
	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event) {
		if (event.getBlock().getType() == Material.WALL_SIGN) {
            maybeBuildImage(event.getBlock().getLocation(), 
            ((Sign)event.getBlock().getState()).getLine(0));
		}
	}
}