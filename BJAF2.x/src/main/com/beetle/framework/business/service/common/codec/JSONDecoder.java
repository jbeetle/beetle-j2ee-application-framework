package com.beetle.framework.business.service.common.codec;

import com.alibaba.fastjson.JSON;
import com.beetle.framework.util.structure.DynamicByteArray;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;

public class JSONDecoder extends FrameDecoder {

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			ChannelBuffer buffer) throws Exception {
		ChannelBufferInputStream in = new ChannelBufferInputStream(buffer);
		DynamicByteArray dba = new DynamicByteArray(1024);
		try {
			while (true) {
				byte[] b = new byte[1024];
				int i = in.read(b);
				if (i == -1) {
					break;
				}
				dba.add(b, i);
			}
		} finally {
			in.close();
		}
		String txt = new String(dba.getBytes());
		return JSON.parse(txt.trim());
	}

}
