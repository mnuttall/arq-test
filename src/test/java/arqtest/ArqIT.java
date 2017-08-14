package arqtest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.Exception;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;

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

    Test passes on minikube with @PortForward commented out. 
    */

    @Named("hello-service")
    @PortForward
    @ArquillianResource
    URL url;

    enum Method {GET, PUT, POST, DELETE};

    @Test
    public void testHelloService() throws Exception { 
        System.out.println ("Got injected URL " + ((url == null) ? "null" : url.toString()));
        String response = getContent (url, Method.GET, null);
        String result = response.toString();
        System.out.println ("Got result " + result);
        assertTrue ("Expected Hello, got " + result, result.contains("Hello"));

    }
    
/* 
This next test is relevant to a second problem relating to JAX-RS. Not relevant to @PortForward

    @Named("rest-service")
    @PortForward
    @ArquillianResource
    URL resturl;

    @Test
    public void testPost() throws Exception { 
        String target = resturl.toURI().toString() + "/rest";
        URL targetURL = new URI(target).toURL();
        String result = getContent(targetURL, Method.POST, "payload");

        System.out.println ("testPost got result " + result);
        assertTrue ("Expected payload", result.contains("payload"));
        assertTrue ("Expected count", result.contains("count"));
    }
*/

    private String getContent (URL url, Method method, String payload ) throws IOException { 
        StringBuilder response = new StringBuilder();
        int retries = 5;
        while (response.toString().isEmpty() && retries > 0) { 
            try { 
                Writer writer = null;
                BufferedReader reader;
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("content-type", "text/plain");
                urlc.setRequestMethod(method.toString());

                if (method == Method.PUT || method == Method.POST) { 
                    urlc.setDoOutput(true);
                    writer = new OutputStreamWriter(urlc.getOutputStream());
                    writer.write(payload);
                    writer.flush();
                }
                reader = new BufferedReader (new InputStreamReader(urlc.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append (line + "\n");
                }
                if (writer != null) writer.close();
                reader.close();
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
        return response.toString();
    }
}