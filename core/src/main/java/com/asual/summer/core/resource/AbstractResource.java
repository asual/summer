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

package com.asual.summer.core.resource;

import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * 
 * @author Rostislav Hristov
 *
 */
public abstract class AbstractResource implements PriorityOrdered {

	private int order = Ordered.LOWEST_PRECEDENCE;
	private String[] locations;
	
	public void setLocation(String location) {
		setLocations(new String[] {location});
	}
	
	public String[] getLocations() {
		return locations;
	}

	public void setLocations(String[] locations) {
		this.locations = locations;
	}
	
	public int getOrder() {
		return order;
	}
	
	public void setOrder(int order) {
		this.order = order;
	}
		
}