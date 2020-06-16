package com.jlefebure.minio.example;

import com.google.api.client.util.IOUtils;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;

import afu.org.checkerframework.checker.units.qual.min;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidArgumentException;
import io.minio.errors.InvalidBucketNameException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.NoResponseException;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/files")
public class MainController {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);
    @Autowired
    private MinioService minioService;
    @Autowired
    private MinioClient minioClient;
    
    @GetMapping
    public List<Item> testMinio() {
    	
    	List<Item> listMinio = null;
    	LOGGER.info("Liet ke danh sach file");
        if(minioService.list().size()==0)
        {
        	LOGGER.warn("Chua co file nao trong bucket");
            
        }
        try {
        listMinio = minioService.list();
        }catch (Exception e) {
			// TODO: handle exception
        	e.printStackTrace();   	
		}
        if(listMinio.size() >10)
        {
        listMinio =listMinio.subList(listMinio.size()-10,listMinio.size());
        }
         return listMinio;
        }

    @GetMapping("/delete/{file}")  
    public int deleteFile(@PathVariable("file") Path file,HttpServletResponse response) throws  IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidBucketNameException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, InvalidArgumentException, InvalidResponseException, XmlPullParserException
    {
    	
        int ret=0; 
    //	Path path = Path.of(file.getOriginalFilename());
    	List<Item> listMinio = minioService.list();
    	for (Item item : listMinio) {
    		if(item.objectName().equals(file.toString()))
    		{
    			ret=1;
    		}
			
		}
    	if(ret==1)
    	{
        LOGGER.info("Xoa file:"+file);
        try {  
    	minioClient.removeObject("12345678",file.toString());
        
        }catch (io.minio.errors.MinioException e) {
			// TODO: handle exception
		 System.out.println("Loi file ko tong tai"+e);
		 
        }
        LOGGER.info("Da xoa:"+file);
    	} 
    	else 
    	{
    		LOGGER.info("File khong ton tai");

    	}
        return ret; 
       
    	
    }
    //API dem so luong ojbect trong package 
    @RequestMapping("/count")
    public int countFile()
    {
        LOGGER.info("Thong ke so luong file");
       
    	
        return minioService.list().size();	
    
    }
    //API tinh kich thuoc file 
    @RequestMapping("/size")
    public double sizeFile() throws InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, NoResponseException, ErrorResponseException, InternalException, IOException, XmlPullParserException
    {
    	double sizeBucket=0;
    	Iterable<Result<Item>> results=null;
		try {
			results = minioClient.listObjects("12345678");
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	for (Result<Item> result : results) {
    	  Item item = result.get();
    	  System.out.println(item.lastModified() + ", " + item.objectSize() + ", " + item.objectName());
          sizeBucket+=item.objectSize();  
    	}
    	return sizeBucket/1000/1000;
    }

    @GetMapping("/{object}")
    public void getObject(@PathVariable("object") String object, HttpServletResponse response) throws MinioException, IOException {
        InputStream inputStream = minioService.get(Path.of(object));
        InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

        // Set the content type and attachment header.
        response.addHeader("Content-disposition", "attachment;filename=" + object);
        response.setContentType(URLConnection.guessContentTypeFromName(object));
        LOGGER.info("Thuc hien tai file:"+object);
        // Copy the stream to the response's output stream.
        IOUtils.copy(inputStream, response.getOutputStream());
        response.flushBuffer();
    }

    @PostMapping
    public void addAttachement(@RequestParam("file") MultipartFile file) {
        Path path = Path.of(file.getOriginalFilename());
        Map<String, String> header = new HashMap<>();
        header.put("X-Incident-Id", "C918371984");
        try {
        	LOGGER.info("Thực hiện upload file:"+path);
        	minioService.upload(path, file.getInputStream(), file.getContentType(), header);
        } catch (MinioException e) {
           throw new IllegalStateException("The file cannot be upload on the internal storage. Please retry later", e);
        } catch (IOException e) {
            throw new IllegalStateException("The file cannot be read", e);
        }
    }
}
