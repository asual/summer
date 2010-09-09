package com.asual.summer.sample.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.net.URL;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.asual.summer.core.util.StringUtils;

@Configurable
@Entity
@Table(name="technology", uniqueConstraints={@UniqueConstraint(columnNames="name")})
public class Technology implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private transient EntityManager entityManager;
    
    @Id
    @GeneratedValue(strategy=IDENTITY)
    @Column(unique=true, nullable=false)
    private Integer id;
    
    @Column(unique=true, nullable=false)
    private String value;
    
    @NotEmpty
    @Size(max=128)
    @Column(length=128)
    private String name;
    
    @Size(min=128, max=512)
    @Column(length=512)
    private String description;

    @Size(max=255)
    @Column
    private String version;

    @Column
    private URL homepage;

    @ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinTable(name="technology_license",    
            joinColumns={ @JoinColumn(name="technology_id") },  
            inverseJoinColumns={ @JoinColumn(name="license_id") })
    private List<License> licenses;

    @Column
    private Status status;

    @Column
    private boolean required;

    @Lob
    private Image image;
    
    public Technology() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    public URL getHomepage() {
        return homepage;
    }

    public void setHomepage(URL homepage) {
        this.homepage = homepage;
    }

    public List<License> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<License> licenses) {
        this.licenses = licenses;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isRequired() {
        return required;
    }
    
    public void setRequired(boolean required) {
        this.required = required;
    }

    public Image getImage() {
        return this.image;
    }
    
    public void setImage(Image image) {
        this.image = image;
    }
    
    @Transactional
    public void persist() {
        entityManager.persist(this);
    }
    
    @Transactional
    public Technology merge() {
        Technology merged = entityManager.merge(this);
        entityManager.flush();
        return merged;
    }
    
    @Transactional
    public void remove() {
        if (entityManager.contains(this)) {
            entityManager.remove(this);
        } else {
            Technology attached = entityManager.find(this.getClass(), this.id);
            entityManager.remove(attached);
        }
    }
    
    @Transactional
    public void flush() {
        entityManager.flush();
    }

    public static final EntityManager entityManager() {
        EntityManager em = new Technology().entityManager;
        if (em == null) {
        	throw new IllegalStateException("Entity manager has not been injected.");
        }
        return em;
    }
    
    public static Technology findTechnology(Integer id) {
        if (id == null) {
        	return null;
        }
        return entityManager().find(Technology.class, id);
    }
    
    @SuppressWarnings("unchecked")
    public static List<Technology> findTechnologies() {
        return entityManager().createQuery("select o from Technology o").getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public static List<Technology> findTechnologies(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Technology o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    }
    
}