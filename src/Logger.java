package uk.org.shamrock.demo.transcoder.mosaic;

import com.wowza.wms.logging.WMSLogger;

public class Logger {
	
	private WMSLogger Logger = null;
	private boolean Debug = false;
	
	public Logger(
			WMSLogger Log,
			boolean debug
			)
	{
					
			Logger = Log;
			Debug = debug;
	}


	
	public void LogMessage(	String Message )
	{
	if ( Debug == true )
		{
		Logger.info("Mosaic: '"+Message+"'");
		}
	}

}
