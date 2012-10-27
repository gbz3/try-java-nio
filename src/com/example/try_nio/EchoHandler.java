package com.example.try_nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EchoHandler implements IOEventHandler {

	final Logger log = LoggerFactory.getLogger( EchoHandler.class );

	final ByteBuffer buff = ByteBuffer.allocateDirect( 4096 );

	@Override
	public void handleReadable( final SocketChannel sc ) throws IOException {
		int rcount = sc.read( buff );
		log.debug( "read {} Bytes.", rcount );
		
		if( rcount < 0 ) {
			sc.close();
			log.debug( "SocketChannel.close()" );
			return;
		}
		
		buff.flip();
		int wcount = sc.write( buff );
		buff.compact();

		log.debug( "write {} Bytes.", wcount );
	}

	@Override
	public void handleWritable( final SocketChannel sc ) throws IOException {
		
	}
}
