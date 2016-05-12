package uk.org.shamrock.demo.transcoder.mosaic;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.wowza.demo.transcoder.mosaic.overlays.OverlayImageHandler;
import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.media.model.MediaCodecInfoAudio;
import com.wowza.wms.media.model.MediaCodecInfoVideo;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoder;
import com.wowza.wms.stream.livetranscoder.ILiveStreamTranscoderNotify;
import com.wowza.wms.transcoder.model.ITranscoderFrameGrabResult;
import com.wowza.wms.transcoder.model.LiveStreamTranscoder;
import com.wowza.wms.transcoder.model.LiveStreamTranscoderActionNotifyBase;
import com.wowza.wms.transcoder.model.TranscoderNativeVideoFrame;
import com.wowza.wms.transcoder.model.TranscoderSession;
import com.wowza.wms.transcoder.model.TranscoderSessionVideo;
import com.wowza.wms.transcoder.model.TranscoderStream;
import com.wowza.wms.transcoder.model.TranscoderStreamSourceVideo;
import com.wowza.wms.transcoder.model.TranscoderVideoDecoderNotifyBase;
import com.wowza.wms.transcoder.model.TranscoderVideoOverlayFrame;
import com.wowza.wms.transcoder.util.TranscoderStreamUtils;

public class Test extends ModuleBase {

	private Logger Log = null;
	private OverlayImageHandler OHandler = null;
	private IApplicationInstance AppIns = null;
	private String transcoderNames = null;
	private Map<String, TranscoderNameInfo> transcoderMap = new HashMap<String, TranscoderNameInfo>();
	private int OverLaySkipFrame = 0;
	
	class TranscoderNameInfo
	{
		String transcoderName = null;
		int GridPos = 0;
		MyTranscoderVideoDecoderNotifyBaseGrabber transcoder = null;
	}
	
	
	public void onAppStart(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStart: " + fullname);
		
		this.Log = new Logger(getLogger(),true);
		this.OHandler = new OverlayImageHandler(this.Log);
		this.AppIns = appInstance;
		try 
			{
			this.OverLaySkipFrame = appInstance.getProperties().getPropertyInt("transcoderMosaicSkipFrame", 0);
			}
		catch ( Exception OverLaySkip ) { this.OverLaySkipFrame = 0 ; }
		
	    appInstance.addLiveStreamTranscoderListener(new MyTranscoderCreateNotifier(this.OHandler,this.OverLaySkipFrame,this.Log));

	    this.transcoderNames = "grabFrameStream1:1|grabFrameStream2:2|grabFrameStream3:3|grabFrameStream4:4";
		
		String[] names = transcoderNames.split("[|]");
		for(int i=0;i<names.length;i++)
		{
			String[] name = names[i].split(":");
			int gridPos = 0;
			try 
				{
				gridPos = Integer.valueOf(name[1]);
				gridPos--;
				}
			catch ( Exception gridE ) { gridPos = -1; };
			if (gridPos<0) continue;
			this.Log.LogMessage("transcoder name '"+names[i]+"'");
			TranscoderNameInfo transcoderNameInfo = new TranscoderNameInfo();
			transcoderNameInfo.transcoderName = name[0];
			transcoderNameInfo.GridPos = gridPos;
			transcoderMap.put(name[0], transcoderNameInfo);
		}
		
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
		
	

	}

	public void onConnect(IClient client, RequestFunction function,
			AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
	}
	
	
	class StreamListener implements IMediaStreamActionNotify3
	{
		OverlayImageHandler OHandler = null;
		WMSProperties Props = null;
		String StreamNames = null;
		Logger Log = null;
		
		public StreamListener ( WMSProperties props, String streamnames, OverlayImageHandler ohandler, Logger log )
			{
			this.Props = props;
			this.StreamNames = streamnames;
			this.OHandler = ohandler;
			this.Log = log;
			}
		
		public void onMetaData(IMediaStream stream, AMFPacket metaDataPacket)
		{
		}

		public void onPauseRaw(IMediaStream stream, boolean isPause, double location)
		{
		}

		public void onPause(IMediaStream stream, boolean isPause, double location)
		{
		}

		public void onPlay(IMediaStream stream, String streamName, double playStart, double playLen, int playReset)
		{
		}

		public void onPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
		{
		}

		public void onSeek(IMediaStream stream, double location)
		{
		}

		public void onStop(IMediaStream stream)
		{
		}

