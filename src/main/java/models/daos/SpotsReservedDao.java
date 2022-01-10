package models.daos;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.SpotsReserved;

@Dao
public interface SpotsReservedDao {

    @Select
    SpotsReserved findByRegionAndInstanceTypeAndAzName(String region, String instance_type, String az_name);

    @Insert
    void save(SpotsReserved spotsReserved);

    @Delete
    void delete(SpotsReserved spotsReserved);

}
