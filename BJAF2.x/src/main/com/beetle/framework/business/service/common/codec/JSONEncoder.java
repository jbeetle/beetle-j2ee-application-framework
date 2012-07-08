package com.beetle.framework.business.service.common.codec;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

public class JSONEncoder extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) throws Exception {
		ChannelBufferOutputStream bout = new ChannelBufferOutputStream(
				dynamicBuffer(1024, ctx.getChannel().getConfig()
						.getBufferFactory()));
		String txt = JSON.toJSONString(msg, SerializerFeature.WriteClassName);
		try {
			bout.writeBytes(txt);
		} finally {
			bout.close();
		}
		return bout.buffer();
	}
}
