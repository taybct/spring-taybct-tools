package io.github.taybct.tool.file.util;

import cn.hutool.core.codec.Base64;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import io.github.taybct.tool.file.config.OSSConfig;
import io.github.taybct.tool.file.proxy.OSSDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 阿里云oss 图片上传
 *
 * @author LiXiaoMing <br> 2021/3/23 16:30
 */
@Slf4j
public class OSSUtil {

    private static final Map<String, OSS> clientPool = new ConcurrentHashMap<>();

    /**
     * oss 配置
     */
    private OSSConfig config;
    /**
     * 代理地址
     */
    private String proxyUrl;
    /**
     * oss 客户端
     */
    private OSS ossClient;

    private static OSSUtil ossUtil;

    private void setConfig(OSSConfig config) {
        this.config = config;
    }

    private String setProxyUrl(String proxyUrl) {
        if (ObjectUtils.isEmpty(proxyUrl)) {
            return proxyUrl;
        }
        if (proxyUrl.lastIndexOf("/") == (proxyUrl.length() - 1)) {
            proxyUrl = proxyUrl.substring(0, proxyUrl.lastIndexOf("/"));
        }
        this.proxyUrl = proxyUrl;
        return proxyUrl;
    }

    private void setClient(OSS client) {
        this.ossClient = client;
    }

    private static OSS createClient(String endpoint, String accessKeyId, String accessKeySecret) {
        OSS client = clientPool.get(accessKeyId);
        if (ObjectUtils.isEmpty(client)) {
            client = new OSSClient(endpoint, accessKeyId, accessKeySecret);
            clientPool.put(accessKeyId, client);
        }
        return client;
    }

    /**
     * 初始化
     *
     * @param config 配置
     */
    public static OSSUtil ini(OSSConfig config) {
        if (ObjectUtils.isEmpty(ossUtil)) {
            ossUtil = new OSSUtil();
        }
        config.setProxyUrl(ossUtil.setProxyUrl(config.getProxyUrl()));
        ossUtil.setClient(createClient(config.getEndpoint(), config.getAccessKeyId(), config.getAccessKeySecret()));
        ossUtil.setConfig(config);
        return ossUtil;
    }

    /**
     * 上传
     *
     * @param filePath 路径
     * @param stream   流
     */
    public PutObjectResult upload(String filePath, InputStream stream) throws IOException {
        return upload(config.getBucketName(), filePath, stream);
    }

    /**
     * oss 上传
     *
     * @param bucketName 桶
     * @param filePath   路径
     * @param stream     流
     * @return PutObjectResult
     */
    public PutObjectResult upload(String bucketName, String filePath, InputStream stream) throws IOException {
        if (StringUtils.isNotEmpty(this.proxyUrl)) {
            OSSDTO ossdto = new OSSDTO();
            BeanUtils.copyProperties(config, ossdto);
            ossdto.setProxyUrl(null);
            ossdto.setFilePath(filePath);
            ossdto.setBucketName(bucketName);

            // 第一种方式，把 inputStream 转成 base64 然后到了 proxy 那边再转过来
            ossdto.setStreamBase64(Base64.encodeUrlSafe(IOUtils.toByteArray(stream)));

            // 第二种方式，创建临时文件，用于上传，可以使用流的形式
//			File file = new File(config.getTempPath());
//			if (!file.exists()){
//				file.mkdirs();
//			}

//			String fileName = String.format("%s-%s",UUID.randomUUID(),filePath.substring(filePath.lastIndexOf("/")+1));
//			String tmpFile = String.format("%s/%s",config.getTempPath(),fileName);
//			FileUtil.outputFile(tmpFile,stream);

            String response = HttpClientUtil.proxy(this.proxyUrl, "oss/upload", JSONObject.toJSONString(ossdto, JSONWriter.Feature.WriteMapNullValue));

//			file = new File(tmpFile);
//			if(file.exists()){
//				file.delete();
//			}

            return JSONObject.parseObject(response, PutObjectResult.class);
        }
        return ossClient.putObject(bucketName, filePath, stream);
    }

    /**
     * 删除文件
     *
     * @param objectName 文件名
     */
    public void delete(String objectName) {
        delete(config.getBucketName(), objectName);
    }

    /**
     * 删除文件
     *
     * @param bucketName 桶名
     * @param objectName 文件名
     */
    public void delete(String bucketName, String objectName) {
        if (StringUtils.isNotEmpty(this.proxyUrl)) {
            OSSDTO ossdto = new OSSDTO();
            BeanUtils.copyProperties(config, ossdto);
            ossdto.setProxyUrl(null);
            ossdto.setBucketName(bucketName);
            ossdto.setObjectName(objectName);
            String response = HttpClientUtil.proxy(this.proxyUrl, "oss/delete", JSONObject.toJSONString(ossdto, JSONWriter.Feature.WriteMapNullValue));
            return;
        }
        ossClient.deleteObject(bucketName, objectName);

    }

    /**
     * 获取对象
     *
     * @param objectName 对象名
     */
    public String get(String objectName) {
        return get(config.getBucketName(), objectName);
    }

    /**
     * 获取对象
     *
     * @param bucketName 桶名
     * @param objectName 对象名
     */
    public String get(String bucketName, String objectName) {
        if (StringUtils.isNotEmpty(this.proxyUrl)) {
            OSSDTO ossdto = new OSSDTO();
            BeanUtils.copyProperties(config, ossdto);
            ossdto.setProxyUrl(null);
            ossdto.setBucketName(bucketName);
            ossdto.setObjectName(objectName);
            return HttpClientUtil.proxy(this.proxyUrl, "oss/get", JSONObject.toJSONString(ossdto, JSONWriter.Feature.WriteMapNullValue));
        }
        try {
            return Base64.encodeUrlSafe(IOUtils.toByteArray(ossClient.getObject(bucketName, objectName).getObjectContent()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
