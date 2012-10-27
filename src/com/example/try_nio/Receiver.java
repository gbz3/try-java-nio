package com.example.try_nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TCPレイヤでのデータ転送を行う。<br />
 * <br />
 * 割り込みを受けた場合、バッファのデータは破棄し、即時終了する。バッファの転送の終了を待つ動作はオプション。<br />
 * ◆IOイベント時の挙動<br />
 * [接続]<br />
 * クライアントからの接続を accept し、転送先ホストへ接続を試みる。<br />
 * 転送先ホストへの接続が失敗したら、クライアントからの接続を閉じる<br />
 * [読み込み]<br />
 * クライアントから受信したデータをそのまま転送先ホストへ転送する。転送データのバッファリングは極力避け、転送を優先する。<br />
 * [書き込み]<br />
 * 転送先ホストから受信したデータをそのままクライアントへ転送する。転送データのバッファリングは極力避け、転送を優先する。<br />
 * [切断]<br />
 * クライアントから切断された場合、クライアントへの転送データは破棄する。転送先ホストへの転送データを全て転送を試みてから切断する。<br />
 * 転送先ホストから切断された場合、転送先ホストへの転送データは破棄する。クライアントへの転送データを全て転送を試みてから切断する。<br />
 * クライアントから読み込みストリームのみが閉じられた場合、転送先ホストへの転送データを全て転送を試みてから、転送先ホストへの書き込みストリームのみを閉じる。<br />
 * 転送先ホストから読み込みストリームのみが閉じられた場合、クライアントへの転送データを全て転送を試みてから、クライアントへの書き込みストリームのみを閉じる。<br />
 * クライアントから書き込みストリームのみが閉じられた場合、転送先ホストからの読み込みストリームを閉じ、クライアントへの転送データを破棄する。<br />
 * 転送先ホストから書き込みストリームのみが閉じられた場合、クライアントからの読み込みストリームを閉じ、転送先ホストへの転送データを破棄する。<br />
 * 両方向のストリームが閉じられた時、ソケットをクローズする。→必要？<br />
 * 
 * 
 */
public class Receiver implements Runnable {
	
	final Logger log = LoggerFactory.getLogger( Receiver.class );

	private void receiveStart( Selector sel, InetSocketAddress listenAddr, int backLogSize ) {
		
		ServerSocketChannel ssc = null;
		try {
			
			try {
				ssc = SelectorProvider.provider().openServerSocketChannel();
				ssc.socket().setReuseAddress( true );
				ssc.configureBlocking( false );
				ssc.socket().bind( listenAddr, backLogSize );
				final SelectionKey sscKey = ssc.register( sel, SelectionKey.OP_ACCEPT );
				
				log.debug( "registed selection-key. {}",
						( sscKey.isValid() ? " isValid": "" )
						+ ( sscKey.isAcceptable() ? " isAcceptable": "" )
						+ ( sscKey.isConnectable() ? " isConnectable": "" )
						+ ( sscKey.isReadable() ? " isReadable": "" )
						+ ( sscKey.isWritable() ? " isWritable": "" ) );
				
				while( sel.keys().size() > 0 ) {
					final int selCnt = sel.select();	// TODO: timeout
					log.debug( "select()={}", selCnt );
					Thread.sleep( 1000 );
					
					final Set<SelectionKey> readyKeys = sel.selectedKeys();
					synchronized( readyKeys ) {
						final Iterator<SelectionKey> readyKeysIte = readyKeys.iterator();
						while( readyKeysIte.hasNext() ) {
							SelectionKey key = readyKeysIte.next();
							readyKeysIte.remove();
							if( !key.isValid() ) continue;

							try {
								if( key.isAcceptable() ) {
									log.trace( "acceptable." );
									handleAcceptable( key, sel );
									
								} else if ( key.isConnectable() ) {
									log.trace( "connectable." );
									
								} else if ( key.isReadable() ) {
									log.trace( "readable." );
									handleReadable( key );
									
								} else if ( key.isWritable() ) {
									log.trace( "writable." );
									
								}
								//IOEventHandler handler = (IOEventHandler)key.attachment();
							} catch ( IOException e ) {
								log.error( "", e );
								// 処理継続
							}
						}
					}
				}
				
				log.debug( "OK?" );
				Thread.sleep( 10 * 1000 );
				
			} catch ( IOException e ) {
				log.error( "can't open server-socket.", e );
				ssc = null;
				return;
			}

		} catch ( InterruptedException e ) {
			log.warn( "interrupted.", e );
			// 割り込み例外は上に上げない
		} finally {
			if( ssc != null ) {
				if( ssc.isOpen() ) {
					try {
						ssc.close();
					} catch ( IOException e ) {
						log.error( "", e );
					}
				}
				ssc = null;
			}
		}
	}

	@Override
	public void run() {

		log.trace( "{} start", Receiver.class.getSimpleName() );
		Selector sel = null;
		try {
			sel = Selector.open();
			receiveStart( sel, new InetSocketAddress( InetAddress.getByName( "127.0.0.1" ), 8088 ), 50 );

		} catch ( IOException e ) {
			e.printStackTrace();

		} finally {
			if( sel != null && sel.isOpen() ) {
				try {
					sel.close();
				} catch ( IOException e ) {
					e.printStackTrace();
				}
			}
			sel = null;
			log.trace( "{} end", Receiver.class.getSimpleName() );
		}
	}

	private void handleAcceptable( SelectionKey key, Selector sel ) throws IOException {
		final ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
		final SocketChannel acceptedChannel = ssc.accept();
		if( acceptedChannel == null ) {
			log.debug( "accepted. but result is null" );
			return;
		}
		log.debug( "accepted." );

		acceptedChannel.configureBlocking( false );
		acceptedChannel.register( sel, SelectionKey.OP_READ, new CommandHandler() );
	}

	private void handleReadable( SelectionKey key ) throws IOException {
		final IOEventHandler handler = (IOEventHandler)key.attachment();
		final SocketChannel sc = (SocketChannel)key.channel();
		handler.handleReadable( sc );
		
		if( !sc.isOpen() ) {
			key.cancel();
			log.debug( "key.cancel()" );
		}
		
	}

	private void handleCommand( SelectionKey key ) throws IOException {
		final ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
	}

}
