package com.together.ImagePreview;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImgurUploadService {

    @Value("${imgur.client-id}")
    private String clientId;

    public String uploadImage(MultipartFile file) throws IOException {
        String url = "https://api.imgur.com/3/image";
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost upload = new HttpPost(url);

            upload.setHeader("Authorization", "Client-ID " + clientId);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("image", file.getInputStream(), org.apache.http.entity.ContentType.DEFAULT_BINARY, file.getOriginalFilename());
            HttpEntity entity = builder.build();
            upload.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(upload)) {
                String responseString = EntityUtils.toString(response.getEntity());
                JSONObject json = new JSONObject(responseString);

                // imgur API 응답 예시: { data: { link: "..." }, success: true, status: 200 }
                if (json.getBoolean("success")) {
                    return json.getJSONObject("data").getString("link");
                } else {
                    throw new IOException("Imgur 업로드 실패: " + responseString);
                }
            }
        }
    }
}
