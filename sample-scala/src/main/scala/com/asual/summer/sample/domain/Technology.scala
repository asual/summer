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
import com.asual.summer.sample.domain.Technology.TechnologyListener

import java.io._
import java.lang.Integer
import java.net.URL
import java.util._

import javax.persistence._
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

import org.hibernate.validator.constraints.NotEmpty
import org.springframework.beans.factory.annotation.Configurable
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

import scala.reflect.BeanProperty

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Configurable
@Entity
@EntityListeners(Array(classOf[TechnologyListener]))
@Table
@SerialVersionUID(1L)
@serializable
class Technology {
    
    @PersistenceContext
    @BeanProperty
    @transient 
    var entityManager:EntityManager = _

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(unique=true, nullable=false)
    @BeanProperty
    var id:Integer = _
    
    @Column(unique=true, nullable=false)
    @BeanProperty 
    var value:String = _
    
    @NotEmpty
    @Size(max=128)
    @Column(length=128)
    var name:String = _
    
    @Size(min=128, max=512)
    @Column(length=512)
    @BeanProperty
    var description:String = _

    @Size(max=255)
    @Column
    @BeanProperty
    var version:String = _

    @Column
    @BeanProperty 
    var homepage:URL = _

    @ManyToMany(cascade=Array(CascadeType.ALL), fetch=FetchType.EAGER)
    @JoinTable(name="technology_license",    
            joinColumns=Array(new JoinColumn(name="technology_id")),  
            inverseJoinColumns=Array(new JoinColumn(name="license_id")))
    @BeanProperty
    var licenses:List[License] = _

    @NotNull
    @Column
    @BeanProperty
    var status:Status = _

    @Column
    @BeanProperty
    var required:Boolean = _

    @Lob
    var image:Technology.Image = _

    def getName():String = {
    	return name
    }
    
    def setName(name:String) = {
        if (name != null) {
            value = StringUtils.toURIPath(name)
        }
        this.name = name
    }

    def getImage():Technology.Image = {
    	return image
    }
    
    def setImage(image:Technology.Image) = {
    	if (image == null) {
			var technology:Technology = Technology.find(value)
			if (technology != null) {
	    		this.image = technology.getImage()
			}
    	} else {
    		this.image = image
    	}
    }

	override def hashCode = {
		41 * value.hashCode
	}
	
	override def equals(other:Any) = other match {
		case that: Technology => 
			(that canEqual this) && (this.value == that.value)
		case _ => 
			false
	} 
	
	def canEqual(other:Any) = {
		other.isInstanceOf[Technology]
	}
	
	@Transactional
    def persist() = {
		entityManager.persist(this)
	}
	
    @Transactional
    def merge():Technology = {
        var merged:Technology = entityManager.merge(this)
        entityManager.flush()
        return merged
    }
    
    @Transactional
    def remove() = {
        if (entityManager.contains(this)) {
            entityManager.remove(this)
        } else {
            var attached:Technology = entityManager.find(this.getClass(), this.id).asInstanceOf[Technology]
            entityManager.remove(attached)
        }
    }
    
    @Transactional
    def flush() = {
    	entityManager.flush()
    }
    
}

object Technology {
	
    def entityManager():EntityManager = {
        var em:EntityManager = new Technology().entityManager
        if (em == null) {
        	throw new IllegalStateException("Entity manager has not been injected.")
        }
        return em
    }
    
    def find(value:String):Technology = {
		var technologies:List[Technology] = 
			entityManager().createQuery("select o from Technology o where o.value = ?1")
				.setParameter(1, value).getResultList().asInstanceOf[List[Technology]]
    	if (technologies.size() != 0) {
    		return technologies.get(0)
    	}
    	return null
    }
    
    def list():List[Technology] = { 
    	return entityManager().createQuery("select o from Technology o").getResultList().asInstanceOf[List[Technology]]
    }
    
    def list(firstResult:Int, maxResults:Int):List[Technology] = {
    	return entityManager().createQuery("select o from Technology o")
    		.setFirstResult(firstResult)
    		.setMaxResults(maxResults).getResultList().asInstanceOf[List[Technology]]
	}

	@SerialVersionUID(1L)
	@serializable
	class Image(file:MultipartFile) {
	
	    @BeanProperty
	    var value:String = _
	    
	    @BeanProperty
	    var contentType:String = _
	    
	    var bytes:Array[Byte] = _
	    
        def getBytes():Array[Byte] = {
            return bytes
        }
        
        def setBytes(bytes:Array[Byte]) = {
            this.bytes = bytes
            flush()
        }
    
		override def hashCode = {
			41 * Arrays.hashCode(bytes)
		}
		
		override def equals(other:Any) = other match {
			case that: Image => 
				(that canEqual this) && (this.bytes == that.bytes)
			case _ => 
				false
		} 
		
		def canEqual(other:Any) = {
			other.isInstanceOf[Image]
		}
    	
        def flush() = {
			var file:File = new File(new File(System.getProperty("java.io.tmpdir")), String.valueOf(hashCode()))
			if (!file.exists()) {
                var fos:FileOutputStream = new FileOutputStream(file)
                var oos:ObjectOutputStream = new ObjectOutputStream(fos)
                oos.writeObject(this)
                oos.flush()
                fos.close()
			}
	    }
	    
    	value = file.getOriginalFilename()
    	contentType = file.getContentType()    	
    	setBytes(file.getBytes())

	}
	
    object Image {
    
        def find(hashCode:String):Image = {
            var file:File = new File(new File(System.getProperty("java.io.tmpdir")), hashCode)
            var fis:FileInputStream = new FileInputStream(file)
            var ois:ObjectInputStream = new ObjectInputStream(fis)
            return ois.readObject().asInstanceOf[Image]
        }
        
    }
	    
    class TechnologyListener {
	    
    	@PostLoad
    	def postLoad(technology:Technology) = {
    		var image:Image = technology.getImage()
    		if (image != null) {
    			image.flush()
    		}
    	}    	
    }
    
}