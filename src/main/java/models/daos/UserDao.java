package models.daos;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import models.entities.User;

@Dao
public interface UserDao {

    @Select
    User find(String name);

    @Select
    PagingIterable<User> findAll();

    @Insert
    void save(User user);

    @Delete
    void delete(User user);

}