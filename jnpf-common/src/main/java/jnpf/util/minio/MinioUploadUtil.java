package jnpf.util.minio;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import jnpf.util.FileUtil;
import jnpf.util.ServletUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * minio文件上传工具类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-04-01
 */
@Component
@Slf4j
public class MinioUploadUtil {
    @Resource(name = "minioClient")
    private MinioClient minioClient;

    /**
     * 上传文件
     *
     * @param file
     * @param bucketName
     * @param fileName
     * @return
     */
    public void uploadFile(MultipartFile file, String bucketName, String fileName) {
        //判断文件是否为空
        if (null == file || 0 == file.getSize()) {
            log.error("文件不能为空");
        }
        //判断存储桶是否存在
        bucketExists(bucketName);
        //文件名
        String originalFilename = file.getOriginalFilename();
        //新的文件名 = 存储桶文件名_时间戳.后缀名
        assert originalFilename != null;
        //开始上传
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                            file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 上传文件（可以传空）    数据备份使用
     * @param filePath
     * @param bucketName
     * @param fileName
     * @throws IOException
     */
    public void uploadFiles(String filePath, String bucketName, String fileName) throws IOException {
        MultipartFile file = FileUtil.createFileItem(new File(filePath));
        //开始上传
        try {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                            file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param fileName   文件名
     * @param bucketName 桶名（文件夹）
     * @return
     */
    public void downFile(String fileName, String bucketName) {
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            //下载文件
            HttpServletResponse response = ServletUtil.getResponse();
            HttpServletRequest request = ServletUtil.getRequest();
            try {
                @Cleanup BufferedInputStream bis = new BufferedInputStream(inputStream);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("text/plain");
                if(fileName.contains(".svg")){
                    response.setContentType("image/svg+xml");
                }
                //编码的文件名字,关于中文乱码的改造
                String codeFileName = "";
                String agent = request.getHeader("USER-AGENT").toLowerCase();
                if (-1 != agent.indexOf("msie") || -1 != agent.indexOf("trident")) {
                    //IE
                    codeFileName = URLEncoder.encode(fileName, "UTF-8");
                } else if (-1 != agent.indexOf("mozilla")) {
                    //火狐，谷歌
                    codeFileName = new String(fileName.getBytes("UTF-8"), "iso-8859-1");
                } else {
                    codeFileName = URLEncoder.encode(fileName, "UTF-8");
                }
                response.setHeader("Content-Disposition", "attachment;filename=" + new String(codeFileName.getBytes(), "utf-8"));
                @Cleanup OutputStream os = response.getOutputStream();
                int i;
                byte[] buff = new byte[1024 * 8];
                while ((i = bis.read(buff)) != -1) {
                    os.write(buff, 0, i);
                }
                os.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            if (inputStream!=null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 返回图片
     *
     * @param fileName   文件名
     * @param bucketName 桶名（文件夹）
     * @return
     */
    public void dowloadMinioFile(String fileName, String bucketName) {
        InputStream inputStream = null;
        try {
            inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
            @Cleanup OutputStream outputStream =null;
            //读取指定路径下面的文件
            outputStream = new BufferedOutputStream(ServletUtil.getResponse().getOutputStream());
            //创建存放文件内容的数组
            byte[] buff = new byte[1024];
            //所读取的内容使用n来接收
            int n;
            //当没有读取完时,继续读取,循环
            while ((n = inputStream.read(buff)) != -1) {
                //将字节数组的数据全部写入到输出流中
                outputStream.write(buff, 0, n);
            }
            //强制将缓存区的数据进行输出
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取資源
     *
     * @param fileName
     * @param bucketName
     */
    public String getFile(String fileName, String bucketName) {
        String objectUrl = null;
        try {
            objectUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(fileName)
                            .build());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return objectUrl;
    }

    /**
     * 下载文件
     *
     * @param fileName   文件名称
     * @param bucketName 存储桶名称
     * @return
     */
    public InputStream downloadMinio(String fileName, String bucketName) {
        try {
            @Cleanup InputStream stream =
                    minioClient.getObject(
                            GetObjectArgs.builder().bucket(bucketName).object(fileName).build());
            return stream;
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
            return null;
        }
    }

    /**
     * 获取全部bucket
     *
     * @return
     */
    public List<String> getAllBuckets() throws Exception {
        return minioClient.listBuckets().stream().map(Bucket::name).collect(Collectors.toList());
    }

    /**
     * 根据bucketName删除信息
     *
     * @param bucketName bucket名称
     */
    public void removeBucket(String bucketName) throws Exception {
        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    /**
     * 删除一个对象
     *
     * @param name
     * @return
     */
    public boolean removeFile(String bucketName, String name) {
        boolean isOK = true;
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(name).build());
        } catch (Exception e) {
            e.printStackTrace();
            isOK = false;
        }
        return isOK;
    }

    /**
     * 检查存储桶是否已经存在(不存在不创建)
     *
     * @param name
     * @return
     */
    public boolean bucketExists(String name) {
        boolean isExist = false;
        try {
            isExist = minioClient.bucketExists(getBucketExistsArgs(name));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isExist;
    }

    /**
     * 检查存储桶是否已经存在(不存在则创建)
     *
     * @param name
     * @return
     */
    public void bucketExistsCreate(String name) {
        try {
            minioClient.bucketExists(getBucketExistsArgs(name));
            minioClient.makeBucket(getMakeBucketArgs(name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 代码生成器下载代码
     *
     * @param filePath      文件路径
     * @param bucketName    存储桶
     * @param objectName    文件夹名称
     * @return
     */
    public boolean putFolder(String filePath, String bucketName, String objectName){
        boolean flag = false;
        try {
            //判断文件夹是否存在
            if (!FileUtil.fileIsExists(filePath)) {
                return false;
            }
            //压缩文件后上传到minio
            FileUtil.toZip(filePath + ".zip", true, filePath);
            MultipartFile multipartFile = FileUtil.createFileItem(new File(filePath + ".zip"));
            //上传到minio
            uploadFile(multipartFile,bucketName,objectName + ".zip");
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 通过流下载文件
     *
     * @param bucketName
     * @param filePath
     * @param objectName
     */
    public void streamToDown(String bucketName, String filePath, String objectName){
        try {
            @Cleanup InputStream stream =
                    minioClient.getObject(
                            GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
            FileUtil.writeFile(stream, filePath, objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
    }

    /**
     * 获取存储桶下所有文件
     *
     * @param bucketName    存储桶名
     * @return
     */
    public List getFileList(String bucketName) {
        List<Item> list = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                list.add(item);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return list;
    }

    /**
     * 获取存储桶下所有文件
     *
     * @param bucketName    存储桶名
     * @param bucketName    桶下的文件夹
     * @return
     */
    public List getFileList(String bucketName,String type) {
        List<Item> list = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).prefix(type).recursive(true).build());
            for (Result<Item> result : results) {
                Item item = result.get();
                list.add(item);
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return list;
    }

    /**
     * 拷贝文件
     *
     * @param bucketName bucket名称
     * @param objectName 文件名称
     * @param copyToBucketName 目标bucket名称
     * @param copyToObjectName 目标文件名称
     */
    public void copyObject(String bucketName, String objectName, String copyToBucketName, String copyToObjectName){
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder().bucket(bucketName).object(objectName).build())
                            .bucket(copyToBucketName)
                            .object(copyToObjectName)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * String转MakeBucketArgs
     *
     * @param name
     * @return
     */
    public static MakeBucketArgs getMakeBucketArgs(String name) {
        return MakeBucketArgs.builder().bucket(name).build();
    }

    /**
     * String转BucketExistsArgs
     *
     * @param name
     * @return
     */
    public static BucketExistsArgs getBucketExistsArgs(String name) {
        return BucketExistsArgs.builder().bucket(name).build();
    }

    /**
     * String转SetBucketPolicyArgs
     *
     * @param name
     * @return
     */
    public static SetBucketPolicyArgs getSetBucketPolicyArgs(String name) {
        return SetBucketPolicyArgs.builder().bucket(name).build();
    }

    /**
     * 通过流下载文件
     *
     * @param bucketName
     * @param filePath
     * @param objectName
     */
    public void downToLocal(String bucketName, String filePath, String objectName){
        try {
            @Cleanup InputStream stream =
                    minioClient.getObject(
                            GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
            FileUtil.write(stream, filePath, objectName);
        } catch (Exception e) {
            e.printStackTrace();
            log.info(e.getMessage());
        }
    }

}
