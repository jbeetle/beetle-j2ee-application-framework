package com.beetle.framework.business.service.client;

import org.jboss.netty.channel.*;

import java.net.SocketAddress;

public class MockChannel implements Channel {

	@Override
	public int compareTo(Channel o) {
		throw new UnsupportedOperationException("Unsupported yet");
		// return -1;
	}

	@Override
	public ChannelFuture bind(SocketAddress arg0) {
		return null;
	}

	@Override
	public ChannelFuture close() {
		return null;
	}

	@Override
	public ChannelFuture connect(SocketAddress arg0) {
		return null;
	}

	@Override
	public ChannelFuture disconnect() {
		return null;
	}

	@Override
	public ChannelFuture getCloseFuture() {
		return null;
	}

	@Override
	public ChannelConfig getConfig() {
		return null;
	}

	@Override
	public ChannelFactory getFactory() {
		return null;
	}

	@Override
	public Integer getId() {
		return null;
	}

	@Override
	public int getInterestOps() {
		return -1;
	}

	@Override
	public SocketAddress getLocalAddress() {
		return null;
	}

	@Override
	public Channel getParent() {
		return null;
	}

	@Override
	public ChannelPipeline getPipeline() {
		return null;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return null;
	}

	@Override
	public boolean isBound() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public ChannelFuture setInterestOps(int arg0) {
		return null;
	}

	@Override
	public ChannelFuture setReadable(boolean arg0) {
		return null;
	}

	@Override
	public ChannelFuture unbind() {
		return null;
	}

	@Override
	public ChannelFuture write(Object arg0) {
		return null;
	}

	@Override
	public ChannelFuture write(Object arg0, SocketAddress arg1) {
		return null;
	}

}
