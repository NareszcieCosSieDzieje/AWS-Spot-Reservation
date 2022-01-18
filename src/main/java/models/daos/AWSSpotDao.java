package models.daos;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import models.entities.AWSSpot;

import java.math.BigDecimal;
import java.util.UUID;

@Dao
public interface AWSSpotDao {

    @Select
    AWSSpot findByRegionAndAzNameAndInstanceTypeAndID(String region, String az_name, String instance_type, UUID spot_id);

    @Select
    AWSSpot findByRegionAndAzNameAndInstanceAndIDTypeAndMaxPrice(String region, String az_name, String instance_type, UUID spot_id, BigDecimal max_price);

    @Select
    PagingIterable<AWSSpot> findAll();

    @Insert
    void save(AWSSpot awsSpot);

    @Delete
    void delete(AWSSpot awsSpot);

    @Update
    void update(AWSSpot awsSpot);

}
