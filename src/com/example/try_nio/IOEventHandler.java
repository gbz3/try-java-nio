package com.example.try_nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * IOイベントを処理するハンドラ
 *
 */
public interface IOEventHandler {

	void handleReadable( SocketChannel sc ) throws IOException;
	void handleWritable( SocketChannel sc ) throws IOException;
}
