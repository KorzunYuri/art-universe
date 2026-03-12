package yurykorzun.art.universe.common.config.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.common.config.service.entity.ConfigPropertyEntity;

public interface ConfigPropertyRepository extends JpaRepository<ConfigPropertyEntity, String> {
}
