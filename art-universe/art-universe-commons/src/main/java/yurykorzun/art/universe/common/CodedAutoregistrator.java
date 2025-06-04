package yurykorzun.art.universe.common;

import jakarta.annotation.PostConstruct;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * A trick to forcefully load all the {@link Coded} instances into {@link CodedRegistry}
 */
@Component
public class CodedAutoregistrator {

    @PostConstruct
    public void registerCodedInstances() {
        Reflections reflections = new Reflections("yurykorzun.art.universe");
        Set<Class<? extends Coded>> codedClasses = reflections.getSubTypesOf(Coded.class);
        for (Class<?> clazz : codedClasses) {
            try {
                Class.forName(clazz.getName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
