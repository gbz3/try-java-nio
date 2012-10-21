package com.example.try_nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;

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

	private void receiveStart( Selector sel, InetSocketAddress listenAddr, int backLogSize ) {
		
		try {
			Thread.sleep( 1 );
		} catch ( InterruptedException e ) {
			// ���荞�ݗ�O�͏�ɏグ�Ȃ�
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
