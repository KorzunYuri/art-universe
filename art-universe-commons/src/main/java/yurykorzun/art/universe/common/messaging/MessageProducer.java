package yurykorzun.art.universe.common.messaging;

import yurykorzun.art.universe.common.messaging.dto.DataCollectionTaskMessage;

public interface MessageProducer {

    void send(DataCollectionTaskMessage message);

}
