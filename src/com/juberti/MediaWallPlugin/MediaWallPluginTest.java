package com.juberti.MediaWallPlugin;

import static org.junit.Assert.*;

import java.awt.Color;
import java.net.URL;
import java.util.List;

import org.junit.Test;

public class MediaWallPluginTest {

	@Test
	public void testLoadImage() {
		MediaWallPlugin plugin = new MediaWallPlugin();
		List<ImageFrame> frames = null;
		try {
			frames = plugin.loadImage(new URL("http://goo.gl/vsjgLW"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(1, frames.size());
	}
	@Test
	public void testLoadAnimatedImage() {
		MediaWallPlugin plugin = new MediaWallPlugin();
		List<ImageFrame> frames = null;
		try {
			frames = plugin.loadImage(new URL("http://goo.gl/cO01h0"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(4, frames.size());
		for (ImageFrame frame : frames) {
			assertTrue(frame.getDelay() > 0);
		}
	}
	@Test
	public void testLoadAnimatedImageWithOffset() {
		MediaWallPlugin plugin = new MediaWallPlugin();
		List<ImageFrame> frames = null;
		try {
			frames = plugin.loadImage(new URL("http://goo.gl/3eUInA"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(7, frames.size());
		for (ImageFrame frame : frames) {
			assertTrue(frame.getDelay() > 0);
		}
	}
	@Test
	public void testMapPixelToWool() {
		MediaWallPlugin plugin = new MediaWallPlugin();
		assertEquals(14, plugin.mapColorToWool(new Color(192, 0, 0)));
		assertEquals(14, plugin.mapColorToWool(new Color(215, 26, 33)));
		assertEquals(15, plugin.mapColorToWool(new Color(17, 13, 14)));
		assertEquals(0, plugin.mapColorToWool(new Color(242, 242, 242)));
	}

}
