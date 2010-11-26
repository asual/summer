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

package com.asual.summer.sample.domain

import com.asual.summer.core.util.StringUtils

import java.lang.Integer
import java.util.List

import javax.persistence._

import org.hibernate.validator.constraints.NotEmpty
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.transaction.annotation.Transactional

import scala.reflect.BeanProperty

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
class Status {

    @PersistenceContext
    @BeanProperty
    @transient
    var entityManager:EntityManager = _
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    @BeanProperty
    var id:Integer = _
    
    @NotEmpty
    @Column(unique=true, nullable=false)
    @BeanProperty
    var value:String = _
    
    @NotEmpty
    @Column
    @BeanProperty
    var name:String = _
    
	override def hashCode = {
    	41 * value.hashCode
    }
	
	override def equals(other:Any) = other match {
		case that: Status => 
			(that canEqual this) && (this.value == that.value)
		case _ => 
			false
	} 
	
	def canEqual(other:Any) = {
		other.isInstanceOf[Status]
	}

	@Transactional
    def persist() = {
        entityManager.persist(this)
    }
    
    @Transactional
    def merge():Status = {
    	var merged:Status = entityManager.merge(this)
        entityManager.flush()
        return merged
    }
    
    @Transactional
    def remove() = {
        if (entityManager.contains(this)) {
            entityManager.remove(this)
        } else {
        	var attached:Status = 
        		entityManager.find(this.getClass(), this.id).asInstanceOf[Status]
            entityManager.remove(attached)
        }
    }
    
    @Transactional
    def flush() = {
        entityManager.flush()
    }
}

object Status {

    def entityManager():EntityManager = {
        var em:EntityManager = new Status().entityManager
        if (em == null) {
        	throw new IllegalStateException("Entity manager has not been injected.")
        }
        return em
    }
    
    def find(value:String):Status = {
    	return entityManager()
    		.createQuery("select o from Status o where o.value = ?1")
    			.setParameter(1, value).getSingleResult().asInstanceOf[Status]
    }
    
    def list():List[Status] = {
        return entityManager().createQuery("select o from Status o")
        	.getResultList().asInstanceOf[List[Status]]
    }
    
    def list(firstResult:Int, maxResults:Int):List[Status] = {
        return entityManager().createQuery("select o from Status o")
        	.setFirstResult(firstResult).setMaxResults(maxResults)
        		.getResultList().asInstanceOf[List[Status]]
    }
    
}