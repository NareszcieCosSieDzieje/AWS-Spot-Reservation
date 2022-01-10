package models.daos;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.EC2Instance;

@Dao
public interface Ec2InstanceDao {

    @Select
    EC2Instance findByInstanceType(String instance_type);

    @Select
    EC2Instance findByInstanceTypeAndFamily(String instance_type, String family);

    @Select
    PagingIterable<EC2Instance> findAll();

    @Insert
    void save(EC2Instance ec2Instance);

    @Delete
    void delete(EC2Instance ec2Instance);

}
