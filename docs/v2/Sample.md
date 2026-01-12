This is a sample to connect and download files from COS, for your reference while building COSExtractor.

```java
package com.chinalin.ficc.quant.aps.cos;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.AnonymousCOSCredentials;
import com.qcloud.cos.endpoint.EndpointBuilder;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;

import java.io.File;

public class CosETLService {

      public static void main(String[] args) {
        AnonymousCOSCredentials anonymousCOSCredentials = new AnonymousCOSCredentials();
        String region = "sz";
        String domain = "chinalionscos.cn";
        SelfDefinedEndpointBuilder builder = new SelfDefinedEndpointBuilder(region, domain);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setEndpointBuilder(builder);
        clientConfig.setHttpProtocol(HttpProtocol.http);

        COSClient client = new COSClient(anonymousCOSCredentials, clientConfig);

        String bucketName = "ficc-quote-prod-1255000016";
        String rootPrefix = "";
        listObjects(bucketName, client, rootPrefix, "");
    }

    private static void listObjects(String bucketName, COSClient client, String prefix, String s) {
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(bucketName);
            listObjectsRequest.setPrefix(prefix);
            listObjectsRequest.setMaxKeys(1000);
            listObjectsRequest.setDelimiter("/");
            ObjectListing objectListing = client.listObjects(listObjectsRequest);
            for (String pref : objectListing.getCommonPrefixes()) {
                System.out.println(s + pref);
                listObjects(bucketName, client, pref, s + "--");
            }

        } catch (CosServiceException serviceException) {
            serviceException.printStackTrace();
        } catch (CosClientException clientException) {
            clientException.printStackTrace();
        }
    }

    private static void downloadFile(String bucketName, String key, COSClient client, String path) {
        try {
            File downFile = new File(path);
            GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
            ObjectMetadata obj = client.getObject(getObjectRequest, downFile);
        } catch (CosServiceException serviceException) {
            serviceException.printStackTrace();
        } catch (CosClientException clientException) {
            clientException.printStackTrace();
        }
    }

    private static class SelfDefinedEndpointBuilder implements EndpointBuilder {
        private String region;
        private String domain;

        public SelfDefinedEndpointBuilder(String region, String domain) {
            super();
            this.region = Region.formatRegion(new Region(region));
            this.domain = domain;
        }

        @Override
        public String buildGeneralApiEndpoint(String bucketName) {
            String endpoint = String.format("%s.%s", this.region, this.domain);
            return String.format("%s.%s", bucketName, endpoint);
        }

        @Override
        public String buildGetServiceApiEndpoint() {
            return String.format("%s.%s", this.region, this.domain);
        }
    }
}

```