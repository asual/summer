/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.sample.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.asual.summer.core.util.StringUtils;
import com.asual.summer.sample.domain.Technology.TechnologyListener;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Configurable
@Entity
@EntityListeners(TechnologyListener.class)
@Table
public class Technology implements Serializable {

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

    @NotNull
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
    	return image;
    }
    
    public void setImage(Image image) {
    	if (image == null) {
			Technology technology = find(value);
			if (technology != null) {
	    		image = technology.getImage();
			}
    	}
        this.image = image;
    }
    
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Technology other = (Technology) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
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
    
    @SuppressWarnings("unchecked")
    public static Technology find(String value) {
		List<Technology> technologies = 
			entityManager().createQuery("select o from Technology o where o.value = ?1").setParameter(1, value).getResultList();
    	if (technologies.size() != 0) {
    		return technologies.get(0);
    	}
    	return null;
    }
    
    @SuppressWarnings("unchecked")
    public static List<Technology> list() {
        return entityManager().createQuery("select o from Technology o").getResultList();
    }
    
    @SuppressWarnings("unchecked")
    public static List<Technology> list(int firstResult, int maxResults) {
        return entityManager().createQuery("select o from Technology o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();
    } 
    
    public static class Image implements Serializable {

    	private static final long serialVersionUID = 1L;

        private String value;
        private String contentType;
        private byte[] bytes;
        
        public Image(MultipartFile file) throws IOException {
        	setValue(file.getOriginalFilename());
        	setContentType(file.getContentType());
        	setBytes(file.getBytes());
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
        	flush();
        }
        
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(bytes);
			return result;
		}
		
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Image other = (Image) obj;
			if (!Arrays.equals(bytes, other.bytes))
				return false;
			return true;
		}
	    
	    public void flush() {
			File file = new File(new File(System.getProperty("java.io.tmpdir")), String.valueOf(hashCode()));
			if (!file.exists()) {
				try {
					FileOutputStream fos = new FileOutputStream(file);
			    	ObjectOutputStream oos = new ObjectOutputStream(fos);
			    	oos.writeObject(this);
			    	oos.flush();
			    	fos.close();
				} catch (IOException e) {
				}
			}
	    }
	    
	    public static Image find(String hashCode) {
	    	try {
				File file = new File(new File(System.getProperty("java.io.tmpdir")), hashCode);
		    	FileInputStream fis = new FileInputStream(file);
			    ObjectInputStream ois = new ObjectInputStream(fis);
			    return (Image) ois.readObject();
			} catch (IOException e) {
			} catch (ClassNotFoundException e) {    
			}
			return null;
	    }
	    
    }
    
    public static class TechnologyListener {
	    
    	@PostLoad
    	public void postLoad(Technology technology) {
    		technology.getImage().flush();
    	}    	
    }
    
}