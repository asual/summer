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

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;

import com.asual.summer.core.util.StringUtils;

import scala.reflect.BeanProperty;
import scala.transient;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Configurable
@Entity
@Table
@SerialVersionUID(1L) 
@serializable
class License extends Serializable {

    @PersistenceContext
    @BeanProperty    
    @transient 
    var entityManager:EntityManager = _;
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    @BeanProperty
    var id:Integer = _;

    @NotEmpty
    @Column(unique=true, nullable=false)
    @BeanProperty
    var value:String = _;
    
    @NotEmpty
    @Column
    @BeanProperty
    var name:String = _;

//    public void setName(String name) {
//        if (name != null) {
//            value = StringUtils.toURIPath(name);
//        }
//        this.name = name;
//    }
    
//	public int hashCode() {
//		int prime = 31;
//		int result = 1;
//		result = (prime * result) + ((value == null) ? 0 : value.hashCode());
//		return result;
//	}
//    
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		License other = (License) obj;
//		if (value == null) {
//			if (other.value != null)
//				return false;
//		} else if (!value.equals(other.value))
//			return false;
//		return true;
//	}

    @Transactional
    def persist() = entityManager.persist(this);
    
    @Transactional
    def merge():License = {
    	var merged:License = entityManager.merge(this);
        entityManager.flush();
        return merged;
    }
    
    @Transactional
    def remove() = {
        if (entityManager.contains(this)) {
            entityManager.remove(this);
        } else {
        	var attached:License = 
        		entityManager.find(this.getClass(), this.id).asInstanceOf[License];
            entityManager.remove(attached);
        }
    }
    
    @Transactional
    def flush() = {
    	entityManager.flush();
    }    
}

object License {
	
	def entityManager():EntityManager = {
        var em:EntityManager = new License().entityManager;
        if (em == null) {
        	throw new IllegalStateException("Entity manager has not been injected.");
        }
        return em;
    }
    
    def find(value:String):License = {
    	return entityManager().createQuery("select o from License o where o.value = ?1")
    		.setParameter(1, value).getSingleResult().asInstanceOf[License];
    }
    
    def list():List[License] = {
        return entityManager().createQuery("select o from License o")
        	.getResultList().asInstanceOf[List[License]];
    }
    
    def list(firstResult:Int, maxResults:Int):List[License] = {
        return entityManager().createQuery("select o from License o")
        	.setFirstResult(firstResult).setMaxResults(maxResults)
        		.getResultList().asInstanceOf[List[License]];
    }

}