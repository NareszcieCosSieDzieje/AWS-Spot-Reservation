package models.daos;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.AWSSpot;

import java.math.BigDecimal;
import java.util.UUID;

@Dao
public interface AWSSpotDao {

    @Select
    AWSSpot findByRegionAndAzNameAndInstanceType(String region, String az_name, String instance_type, UUID spot_id);

    @Select
    AWSSpot findByRegionAndAzNameAndInstanceTypeAndMaxPrice(String region, String az_name, String instance_type, UUID spot_id, BigDecimal max_price);

    @Select
    PagingIterable<AWSSpot> findAll();

    @Insert
    void save(AWSSpot awsSpot);

    @Delete
    void delete(AWSSpot awsSpot);

}
