/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework.business.service.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import com.beetle.framework.AppProperties;
import com.beetle.framework.business.service.common.AsyncMethodCallback;
import com.beetle.framework.business.service.common.RpcConst;
import com.beetle.framework.business.service.common.RpcRequest;
import com.beetle.framework.business.service.common.RpcResponse;
import com.beetle.framework.log.AppLogger;

public class RpcClientHandler extends SimpleChannelUpstreamHandler {
	private static final AppLogger logger = AppLogger
			.getInstance(RpcClientHandler.class);
	private volatile Channel channel;
	private final BlockingQueue<RpcResponse> resultQueue;
	private final int timeout;
	private volatile boolean invokeFlag;

	public RpcClientHandler() {
		super();
		resultQueue = new LinkedBlockingQueue<RpcResponse>();
		timeout = AppProperties.getAsInt("rpc_client_invoke_max_waitForTime",
				1000 * 60 * 10);
		this.invokeFlag = false;
	}

	public void asyncInvoke(final RpcRequest req) {
		channel.write(req);
	}

	public RpcResponse invoke(final RpcRequest req) {
		invokeFlag = true;
		try {
			return docall(req);
		} finally {
			invokeFlag = false;
		}
	}

	private RpcResponse docall(final RpcRequest req) {
		channel.write(req);
		RpcResponse res;
		// resultQueue.clear();??
		boolean interrupted = false;
		for (;;) {
			try {
				// res = resultQueue.take();
				res = resultQueue.poll(timeout, TimeUnit.MILLISECONDS);
				if (res == null) {
					res = new RpcResponse();
					res.setReturnFlag(RpcConst.ERR_CODE_CLIENT_INVOKE_TIMEOUT_EXCEPTION);
					res.setReturnMsg("client invoke timeout[" + timeout + "ms]");
					channel.close();
				}
				break;
			} catch (InterruptedException e) {
				channel.close();// 超时关闭链路，以防服务端执行完毕后通过此通过返回
				interrupted = true;
			} finally {
				resultQueue.clear();// 以防有垃圾
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
		return res;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		this.channel = e.getChannel();
		super.channelOpen(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		logger.debug("channelDisconnected:{}", e);
		try {
			repair("server shutdwon raise err");
		} finally {
			super.channelDisconnected(ctx, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("message:{}", e.getMessage());
		}
		// logger.info(e.getMessage());
		if (e.getMessage() instanceof RpcResponse) {
			// ServiceClient.putResultIntoCache((RpcResponse) e.getMessage());
			RpcResponse rrs = (RpcResponse) e.getMessage();
			if (!rrs.isAsync()) {
				boolean f = resultQueue.offer(rrs);
				if (logger.isDebugEnabled()) {
					logger.debug("insert into queue state:{}", f);
				}
			} else {
				if (rrs.getResult() != null) {
					@SuppressWarnings("rawtypes")
					AsyncMethodCallback amcbObj = (AsyncMethodCallback) rrs
							.getResult();
					logger.debug("callback work:{}", amcbObj);
					if (rrs.getReturnFlag() < 0) {
						amcbObj.onError(rrs.getReturnFlag(),
								rrs.getReturnMsg(),
								(Throwable) rrs.getException());
					} else {
						amcbObj.onComplete(amcbObj.getResult());
					}
				}
			}
		} else {
			// ctx.getChannel().write(req);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		logger.error("Unexpected exception {}", e.getCause());
		try {
			repair(logger.getStackTraceInfo(e.getCause()));
		} finally {
			e.getChannel().close();
		}
	}

	private void repair(String info) {
		if (resultQueue.isEmpty() && invokeFlag) {
			invokeFlag = false;
			RpcResponse res = new RpcResponse();
			res.setReturnFlag(RpcConst.ERR_CODE_REMOTE_CALL_EXCEPTION);
			res.setReturnMsg(info);
			// res.setException(e.getCause());
			boolean f = resultQueue.offer(res);
			if (logger.isDebugEnabled()) {
				logger.debug("insert exception response into queue state:{}", f);
			}
		}
	}

}
