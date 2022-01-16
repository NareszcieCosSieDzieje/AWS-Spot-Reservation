package models.entities;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

import java.math.BigDecimal;
import java.util.UUID;

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

    @PartitionKey
    @CqlName("id")
    private UUID id;

    @CqlName("name")
    private String name;

    @CqlName("name")
    private String password;

    @CqlName("name")
    private BigDecimal credits;

    public User() {
    }

    public User(UUID id, String name, String password, BigDecimal credits) {
        this.id = id;
        this.name = name;
        this.password = password;
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

    public UUID getId() {
        return id;
    }

}
