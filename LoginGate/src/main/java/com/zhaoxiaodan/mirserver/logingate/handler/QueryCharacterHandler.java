package com.zhaoxiaodan.mirserver.logingate.handler;

import com.zhaoxiaodan.mirserver.db.DB;
import com.zhaoxiaodan.mirserver.db.entities.User;
import com.zhaoxiaodan.mirserver.network.*;
import io.netty.channel.ChannelHandlerContext;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.List;

/**
 * +-------------------------------------------------+
 * |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 01 00 00 00 08 02 00 00 01 00 00 00 2a c5 d6 c1 |............*...|
 * |00000010| ba b9 fe b9 fe 2f 30 2f 32 2f 32 2f 30 2f 00 00 |...../0/2/2/0/..|
 * |00000020| 00 00 00 00 00 00 00 00                         |........        |
 * +--------+-------------------------------------------------+----------------+
 * +-------------------------------------------------+
 * |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |
 * +--------+-------------------------------------------------+----------------+
 * |00000000| 02 00 00 00 08 02 00 00 01 00 00 00 2a c5 d6 c1 |............*...|
 * |00000010| ba b9 fe b9 fe 2f 30 2f 32 2f 32 2f 30 2f 70 61 |...../0/2/2/0/pa|
 * |00000020| 6e 67 6c 69 61 6e 67 2f 32 2f 35 2f 30 2f 31 2f |ngliang/2/5/0/1/|
 * |00000030| 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 |................|
 * +--------+-------------------------------------------------+----------------+
 */
public class QueryCharacterHandler implements PacketHandler {

	@Override
	public void exce(ChannelHandlerContext ctx, Packet packet) {
		ClientPackets.QueryCharacter request = (ClientPackets.QueryCharacter) packet;

		Session session = DB.getSession();

		List<User> list = session.createCriteria(User.class).add(Restrictions.eq("loginId",request.user.loginId)).list();
		if(1 == list.size() && list.get(0).characters.size() > 0)
		{
			ctx.writeAndFlush(new ServerPackets.QueryCharactorOk(list.get(0).characters));
		}else{
//			ctx.writeAndFlush(new Packet(Protocol.SM_QUERYCHR_FAIL));
//			return ;
		}
	}

}