package org.fffd.l23o6.dao;

import org.fffd.l23o6.pojo.entity.TrainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TrainDao extends JpaRepository<TrainEntity, Long>{
    //@Query("SELECT t FROM TrainEntity t WHERE : routeId MEMBER OF routesId")
    List<TrainEntity> findByRouteIdIn(List<Long> routesId);

}
