package com.ulake.api.payload.request;

import java.util.Set;

public class AddFileRequest {
    private Set<String> file;

    public Set<String> getFile(){
    	return this.file;
    }
    
    public void setFile(Set<String> file) {
    	this.file = file;
    }	
}
