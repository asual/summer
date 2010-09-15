package com.asual.summer.sample.websocket;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.jersey.JerseyBroadcaster;
import org.atmosphere.jersey.SuspendResponse;

import com.sun.jersey.spi.resource.Singleton;

@Path("/clock")
@Singleton
public class Clock {
  
    private Date date = new Date();
    private Timer timer = new Timer();
    private Broadcaster topic = new JerseyBroadcaster("clock");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z (EEE, dd MMM yyyy)");
 
    public Clock() {
        timer.schedule(new TimerTask() {
            public void run() {
                date.setTime(System.currentTimeMillis());
                topic.broadcast(dateFormat.format(date));
            }
        }, new Date(), 1000);
    }
    
    @GET
    public SuspendResponse<String> subscribe() {
        return new SuspendResponse.SuspendResponseBuilder<String>()
            .broadcaster(topic)
            .outputComments(true)
            .build();
    }

}