		public void onUnPublish(IMediaStream stream, String streamName, boolean isRecord, boolean isAppend)
		{
			if ( this.StreamNames.contains(streamName) )
			{
				int pos = -1;
				
				pos = this.OHandler.getgridPosName(streamName);
				
				if ( pos >= 0 )
					{	
					this.OHandler.setInActiveImages(pos);
					}
			}
		}

		public void onCodecInfoAudio(IMediaStream stream,
				MediaCodecInfoAudio codecInfoAudio) {
		}

		public void onCodecInfoVideo(IMediaStream stream,
				MediaCodecInfoVideo codecInfoVideo) {
		}
	}	 
 
	public void onStreamCreate(IMediaStream stream)
	{
		IMediaStreamActionNotify2 actionNotify = new StreamListener(this.AppIns.getProperties(),this.transcoderNames,this.OHandler, this.Log);

		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			props.put("streamActionNotifier", actionNotify);
		}
		stream.addClientListener(actionNotify);
	}

	public void onStreamDestroy(IMediaStream stream)
	{

		IMediaStreamActionNotify2 actionNotify = null;
		WMSProperties props = stream.getProperties();
		synchronized (props)
		{
			actionNotify = (IMediaStreamActionNotify2) stream.getProperties().get("streamActionNotifier");
		}
		if (actionNotify != null)
		{
			stream.removeClientListener(actionNotify);
		}
	}
	
	
	
    class MyTranscoderCreateNotifier implements ILiveStreamTranscoderNotify
    {

 		private OverlayImageHandler OHandler = null;
 		private Logger Log = null;
 		private int OverLaySkipFrame = 0;
 	
 		public MyTranscoderCreateNotifier ( OverlayImageHandler ohandler, int skipCount, Logger log )
 		{
 			this.Log = log;
 			this.OHandler = ohandler;
 			this.OverLaySkipFrame = skipCount;
 		}
 	   
           public void onLiveStreamTranscoderCreate(ILiveStreamTranscoder liveStreamTranscoder, IMediaStream stream)
           {
                  ((LiveStreamTranscoder)liveStreamTranscoder).addActionListener(new MyTranscoderActionNotifier(this.OHandler,this.OverLaySkipFrame,this.Log));
           }
           public void onLiveStreamTranscoderDestroy(ILiveStreamTranscoder liveStreamTranscoder, IMediaStream stream)

           {
           }
           public void onLiveStreamTranscoderInit(ILiveStreamTranscoder liveStreamTranscoder, IMediaStream stream)
           {
           }      
    } 
    
    class MyTranscoderActionNotifier extends LiveStreamTranscoderActionNotifyBase
	{
		private OverlayImageHandler OHandler = null;
 		private int OverLaySkipFrame = 0;
		private Logger Log = null;
		 	   
 	   		public MyTranscoderActionNotifier( OverlayImageHandler ohandler, int skipCount, Logger log )
 	   		{ 
 	   		this.Log = log;
 			this.OHandler = ohandler;
 			this.OverLaySkipFrame = skipCount;
 	   		}
 	   

		public void onInitStop(LiveStreamTranscoder liveStreamTranscoder)
		{
			try
			{
				while(true)
				{
					if (!liveStreamTranscoder.getStreamName().contains("grabFrameStream"))
						break;
					TranscoderStream transcoderStream = liveStreamTranscoder.getTranscodingStream();
					if (transcoderStream == null)
						break;
					TranscoderSession transcoderSession = liveStreamTranscoder.getTranscodingSession();
					TranscoderSessionVideo transcoderVideoSession = transcoderSession.getSessionVideo();
					Iterator<TranscoderNameInfo> iter = transcoderMap.values().iterator();
					while(iter.hasNext())
					{
						TranscoderNameInfo transcoderNameInfo = iter.next();
						TranscoderStreamSourceVideo sourceVideo = transcoderStream.getSource().getVideo();
						if (sourceVideo != null )
						{
							if (liveStreamTranscoder.getStreamName().equalsIgnoreCase(transcoderNameInfo.transcoderName) )
							{
							this.OHandler.setgridPosName(transcoderNameInfo.transcoderName, transcoderNameInfo.GridPos);
							transcoderNameInfo.transcoder = new MyTranscoderVideoDecoderNotifyBaseGrabber(this.OHandler,this.OverLaySkipFrame,transcoderNameInfo.GridPos,this.Log);
							transcoderVideoSession.addFrameListener(transcoderNameInfo.transcoder);
							}
						}
					}
					break;
				}
				while (true)
				{
				if (!liveStreamTranscoder.getStreamName().equals("Stream1"))
						break;
		        TranscoderStream transcoderStream = liveStreamTranscoder.getTranscodingStream();
                if (transcoderStream == null)
                       break;
                TranscoderSession transcoderSession = liveStreamTranscoder.getTranscodingSession();
                TranscoderSessionVideo transcoderVideoSession = transcoderSession.getSessionVideo();
                transcoderVideoSession.addFrameListener(new MyTranscoderVideoDecoderNotifyBase(this.OHandler, this.OverLaySkipFrame, this.Log));
                break;
				}

			}
			catch(Exception e)
			{
				this.Log.LogMessage("onInitStop["+liveStreamTranscoder.getStreamName()+"] "+e.toString());
				e.printStackTrace();
			}
			
		}
	}
	

    class MyTranscoderVideoDecoderNotifyBase extends TranscoderVideoDecoderNotifyBase
    {
 	     private OverlayImageHandler OHandler = null;
 	     private Logger Log = null;
 	     private int OverLaySkipFrame = 0 ;
 	     private int t = 0;
 	     private int GridCount = 4;
 	     private Object lock = new Object();
 	   
 	      public MyTranscoderVideoDecoderNotifyBase ( OverlayImageHandler ohandler, int skipCount, Logger log )
 	      {
 	    	  this.OHandler = ohandler;
 	    	  this.OverLaySkipFrame = skipCount;
 	    	  this.Log = log;
 	      }
 	   
          public void onBeforeScaleFrame(TranscoderSessionVideo sessionVideo, TranscoderStreamSourceVideo sourceVideo, long frameCount)
           {
           synchronized(this.lock)
        	   {
        	   for ( int gridPos = 0; gridPos<GridCount; gridPos++ )
        			{
        		   if ( t == this.OverLaySkipFrame )
        				{
        			    if ( this.OHandler.returnActiveImages(gridPos) == true )
        					{
        			    	TranscoderVideoOverlayFrame overlay = this.OHandler.returnOverlayImageReal(gridPos);
        			    	if ( overlay != null )
        						{
        			    		sourceVideo.addOverlay(gridPos,overlay);
        						}
        					}
        			    else
        					{
        			    	sourceVideo.clearOverlay(gridPos);
        					}
        				}
        		   t++;
        		   if ( t > this.OverLaySkipFrame )
        				{
        			   	t =0;
        				}
        			}
        	   }
           }	
    	}
    
    
    
    
    class MyTranscoderVideoDecoderNotifyBaseGrabber extends TranscoderVideoDecoderNotifyBase
    {
 	     private OverlayImageHandler OHandler = null;
 	     private Logger Log = null;
 	     private int GridPos = 0;
 	     private int t = 0 ;
 	     private int OverLaySkipFrame = 0 ;
 	     private Object lock = new Object();
 	   
 	      public MyTranscoderVideoDecoderNotifyBaseGrabber ( OverlayImageHandler ohandler, int skipCount, int gridpos, Logger log )
 	      {
 	    	  this.Log = log;
 	    	  this.OHandler = ohandler;
 	    	  this.OverLaySkipFrame = skipCount;
 	    	  this.GridPos = gridpos;
 	      }
 	   
         public void onBeforeScaleFrame(TranscoderSessionVideo sessionVideo, TranscoderStreamSourceVideo sourceVideo, long frameCount)
         	{
        	synchronized (this.lock)
        		{
        		if ( t == this.OverLaySkipFrame )
    				{
        			sourceVideo.grabFrame(new CreateShot(this.OHandler,this.GridPos,this.Log),440,210);
    				}
        		t++;
        		if ( t>this.OverLaySkipFrame )
       				{
        			t =0;
       				}
        		}
         	}
    	}
    
	class CreateShot implements ITranscoderFrameGrabResult
	{
		
		private Logger Log = null;
		private OverlayImageHandler OHandler = null;
		private int gridPos = 0;
	    private Object lock = new Object();

			
		public CreateShot ( OverlayImageHandler ohandler, int gridpos, Logger log)
			{
			this.OHandler = ohandler;
			this.gridPos = gridpos;
			this.Log = log;	
			}
		
		public void onGrabFrame(TranscoderNativeVideoFrame videoFrame)
			{			
			BufferedImage image = TranscoderStreamUtils.nativeImageToBufferedImage(videoFrame);
        	synchronized (this.lock)
        		{
        		this.OHandler.updateOverlayImage(image, this.gridPos);
        		}
			}
	}
	
    
}
