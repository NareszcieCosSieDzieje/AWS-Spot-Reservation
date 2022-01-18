package models.daos;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.*;
import models.entities.User;

@Dao
public interface UserDao {

    @Select
    User find(String name);

    @Select
    PagingIterable<User> findAll();

    @Insert
    void save(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

}