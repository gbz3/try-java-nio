package com.example.try_nio;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * IO�C�x���g����������n���h��
 *
 */
public interface IOEventHandler {

	void handleReadable( SocketChannel sc ) throws IOException;
	void handleWritable( SocketChannel sc ) throws IOException;
}
