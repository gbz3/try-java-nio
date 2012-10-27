package com.example.try_nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandHandler implements IOEventHandler {

	final Logger log = LoggerFactory.getLogger( CommandHandler.class );

	final int BUFF_SIZE = 4096;
	final ByteBuffer readBuff = ByteBuffer.allocateDirect( BUFF_SIZE );
	final ByteBuffer writeBuff = ByteBuffer.allocateDirect( BUFF_SIZE );

	@Override
	public void handleReadable( final SocketChannel sc ) throws IOException {
		int rcount = sc.read( readBuff );
		log.debug( "read {} Bytes.", rcount );

		if( rcount < 0 ) {
			sc.close();
			log.debug( "SocketChannel.close()" );
			return;
		}

		readBuff.flip();
		final byte[] bbuff = new byte[readBuff.limit()];
		readBuff.get( bbuff ).compact();
		final String input = new String( bbuff, "UTF-8" );	// TODO: コマンドが不完全な場合の対処
		log.debug( "[{}]", input );
		
		if( input.toLowerCase().startsWith( "exit" ) || input.toLowerCase().startsWith( "quit" ) ) {
			writeBuff.put( "bye.".getBytes( "UTF-8" ) ).flip();
			sc.write( writeBuff );
			writeBuff.compact();
			log.debug( "interrupt to currentThread." );
			Thread.currentThread().interrupt();
			return;
		}
		
		writeBuff.put( ( "input is [" + input + "]" ).getBytes( "UTF-8" ) ).flip();
		sc.write( writeBuff );
		writeBuff.compact();
	}

	@Override
	public void handleWritable( final SocketChannel sc ) throws IOException {
		
	}
}
