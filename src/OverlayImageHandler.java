package uk.org.shamrock.demo.transcoder.mosaic.overlays;

import java.awt.image.BufferedImage;
import com.wowza.demo.transcoder.mosaic.Logger;
import com.wowza.wms.transcoder.model.TranscoderVideoOverlayFrame;

public class OverlayImageHandler {
	
	// At 30 frames per second and a 10 second buffer
	private int imageCount = 50*10;
	// Number of grids to use ( this is fixed at the moment)
	private int GridCount = 4;
	private TranscoderVideoOverlayFrame[][] ActualOverlays = null;
	private OverlayImages[][] Overlays = null;
	private int[] storeImageCurrentCount = null;
	private int[] transImageCurrentCount = null;
	private boolean[] hasActiveImages = null;
	private String[] gridPosNames = null;
	private Logger Log = null;
	
	public OverlayImageHandler (Logger log )
		{
		this.Log = log;
		this.ActualOverlays = new TranscoderVideoOverlayFrame[this.GridCount][this.imageCount];
		this.Overlays = new OverlayImages[this.GridCount][this.imageCount];
		this.storeImageCurrentCount = new int[this.GridCount];
		this.transImageCurrentCount = new int[this.GridCount];
		this.hasActiveImages = new boolean[this.GridCount];
		this.gridPosNames = new String[this.GridCount];
		
		for ( int init =0;init<this.GridCount; init ++ )
			{
			this.hasActiveImages[init]=false;
			this.storeImageCurrentCount[init] = 0;
			this.transImageCurrentCount[init] = 0;
			this.gridPosNames[init] = "";
			}
		}
	
	public void updatestoreImageCount(int gridPos)
		{
		synchronized (this.storeImageCurrentCount)
			{
			this.storeImageCurrentCount[gridPos]++;
			if ( this.storeImageCurrentCount[gridPos] >= this.imageCount )
				{
				this.storeImageCurrentCount[gridPos] = 0;
				}
			}
		} 
	
	public int returnstoreImageCount(int gridPos)
		{
		return this.storeImageCurrentCount[gridPos];
		}
	
	public void updatetransImageCount(int gridPos)
		{
		synchronized (this.transImageCurrentCount)
			{
			this.transImageCurrentCount[gridPos]++;
			if ( this.transImageCurrentCount[gridPos] >= this.imageCount )
				{
				this.transImageCurrentCount[gridPos] =0;
				}
			}
		}

	public int returntransImageCount(int gridPos)
		{
		return this.transImageCurrentCount[gridPos];
		}
	
	public void updateOverlayImage(BufferedImage image, int gridPos)
		{
		int updatePos = returnstoreImageCount(gridPos);
		synchronized(this.Overlays)
			{
			updatestoreImageCount(gridPos);
			if ( this.Overlays[gridPos][updatePos] == null )
				{
				this.Overlays[gridPos][updatePos] = new OverlayImages(this.Log);
				}
			this.Overlays[gridPos][updatePos].updateOverlayImage(image);
			synchronized(this.ActualOverlays)
				{
				this.ActualOverlays[gridPos][updatePos] = new TranscoderVideoOverlayFrame(440, 210, this.Overlays[gridPos][updatePos].returnOverlayImage());
				int x = 100; int y=100;
				if ( gridPos == 0 ) { x=150;y=50; }
				if ( gridPos == 1 ) { x=150;y=290; }
				if ( gridPos == 2 ) { x=690;y=50; }
				if ( gridPos == 3 ) { x=690;y=290; }

				this.ActualOverlays[gridPos][updatePos].setDstX(x);
				this.ActualOverlays[gridPos][updatePos].setDstY(y);
				}
				synchronized(this.hasActiveImages)
				{
				this.hasActiveImages[gridPos] = true;
				}
			}
			
		}
		
	
	public boolean returnActiveImages(int gridPos)
	{
		return this.hasActiveImages[gridPos];
	}
	
	public void setInActiveImages (int gridPos)
	{
		synchronized(this.hasActiveImages)
		{
		this.hasActiveImages[gridPos] = false;
		synchronized(this.transImageCurrentCount)
			{
			this.transImageCurrentCount[gridPos] =0;
			}
		}
	}
	
	public void setgridPosName (String Name, int gridPos)
	{
		this.gridPosNames[gridPos] = Name;
	}
	
	public int getgridPosName (String Name)
	{
		for (int a=0; a<this.gridPosNames.length; a++ )
			{
			
			if ( this.gridPosNames[a].equalsIgnoreCase(Name) )
				{
				return a;
				}
			}
		return -1;
	}
	
	public String[] returnallgridPosNames()
	{
		return this.gridPosNames;
	}
	
	synchronized public byte[] returnOverlayImage(int gridPos)
		{
		int transPos = returntransImageCount(gridPos);
		updatetransImageCount(gridPos);
		return this.Overlays[gridPos][transPos].returnOverlayImage();
		}
	
	public TranscoderVideoOverlayFrame returnOverlayImageReal(int gridPos)
	{
	int transPos = returntransImageCount(gridPos);
	updatetransImageCount(gridPos);
	return this.ActualOverlays[gridPos][transPos];
	}

	
}
