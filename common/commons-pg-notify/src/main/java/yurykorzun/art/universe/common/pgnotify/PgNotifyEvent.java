package yurykorzun.art.universe.common.pgnotify;

import org.springframework.context.ApplicationEvent;

public class PgNotifyEvent extends ApplicationEvent {

    private final String channel;

    public PgNotifyEvent(Object source, String channel) {
        super(source);
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }
}
