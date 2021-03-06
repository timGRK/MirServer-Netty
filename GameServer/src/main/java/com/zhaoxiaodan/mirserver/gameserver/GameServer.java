package com.zhaoxiaodan.mirserver.gameserver;

import com.zhaoxiaodan.mirserver.db.DB;
import com.zhaoxiaodan.mirserver.gameserver.engine.Engine;
import com.zhaoxiaodan.mirserver.gameserver.handler.GameLoginHandler;
import com.zhaoxiaodan.mirserver.network.PacketDispatcher;
import com.zhaoxiaodan.mirserver.network.debug.ExceptionHandler;
import com.zhaoxiaodan.mirserver.network.decoder.ClientPacketBit6Decoder;
import com.zhaoxiaodan.mirserver.network.decoder.ClientPacketDecoder;
import com.zhaoxiaodan.mirserver.network.encoder.ServerPacketBit6Encoder;
import com.zhaoxiaodan.mirserver.network.encoder.ServerPacketEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class GameServer {

	public static final int REQUEST_MAX_FRAME_LENGTH = 2048;                    // 封包每一帧的最大大小
	public static final int DEFAULT_GAME_SERVER_PORT = 7200;                    // 登录网关默认端口号

	private int port;

	public GameServer(int port) {
		this.port = port == 0 ? DEFAULT_GAME_SERVER_PORT : port;
	}

	public void run() throws Exception {

		// db init
		DB.init();
		Engine.init();

		EventLoopGroup bossGroup   = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(

									new ExceptionHandler(),

									//解码
//									new MyLoggingHandler(MyLoggingHandler.Type.Read),
									new DelimiterBasedFrameDecoder(REQUEST_MAX_FRAME_LENGTH, false, Unpooled.wrappedBuffer(new byte[]{'!'})),
									new ClientPacketBit6Decoder(),
//									new MyLoggingHandler(MyLoggingHandler.Type.Read),
									new ClientPacketDecoder(GameClientPackets.class.getCanonicalName()),
//									new MyLoggingHandler(MyLoggingHandler.Type.Read),

									//编码
//									new MyLoggingHandler(MyLoggingHandler.Type.Write),
									new ServerPacketBit6Encoder(),
//									new MyLoggingHandler(MyLoggingHandler.Type.Write),
									new ServerPacketEncoder(),
//									new MyLoggingHandler(MyLoggingHandler.Type.Write),
									new ExceptionHandler(),
									//分包分发
									new PacketDispatcher(GameLoginHandler.class.getPackage().getName())


							);
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = b.bind(port).sync();

			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		int port;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		} else {
			port = DEFAULT_GAME_SERVER_PORT;
		}
		new GameServer(port).run();
	}
}
