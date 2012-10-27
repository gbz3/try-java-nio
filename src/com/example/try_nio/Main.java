package com.example.try_nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger( Main.class );
		
		final Thread th = new Thread( new Receiver(), "HOGE" );
		log.debug( "th.start." );
		th.start();

	}

}
