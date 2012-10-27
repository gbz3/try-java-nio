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
 * TCP���C���ł̃f�[�^�]�����s���B<br />
 * <br />
 * ���荞�݂��󂯂��ꍇ�A�o�b�t�@�̃f�[�^�͔j�����A�����I������B�o�b�t�@�̓]���̏I����҂���̓I�v�V�����B<br />
 * ��IO�C�x���g���̋���<br />
 * [�ڑ�]<br />
 * �N���C�A���g����̐ڑ��� accept ���A�]����z�X�g�֐ڑ������݂�B<br />
 * �]����z�X�g�ւ̐ڑ������s������A�N���C�A���g����̐ڑ������<br />
 * [�ǂݍ���]<br />
 * �N���C�A���g�����M�����f�[�^�����̂܂ܓ]����z�X�g�֓]������B�]���f�[�^�̃o�b�t�@�����O�͋ɗ͔����A�]����D�悷��B<br />
 * [��������]<br />
 * �]����z�X�g�����M�����f�[�^�����̂܂܃N���C�A���g�֓]������B�]���f�[�^�̃o�b�t�@�����O�͋ɗ͔����A�]����D�悷��B<br />
 * [�ؒf]<br />
 * �N���C�A���g����ؒf���ꂽ�ꍇ�A�N���C�A���g�ւ̓]���f�[�^�͔j������B�]����z�X�g�ւ̓]���f�[�^��S�ē]�������݂Ă���ؒf����B<br />
 * �]����z�X�g����ؒf���ꂽ�ꍇ�A�]����z�X�g�ւ̓]���f�[�^�͔j������B�N���C�A���g�ւ̓]���f�[�^��S�ē]�������݂Ă���ؒf����B<br />
 * �N���C�A���g����ǂݍ��݃X�g���[���݂̂�����ꂽ�ꍇ�A�]����z�X�g�ւ̓]���f�[�^��S�ē]�������݂Ă���A�]����z�X�g�ւ̏������݃X�g���[���݂̂����B<br />
 * �]����z�X�g����ǂݍ��݃X�g���[���݂̂�����ꂽ�ꍇ�A�N���C�A���g�ւ̓]���f�[�^��S�ē]�������݂Ă���A�N���C�A���g�ւ̏������݃X�g���[���݂̂����B<br />
 * �N���C�A���g���珑�����݃X�g���[���݂̂�����ꂽ�ꍇ�A�]����z�X�g����̓ǂݍ��݃X�g���[������A�N���C�A���g�ւ̓]���f�[�^��j������B<br />
 * �]����z�X�g���珑�����݃X�g���[���݂̂�����ꂽ�ꍇ�A�N���C�A���g����̓ǂݍ��݃X�g���[������A�]����z�X�g�ւ̓]���f�[�^��j������B<br />
 * �������̃X�g���[��������ꂽ���A�\�P�b�g���N���[�Y����B���K�v�H<br />
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
								// �����p��
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
			// ���荞�ݗ�O�͏�ɏグ�Ȃ�
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
