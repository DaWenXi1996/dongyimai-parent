package com.offcn.content.controller;

import com.offcn.entity.Result;
import com.offcn.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Auther: qxb
 * @Date: 2020/12/21
 * @Description: 上传文件的控制器
 */
@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL ;

    @RequestMapping("/uploadFile")
    public Result uploadFile(MultipartFile file) {
        try {
            //1.获取文件的扩展名
            String fileName = file.getOriginalFilename();   //文件全名  文件名+扩展名 xxx.jpg
            String extName = fileName.substring(fileName.lastIndexOf(".") + 1);
            //2.创建上传文件的工具类
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:conf/fdfs_client.conf");
            //3.执行上传文件  path  =  group1/M00/00/01/wKgZhVmHINKADo__AAjlKdWCzvg874.jpg
            String path = fastDFSClient.uploadFile(file.getBytes(), extName);
            //4.返回文件存储路径
            return new Result(true,FILE_SERVER_URL+path);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }
}
