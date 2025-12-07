package com.upao.recicla.blockchain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Servicio IPFS para producción usando Pinata
 * Maneja upload de evidencias fotográficas y metadata JSON
 */
@Service
@Slf4j
public class IPFSService {

    @Value("${ipfs.pinata.api-key}")
    private String pinataApiKey;

    @Value("${ipfs.pinata.secret-key}")
    private String pinataSecretKey;

    private static final String PINATA_API_URL = "https://api.pinata.cloud/pinning/pinFileToIPFS";
    private static final String PINATA_JSON_URL = "https://api.pinata.cloud/pinning/pinJSONToIPFS";
    private static final String PINATA_GATEWAY = "https://gateway.pinata.cloud/ipfs/";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IPFSService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Sube una imagen de evidencia a IPFS vía Pinata
     * 
     * @param file     Imagen de la actividad de reciclaje
     * @param metadata Metadata adicional para el pin
     * @return Hash IPFS (CID) de la evidencia
     */
    public String uploadEvidencia(MultipartFile file, String metadata) throws IOException {
        log.info("📤 Subiendo evidencia a Pinata IPFS: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getOriginalFilename(),
                        RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType())))
                .addFormDataPart("pinataMetadata", metadata)
                .build();

        Request request = new Request.Builder()
                .url(PINATA_API_URL)
                .addHeader("pinata_api_key", pinataApiKey)
                .addHeader("pinata_secret_api_key", pinataSecretKey)
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("❌ Error en Pinata API: {} - {}", response.code(), errorBody);
                throw new IOException("Pinata upload failed: " + errorBody);
            }

            JsonNode jsonResponse = objectMapper.readTree(response.body().string());
            String ipfsHash = jsonResponse.get("IpfsHash").asText();

            log.info("✅ Evidencia subida a IPFS: {}", ipfsHash);
            log.info("🌐 URL pública: {}{}", PINATA_GATEWAY, ipfsHash);

            return ipfsHash;

        } catch (IOException e) {
            log.error("❌ Error comunicando con Pinata", e);
            throw new IOException("Error subiendo evidencia a IPFS: " + e.getMessage(), e);
        }
    }

    /**
     * Sube metadata JSON a IPFS vía Pinata
     * 
     * @param metadataJson JSON con información de la actividad
     * @return Hash IPFS del metadata
     */
    public String uploadMetadata(String metadataJson) throws IOException {
        log.info("📤 Subiendo metadata JSON a Pinata IPFS: {} bytes", metadataJson.length());

        JsonNode jsonContent = objectMapper.readTree(metadataJson);

        RequestBody requestBody = RequestBody.create(
                objectMapper.writeValueAsString(Map.of("pinataContent", jsonContent)),
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(PINATA_JSON_URL)
                .addHeader("pinata_api_key", pinataApiKey)
                .addHeader("pinata_secret_api_key", pinataSecretKey)
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("❌ Error en Pinata JSON API: {} - {}", response.code(), errorBody);
                throw new IOException("Pinata JSON upload failed: " + errorBody);
            }

            JsonNode jsonResponse = objectMapper.readTree(response.body().string());
            String ipfsHash = jsonResponse.get("IpfsHash").asText();

            log.info("✅ Metadata JSON subida a IPFS: {}", ipfsHash);

            return ipfsHash;

        } catch (IOException e) {
            log.error("❌ Error comunicando con Pinata JSON API", e);
            throw new IOException("Error subiendo metadata a IPFS: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera contenido desde IPFS vía gateway de Pinata
     * 
     * @param ipfsHash Hash IPFS (CID)
     * @return Contenido del archivo
     */
    public byte[] retrieveContent(String ipfsHash) throws IOException {
        String hash = ipfsHash.replace("ipfs://", "");
        String url = PINATA_GATEWAY + hash;

        log.info("📥 Recuperando contenido de IPFS: {}", hash);

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("❌ Error recuperando de IPFS: {}", response.code());
                throw new IOException("IPFS retrieval failed: " + response.code());
            }

            byte[] content = response.body().bytes();
            log.info("✅ Contenido recuperado: {} bytes", content.length);

            return content;

        } catch (IOException e) {
            log.error("❌ Error accediendo a gateway IPFS", e);
            throw new IOException("Error recuperando contenido de IPFS: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un hash IPFS es válido (CIDv0 o CIDv1)
     */
    public boolean isValidIPFSHash(String ipfsHash) {
        if (ipfsHash == null || ipfsHash.isEmpty()) {
            return false;
        }

        String hash = ipfsHash.replace("ipfs://", "");

        // CIDv0: Qm... (base58, 46 caracteres)
        boolean isCIDv0 = hash.startsWith("Qm") && hash.length() == 46;

        // CIDv1: bafy... (base32, variable length)
        boolean isCIDv1 = hash.startsWith("bafy") && hash.length() >= 50;

        return isCIDv0 || isCIDv1;
    }

    /**
     * Obtiene la URL pública del gateway para un hash IPFS
     */
    public String getPublicUrl(String ipfsHash) {
        String hash = ipfsHash.replace("ipfs://", "");
        return PINATA_GATEWAY + hash;
    }
}
