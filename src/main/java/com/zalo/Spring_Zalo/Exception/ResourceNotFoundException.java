package com.zalo.Spring_Zalo.Exception;

import lombok.Data;

@Data
public class ResourceNotFoundException extends RuntimeException{
    String resourceName;
    String fileName;
    String fileValue;

    public ResourceNotFoundException(String resourceName, String fileName,Integer userId) {
        super(String.format("%s not found with  %s : %s ", resourceName, fileName,userId));
        this.resourceName = resourceName;
        this.fileName= fileName;
        this.fileValue = fileValue;
    }
}
