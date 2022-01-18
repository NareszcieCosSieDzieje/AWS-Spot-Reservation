package models.daos;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.AvailabilityZone;

@Dao
public interface AvailabilityZoneDao {

    @Select
    AvailabilityZone findByRegion(String region);

    @Select
    AvailabilityZone findByRegionAndName(String region, String name);

    @Insert
    void save(AvailabilityZone availabilityZone);

    @Delete
    void delete(AvailabilityZone availabilityZone);

}

