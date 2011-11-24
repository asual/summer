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

package com.asual.summer.sample.websocket

import com.sun.jersey.spi.resource.Singleton

import java.text.SimpleDateFormat
import java.util._

import javax.ws.rs._

import org.atmosphere.annotation.Suspend
import org.atmosphere.cpr.BroadcasterFactory
import org.atmosphere.jersey.Broadcastable
import org.atmosphere.jersey.JerseyBroadcaster

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Path("/clock")
@Singleton
class Clock {
  
	var date = new Date()
	var timer = new Timer()
	var topic = BroadcasterFactory.getDefault().get(classOf[JerseyBroadcaster], "clock")
	var dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z (EEE, dd MMM yyyy)")
 
	timer.schedule(new TimerTask() {
		def run = {
			date.setTime(System.currentTimeMillis())
			topic.broadcast(dateFormat.format(date))
		}
	}, date, 1000)
	
	@GET
	@Suspend(resumeOnBroadcast = true)
	def subscribe():Broadcastable = {
		return new Broadcastable(topic)
	}

}