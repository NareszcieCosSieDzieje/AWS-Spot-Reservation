package models;

import com.datastax.oss.driver.api.mapper.annotations.Mapper;

@Mapper
public class InventoryMapper {

    @DaoFactory
    AvailabilityZoneDao availabilityZoneDao();

    @DaoFactory
    AWSSpotDao awsSpotDao();

    @DaoFactory
    AzToEc2MappingDao azToEc2MappingDao();

    @DaoFactory
    SpotsReservedDao spotsReservedDao();

}
