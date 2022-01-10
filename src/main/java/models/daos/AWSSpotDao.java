package models.daos;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.AWSSpot;

@Dao
public interface AWSSpotDao {

    @Select
    AWSSpot findByRegionAndAzNameAndInstanceType(String region, String az_name, String instance_type);

    @Select
    AWSSpot findByRegionAndAzNameAndInstanceTypeAndMaxPrice(String region, String az_name, String instance_type, Float max_price);

    @Insert
    void save(AWSSpot awsSpot);

    @Delete
    void delete(AWSSpot awsSpot);

}
