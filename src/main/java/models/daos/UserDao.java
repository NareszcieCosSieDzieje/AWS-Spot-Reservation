package models.daos;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.User;

import java.util.UUID;

@Dao
public interface UserDao {

    @Select
    User find(UUID id);

    @Select
    PagingIterable<User> findAll(UUID id);

    @Insert
    void save(User user);

    @Delete
    void delete(User user);

}