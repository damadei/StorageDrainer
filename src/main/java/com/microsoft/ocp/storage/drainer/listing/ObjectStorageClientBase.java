package com.microsoft.ocp.storage.drainer.listing;

public class ObjectStorageClientBase {

	//This is to avoid having a prefix x also get all files under /x...
	protected String ensurePrefixLooksLikeDirectory(String prefix) {
		if(prefix != null) {
			prefix = prefix.trim();

			if(!prefix.endsWith("/")) {
				prefix += "/";
			}
		}
		
		return prefix;
	}
	
}
