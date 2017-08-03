package arqtest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Exception;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.junit.runner.RunWith;
import org.arquillian.cube.kubernetes.annotations.Named;
import org.arquillian.cube.kubernetes.annotations.PortForward;
import org.arquillian.cube.kubernetes.impl.requirement.RequiresKubernetes;
import org.arquillian.cube.requirement.ArquillianConditionalRunner;
import org.jboss.arquillian.test.api.ArquillianResource;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(ArquillianConditionalRunner.class)
@RequiresKubernetes
public class ArqIT { 

    /*
    Running mvn outside minikube, without @PortForward the injected URL is in the range http://10.0.0.48:80/
    which is not reachable from the host. So the test fails. @PortForward is essential for running the code 
    from a command line on a laptop. 
    */

    @Named("hello-service")
    // @PortForward
    @ArquillianResource
    URL url;

    @Test
    public void testHelloService() throws Exception { 

        System.out.println ("Got injected URL " + ((url == null) ? "null" : url.toString()));

        StringBuilder response = new StringBuilder();
        int retries = 5;
        while (response.toString().isEmpty() && retries > 0) { 
            try { 
                URLConnection urlc = url.openConnection();
                BufferedReader br = new BufferedReader (new InputStreamReader(urlc.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    response.append (line + "\n");
                }
                br.close();
            } catch (IOException iox) { 
                if (retries <= 1) { 
                    throw iox;
                }
                try { 
                    Thread.sleep (5000);
                } catch (InterruptedException ix) {}
            } finally { 
                retries--;
            }
        }

        String result = response.toString();
        System.out.println ("Got result " + result);
        assertTrue ("Expected Hello, got " + result, result.contains("Hello"));

    }
}