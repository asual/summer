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

package com.asual.summer.sample.websocket;

//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//import javax.ws.rs.GET;
//import javax.ws.rs.POST;
import javax.ws.rs.Path;

//import org.atmosphere.cpr.Broadcaster;
//import org.atmosphere.jersey.JerseyBroadcaster;
//import org.atmosphere.jersey.SuspendResponse;
//
//import com.google.appengine.api.taskqueue.Queue;
//import com.google.appengine.api.taskqueue.QueueFactory;
//import com.google.appengine.api.taskqueue.TaskOptions;
import com.sun.jersey.spi.resource.Singleton;

/**
 * 
 * @author Rostislav Hristov
 *
 */
@Path("/clock")
@Singleton
public class Clock {
	
//	private Broadcaster topic = new JerseyBroadcaster("clock");
//	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z (EEE, dd MMM yyyy)");
// 
//	public Clock() {
//		queue();
//	}
//	
//	@GET
//	public SuspendResponse<String> subscribe() {
//		return new SuspendResponse.SuspendResponseBuilder<String>()
//			.broadcaster(topic)
//			.outputComments(true)
//			.build();
//	}
//	
//	@POST
//	public void tick() {
//		Date date = new Date();
//		date.setTime(System.currentTimeMillis());
//		topic.broadcast(dateFormat.format(date));
//		queue();
//	}
//	
//	public void queue() {
//		Queue queue = QueueFactory.getDefaultQueue();
//		queue.add(
//				TaskOptions.Builder
//				.withUrl("/websocket/clock")
//				.method(TaskOptions.Method.POST)
//				.countdownMillis(1000));		
//	}

}