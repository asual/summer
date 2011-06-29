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

import java.io.Serializable;
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

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Configurable
@Entity
@Table
public class License implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@PersistenceContext
	private transient EntityManager entityManager;
	
	@Id
	@GeneratedValue(strategy=IDENTITY)
	@Column(nullable=false)
	private Long id;

	@NotEmpty
	@Column(nullable=false)	
	private String value;
	
	@NotEmpty
	@Column
	private String name;
	
	public License() {
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
		License other = (License) obj;
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