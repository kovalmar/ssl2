package pl.sitpres4.ssl2;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@RestController
public class getZipController {
    private static final String URL = "https://goldencopy.gleif.org/api/v2/golden-copies/publishes/lei2/latest.xml";

    @Value("${trust.store}")
    private Resource trustStore;

    @Value("${trust.store.password}")
    private String trustStorePassword;

    @GetMapping("/zip")
    public String zip() throws Exception {
        String url = URL;
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(new TrustSelfSignedStrategy())
                .build();
        SSLConnectionSocketFactory socketFactory =
                new SSLConnectionSocketFactory(sslContext);
        Registry<ConnectionSocketFactory> reg =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", socketFactory)
                        .build();
        HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .build();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse sslResponse = httpClient.execute(httpGet);
        return sslResponse.toString();
    }

    @GetMapping("/getzip")
    public ResponseEntity<String> whenGETanHTTPSResource_thenCorrectResponse() throws Exception {

        ResponseEntity<String> response = restTemplate().getForEntity(URL, String.class, Collections.emptyMap());
        return response; //.getBody();
    }

    @GetMapping(value="/download",produces="application/zip" )
    public ResponseEntity<?> download(HttpServletResponse response) throws Exception
    {
        ResponseEntity<String> responseEntity = restTemplate().getForEntity(URL, String.class, Collections.emptyMap());
        populateResponse(responseEntity,response);
        //Some Code...
        String filename = "latest.xml.zip";
        File file = new File("C:\\zip\\" + filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", String.format("inline; filename=\"" + filename + "\""));
        response.setHeader("responseType", "arraybuffer");
        response.setHeader("Content-Length", ""+file.length());

        return new ResponseEntity<InputStreamResource>(resource, HttpStatus.ACCEPTED);
    }

    RestTemplate restTemplate() throws Exception {
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(trustStore.getURL(), trustStorePassword.toCharArray())
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }

    public static void populateResponse(ResponseEntity<String> responseEntity, HttpServletResponse servletResponse)
            throws IOException {
        for (Map.Entry<String, List<String>> header : responseEntity.getHeaders().entrySet()) {
            String chave = header.getKey();
            for (String valor : header.getValue()) {
                servletResponse.addHeader(chave, valor);
            }
        }

        servletResponse.setStatus(responseEntity.getStatusCodeValue());
        servletResponse.getWriter().write(responseEntity.getBody());
    }
}
