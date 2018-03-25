package cn.worldwalker.game.wyqp.web.listener;

import cn.worldwalker.game.wyqp.common.utils.log.ThreadPoolMgr;
import cn.worldwalker.game.wyqp.server.dispatcher.BaseMsgDisPatcher;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class BootstrapListener implements ApplicationListener<ContextRefreshedEvent>  {
	private static final Logger log = Logger.getLogger(BaseMsgDisPatcher.class);
    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
    	log.info("spring context refresh! -^-^-");
//    	TableMgr.getInstance().load();
    	ThreadPoolMgr.getLogDataInsertProcessor();
    }
}
