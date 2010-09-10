package com.asual.summer.sample.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.IOException;
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
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.asual.summer.core.util.StringUtils;

@Configurable
@Entity
@Table
public class Technology implements java.io.Serializable {

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

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        return value != null && value.equals(((Technology) obj).value);
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
    
    public static Technology find(String value) {
    	return (Technology) entityManager().createQuery("select o from Technology o where o.value = ?1").setParameter(1, value).getSingleResult();
    }
    
    @SuppressWarnings("unchecked")
    public static List<Technology> list() {
        return entityManager().createQuery("select o from Technology o").getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public static List<Technology> list(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Technology o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    } 
    
    public static class Image implements java.io.Serializable {

    	private static final long serialVersionUID = 1L;

        private String value;
        private String contentType;
        private byte[] bytes;
        
        public Image(MultipartFile file) throws IOException {
        	value = file.getOriginalFilename();
        	contentType = file.getContentType();
        	bytes = file.getBytes();
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
        
        public String getContentType() {
            return contentType;
        }
        
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
        
        public byte[] getBytes() { 
            return bytes; 
        }
        
        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
        
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (obj.getClass() != getClass())
                return false;
            return bytes != null && bytes.equals(((Image) obj).bytes);
        }
        
    }    
    
}