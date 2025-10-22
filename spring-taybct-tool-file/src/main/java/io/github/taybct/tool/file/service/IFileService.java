package io.github.taybct.tool.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 文件服务<br>
 * 所有的实现类应该实现如下功能，上传，下载和删除
 *
 * @author xijieyin <br> 2022/8/4 15:03
 * @since 1.0.0
 */
public interface IFileService {

    /**
     * 上传文件<br>
     * 为了安全考虑，请不要把文件通过 nginx 代理的方式等方式让外面可以直接通过请求路径访问到，而是通过 get 方法去获取请求流回来，然后再做处理
     *
     * @param file 文件
     * @return 文件上传成功之后的路径
     */
    String upload(MultipartFile file) throws Exception;

    /**
     * 上传文件<br>
     * 这个方法可以指定要上传到哪个路径去
     *
     * @param file     文件
     * @param path     路径
     * @param filename 文件名
     * @return 上传后的路径
     * @throws Exception 异常
     */
    String upload(MultipartFile file, String path, String filename) throws Exception;

    /**
     * 根据文件请求路径删除文件
     *
     * @param path 文件
     * @return 删除是否成功
     */
    Boolean delete(String path) throws Exception;

    /**
     * 根据文件请求路径获取文件，这里返回的是流
     *
     * @param path 上传成功之后的请求路径
     * @return 返回流，外部拿到流自行处理
     */
    InputStream get(String path) throws Exception;

}
