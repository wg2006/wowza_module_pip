The transcoder mosaic code takes 5 streams and overlays 4 of them as insets.

The stream names have to be 

Stream1  - This is the main stream which sits behind the overlays
grabFrameStream1 - This is the overlay on the top left
grabFrameStream2 - This is the overlay on the top right
grabFrameStream3 - This is the overlay on the bottom left
grabFrameStream4 - This is the overlay on the bottom right

Only when the streams are present are they displayed as overlays.

You need to publish these streams from an encoder or use the Scheduler
available from Wowza Media Systems.

This uses 5 transcoder licenses and an enormous amount of CPU so if
it doesnt work for you then you are out of CPU and it may cost
you more than you thought in licensing.

Add the following module to the last Module in the Modules section of
the Application.xml

	<Module>
		<Name>test</Name>
		<Description>test</Description>
		<Class>com.wowza.demo.transcoder.mosaic.Test</Class>
	</Module> 

Also enable the transcoder in the Application.xml and change

	<Templates>${SourceStreamName}.xml,transcode.xml</Templates>

to

	<Templates>${SourceStreamName}.xml</Templates>

Also copy the templates provided into the templates provided.

To help reduce CPU there is one property you can define in your
Application.xml in the last Properties section.

	<Property>
		<Name>transcoderMosaicSkipFrame</Name>
		<Value>4</Value>
	</Property>

The default is 0.

This value determines how often frames are taken from the grabFrameStreams and
used as overlays. The more often the more CPU and hence more issues.

The source code is included for reference.

The transcoder files are included for reference and you should play back

Stream1_720p

to see the resultant transcoded stream with the multiple overlays.

An example picture is shown in this folder for an expected output.

If you can not get it working I am not in a position to support you at
all in any way what so ever.


