package org.superbank.dataRepository.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "UC_NAMES", columnNames = {"name", "lastName"}))
public class Customer {
    @Id
    @GeneratedValue
    private long id;

    private String name;
    private String lastName;

    @OneToMany(fetch = FetchType.EAGER)
    private Collection<Account> accounts = new HashSet<>();

    public Customer() {
    }

    public Customer(String lastName, String name) {
        this.lastName = lastName;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Collection<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Collection<Account> accounts) {
        this.accounts = accounts;
    }
}
