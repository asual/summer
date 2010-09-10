package com.asual.summer.sample.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.asual.summer.core.util.StringUtils;

@Configurable
@Entity
@Table
public class License implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private transient EntityManager entityManager;
    
    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(unique=true, nullable=false)
    private Integer id;

    @NotEmpty
    @Column(unique=true, nullable=false)    
    private String value;
    
    @NotEmpty
    @Column
    private String name;
    
    public License() {
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null) {
            value = StringUtils.toURIPath(name);
        }
        this.name = name;
    }
    
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        return value != null && value.equals(((License) obj).value);
    }
    
    @Transactional
    public void persist() {
        entityManager.persist(this);
    }
    
    @Transactional
    public License merge() {
    	License merged = entityManager.merge(this);
        entityManager.flush();
        return merged;
    }
    
    @Transactional
    public void remove() {
        if (entityManager.contains(this)) {
            entityManager.remove(this);
        } else {
        	License attached = entityManager.find(this.getClass(), this.id);
            entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void flush() {
        entityManager.flush();
    }

    public static final EntityManager entityManager() {
        EntityManager em = new License().entityManager;
        if (em == null) {
        	throw new IllegalStateException("Entity manager has not been injected.");
        }
        return em;
    }
    
    public static License find(String value) {
    	return (License) entityManager().createQuery("select o from License o where o.value = ?1").setParameter(1, value).getSingleResult();
    }
    
    @SuppressWarnings("unchecked")
    public static List<License> list() {
        return entityManager().createQuery("select o from License o").getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public static List<License> list(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from License o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }

}