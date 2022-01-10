package models.mappers;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import models.daos.AWSSpotDao;
import models.daos.AZToEc2MappingDao;
import models.daos.AvailabilityZoneDao;
import models.daos.SpotsReservedDao;

@Mapper
public interface InventoryMapper {

    static MapperBuilder<InventoryMapper> builder(CqlSession session) {
        return new InventoryMapperBuilder(session);
    }

    @DaoFactory
    AvailabilityZoneDao availabilityZoneDao();

    @DaoFactory
    AWSSpotDao awsSpotDao();

    @DaoFactory
    AZToEc2MappingDao azToEc2MappingDao();

    @DaoFactory
    SpotsReservedDao spotsReservedDao();

}
