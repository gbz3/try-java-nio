package com.example.try_nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;

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

	private void receiveStart( Selector sel, InetSocketAddress listenAddr, int backLogSize ) {
		
		try {
			Thread.sleep( 1 );
		} catch ( InterruptedException e ) {
			// 割り込み例外は上に上げない
		}
	}

	@Override
	public void run() {

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
		}
	}

}
