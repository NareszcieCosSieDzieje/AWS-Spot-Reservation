package models.daos;

import com.datastax.oss.driver.api.mapper.annotations.*;
import models.entities.SpotsReserved;

@Dao
public interface SpotsReservedDao {

    @Select
    SpotsReserved findByRegionAndInstanceTypeAndAzName(String region, String instance_type, String az_name);

    @Increment(entityClass = SpotsReserved.class)
    void increment(String region, String instance_type, String az_name, long spots_reserved);

    @Delete
    void delete(SpotsReserved spotsReserved);

}
