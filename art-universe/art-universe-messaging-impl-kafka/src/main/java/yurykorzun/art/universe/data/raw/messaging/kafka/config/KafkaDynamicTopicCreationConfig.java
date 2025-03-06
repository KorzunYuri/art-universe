package yurykorzun.art.universe.data.raw.messaging.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.config.MethodKafkaListenerEndpoint;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import yurykorzun.art.universe.common.data.raw.Coded;
import yurykorzun.art.universe.common.data.raw.task.entity.TaskType;
import yurykorzun.art.universe.common.data.raw.task.entity.TaskTypeRegistry;
import yurykorzun.art.universe.common.data.raw.task.dto.TaskRunRequest;
import yurykorzun.art.universe.common.messaging.kafka.config.KafkaCommonAdminConfig;
import yurykorzun.art.universe.common.messaging.kafka.config.KafkaCommonConsumerConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for dynamic creation of listeners for tasks and api call messages
 * How it works:
 *  1)  class iterates over the values registered in {@link TaskTypeRegistry}
 *  2)  for every task type:
 *    a) new topic is created
 *    b) new listener is created. Callback for the listener must fulfill the contract:
 *      - be registered in the context under the following name: "${Domain}_${DataSource}_${TaskCode}_KafkaListenerCallback"
 *      - have a method with the following signature: "handleMessage(DataCollectionTaskMessage message)
 */
@Configuration
@Import(value = {
        KafkaCommonConsumerConfig.class,
        KafkaCommonAdminConfig.class
})
public class KafkaDynamicTopicCreationConfig implements KafkaListenerConfigurer, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    public KafkaAdmin.NewTopics kafkaDynamicTopics() {
        List<NewTopic> newTopics = new ArrayList<>();
        TaskTypeRegistry.getRegistry().forEach((key, taskType) -> {
            NewTopic topic = TopicBuilder.name(getTopicNameForType(taskType))
                    .replicas(1)
                    .partitions(3)
                .build();
            newTopics.add(topic);
        });
        return new KafkaAdmin.NewTopics(newTopics.toArray(new NewTopic[0]));
    }

    /**
     * Dynamically create consumers for task types.
     * @throws BeansException if there is no bean with @Qualifier matching a specific task type (watch class javadoc)
     */
    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) throws BeansException {
        TaskTypeRegistry.getRegistry().entrySet()
                .forEach(entry -> {
                    TaskType taskType = entry.getValue();
                    String topicName = getTopicNameForType(taskType);
                    String topicListenerQualifier = getDynamicListenerQualifier(taskType);
                    Object taskMessageListener = applicationContext.getBean(topicListenerQualifier);
                    MethodKafkaListenerEndpoint<String, TaskRunRequest> endpoint = new MethodKafkaListenerEndpoint<>();

                    endpoint.setId(getIdForDynamicListener(taskType));
                    endpoint.setGroupId(getGroupIdForDynamicListener(taskType));
                    endpoint.setTopics(topicName);
                    endpoint.setBean(taskMessageListener);
                    try {
                        endpoint.setMethod(taskMessageListener.getClass().getMethod("handleMessage", TaskType.class));
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(
                                "Method 'handleMessage' is not found for dynamic taskMessageListener " + taskMessageListener, e);
                    }
                    registrar.registerEndpoint(endpoint);
                });
    }

    public String getTopicNameForType(Coded type) {
        return String.format("yurykorzun.art.universe.%s.data.raw.%s.%s,%s",
                type.getDomainCode(),
                type.getDataSourceCode(),
                type.getTypeName(),
                type.getCode());
    }

    /**
     * Return group id for dynamically created listener. For now, group id will be that same as topic name
     */
    public String getGroupIdForDynamicListener(Coded type) {
        return getTopicNameForType(type);
    }

    public String getIdForDynamicListener(Coded type) {
        return String.format("%s_KafkaListener", getTypeCode(type));
    }

    public String getDynamicListenerQualifier(Coded type) {
        return String.format("%s_KafkaListenerCallback", getTypeCode(type));

    }

    private String getTypeCode(Coded type) {
        return String.format("%s_%s_%s_%s",
                type.getDomainCode(),
                type.getDataSourceCode(),
                type.getTypeName(),
                type.getCode()
        );
    }

}
