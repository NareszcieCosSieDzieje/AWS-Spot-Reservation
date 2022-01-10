package models.daos;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.AWSSpot;
import models.entities.AZToEC2Mapping;

@Dao
public interface AZToEc2MappingDao {

    @Select
    AZToEC2Mapping findByRegionAndInstanceType(String region, String instance_type);

    @Select
    AZToEC2Mapping findByRegionAndInstanceTypeAndAzName(String region, String instance_type, String az_name);

    @Insert
    void save(AZToEC2Mapping azToEC2Mapping);

    @Delete
    void delete(AZToEC2Mapping azToEC2Mapping);

}


