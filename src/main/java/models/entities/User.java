package models.entities;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.math.BigDecimal;

/*
 * ascii      boolean    decimal    float      int        set        time       tinyint    varint
 * bigint     counter    double     frozen     list       smallint   timestamp  uuid
 * blob       date       duration   inet       map        text       timeuuid   varchar
 *
 * Table User
 *   id uuid, PK
 *   name text,
 *   passwd text, # SALTED HASH?
 *   credits float
 */

@Entity(defaultKeyspace = "aws")
@CqlName(value = "user")
public class User {
    @CqlName("name")
    @PartitionKey
    private String name;

    @CqlName("password")
    private String password;

    @CqlName("salt")
    private String salt;

    @CqlName("credits")
    private BigDecimal credits;

    public User() {
    }

    public User(String name, String password, String salt, BigDecimal credits) {
        this.name = name;
        this.password = password;
        this.salt = salt;
        this.credits = credits;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public BigDecimal getCredits() {
        return credits;
    }

    public void setCredits(BigDecimal credits) {
        this.credits = credits;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
