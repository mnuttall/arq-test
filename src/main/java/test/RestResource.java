package test;

import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("rest")
public class RestResource { 

    private static AtomicInteger count = new AtomicInteger(0);

    @GET
    @Consumes("text/plain")
    @Produces("text/plain")
    public String get () { 
        String result ="Hello GET, count=" + count.incrementAndGet();
        System.out.println (result);
        return result;
    }    

    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public String post (String payload) { 
        String result ="Hello POST, payload=`" + payload + "', count=" + count.incrementAndGet();
        System.out.println (result);
        return result;
    }    


}