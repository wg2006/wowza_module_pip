package uk.org.shamrock.demo.transcoder.mosaic.overlays;

import java.awt.image.BufferedImage;
import com.wowza.demo.transcoder.mosaic.Logger;

public class OverlayImages {
	
	private byte[] overlayImage = null;
	private Logger Log = null;
	
	public OverlayImages(Logger log)
		{
		this.Log = log;
		}
	
	public void updateOverlayImage(BufferedImage image)
		{
	    int imgWidth = image.getWidth();
		int imgHeight = image.getHeight();			
		this.overlayImage = new byte[imgWidth*imgHeight*4];
		int imgLoc = 0;
		for(int y=0;y<imgHeight;y++)
			{
			for(int x=0;x<imgWidth;x++)
				{
				int argb = image.getRGB(x, y);
					
				this.overlayImage[(imgLoc*4)+0] = (byte)((argb>>0)&0x0FF);
				this.overlayImage[(imgLoc*4)+1] = (byte)((argb>>8)&0x0FF);
				this.overlayImage[(imgLoc*4)+2] = (byte)((argb>>16)&0x0FF);
				this.overlayImage[(imgLoc*4)+3] = (byte)((argb>>24)&0x0FF);
				imgLoc++;
				}
			}
		}
	
	public byte[] returnOverlayImage()
		{
		return this.overlayImage;
		}

}
