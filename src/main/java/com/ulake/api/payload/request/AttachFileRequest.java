package com.ulake.api.payload.request;

import java.util.List;

public class AttachFileRequest {
    private List<String> file;

    public List<String> getFile(){
    	return this.file;
    }
    
    public void setFile(List<String> file) {
    	this.file = file;
    }
}
