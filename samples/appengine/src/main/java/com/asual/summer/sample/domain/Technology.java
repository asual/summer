/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.asual.summer.sample.domain;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.asual.summer.core.util.StringUtils;
import com.google.appengine.api.datastore.Link;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Configurable
@Entity
@Table
public class Technology implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(Technology.class);

	@PersistenceContext
	private transient EntityManager entityManager;
	
	@Id
	@GeneratedValue(strategy=IDENTITY)
	@Column(nullable=false)
	private Long id;
	
	@Column(nullable=false)
	private String value;
	
	@NotEmpty
	@Size(max=128)
	@Column(length=128)
	private String name;
	
	@Size(min=32, max=512)
	@Column(length=512)
	private String description;

	@Size(max=255)
	@Column
	private String version;

	@Column
	private Link homepage;
	
	@Column
	private List<Long> licenseIds;

	@NotNull
	@Column
	private Long statusId;

	@Column
	private boolean required;

	@Lob
	private Image image;
	
	public Technology() {
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
			value = StringUtils.toUriPath(name);
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
	
	public Link getHomepage() {
		return homepage;
	}

	public void setHomepage(Link homepage) {
		this.homepage = homepage;
	}

	public List<Long> getLicenseIds() {
		return licenseIds;
	}

	public void setLicenseIds(List<Long> licenseIds) {
		this.licenseIds = licenseIds;
	}
	
	public List<License> getLicenses() {
		List<License> list = new ArrayList<License>();
		if (licenseIds != null) {
			for (Long id : licenseIds) {
				list.add(entityManager.find(License.class, id));
			}
		}
		return list;
	}
	
	public void setLicenses(List<License> licenses) {
		List<Long> list = new ArrayList<Long>();
		if (licenses != null) {
			for (License license : licenses) {
				list.add(license.getId());
			}
		}
		setLicenseIds(list);
	}
	
	public Long getStatusId() {
		return statusId;
	}

	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}
	
	public Status getStatus() {
		if (statusId != null) {
			return entityManager.find(Status.class, statusId);
		}
		return null;
	}

	public void setStatus(Status status) {
		this.setStatusId(status != null ? status.getId() : null);
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}

	public Image getImage() {
		setImage(image);
		return image;
	}
	
	@SuppressWarnings("unchecked")
	public void setImage(Image image) {
		if (image != null) {
			try {
				CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
				Cache cache = cacheFactory.createCache(Collections.emptyMap());
				cache.put(value, image);
			} catch (CacheException e) {
				logger.error(e.getMessage(), e);
			}
		} else if (value != null) {
			image = findImage(value);
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
		List<Technology> resultList = 
			entityManager().createQuery("select o from Technology o where o.value = ?1").setParameter(1, value).getResultList();
		if (resultList.size() != 0) {
			return resultList.get(0);
		}
		return null;
	}
	
	public static Image findImage(String value) {
		if (value != null) {
			try {
				CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
				Cache cache = cacheFactory.createCache(Collections.emptyMap());
				return (Image) cache.get(value);
			} catch (CacheException e) {
				logger.error(e.getMessage(), e);
			}
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
		
	}
	
}