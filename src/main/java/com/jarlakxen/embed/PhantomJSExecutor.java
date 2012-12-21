package com.jarlakxen.embed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class PhantomJSExecutor {

	public static String PHANTOMJS_DATA_FILE = "phantomjs/data.properties";
	public static String PHANTOMJS_TAR_TMP_FILE = System.getProperty("java.io.tmpdir") +"/" + "phantomjs.tmp.tar";
	
	private PhantomJSConfiguration configuration;
	private String phantomJSExecutablePath;
	
	public PhantomJSExecutor() {
		this(new PhantomJSConfiguration());
	}
	
	public PhantomJSExecutor(PhantomJSConfiguration configuration) {
		this.configuration = configuration;
		phantomJSExecutablePath = getPhantomJSExecutablePath();
	}
	
	private String getPhantomJSExecutablePath(){
		
		// Check if phantomjs is installed locally
		if( configuration.getCheckNativeInstallation() ){
			try {
				Process process = Runtime.getRuntime().exec("phantomjs --version");
		        process.waitFor();
		        
		        String processOutput = IOUtils.toString(process.getInputStream());

		        if( PhantomJSVersion.fromValue(processOutput.substring(0, 5)) != null ){
		        	return "phantomjs";
		        }
		        
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		if(!configuration.getVersion().isDownloadSopported()){
			throw new RuntimeException("Unsopported version for downloading!");
		}
		
		// Try download phantomjs
		try {
		
			Properties properties = new Properties();
			properties.load(getClass().getClassLoader().getResourceAsStream(PHANTOMJS_DATA_FILE));
						
			String os;
			if(configuration.getHostOs().indexOf("linux") >= 0){
				os = "linux";
			} else if(configuration.getHostOs().indexOf("win") >= 0){
				os = "win";
			} else if(configuration.getHostOs().indexOf("mac") >= 0){
				os = "macosx";
			} else {
				throw new RuntimeException("Unsopported operation system!");
			}
			
			String name = properties.getProperty(configuration.getVersion().getDescription() + "." + os + ".name");
			
			String architecture = configuration.getArchitecture().indexOf("64") >= 0 ? "x86_64" : "i686";
			
			if(os.equals("linux")){
				name = String.format(name, architecture);
			}
			
			URL downloadPath = new URL(configuration.getDownloadUrl() +  name);
			File phantomJsCompressedFile = new File(System.getProperty("java.io.tmpdir") +"/"+ name);
			FileUtils.copyURLToFile(downloadPath, phantomJsCompressedFile);
			

			ArchiveInputStream compressedInputStream = null;
            
            if(phantomJsCompressedFile.getName().endsWith(".zip")){
            	
            	compressedInputStream = new ZipArchiveInputStream(new FileInputStream(phantomJsCompressedFile));
            	
            } else if(phantomJsCompressedFile.getName().endsWith(".bz2")){
            
            	CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(phantomJsCompressedFile));

            	FileOutputStream out = new FileOutputStream(PHANTOMJS_TAR_TMP_FILE);
            	
            	IOUtils.copy(bzIn, out);

            	bzIn.close();
            	out.close();
            	
            	compressedInputStream = new TarArchiveInputStream(new FileInputStream(PHANTOMJS_TAR_TMP_FILE));
            	
            } else if(phantomJsCompressedFile.getName().endsWith(".gz")){
                
            	CompressorInputStream bzIn = new GzipCompressorInputStream(new FileInputStream(phantomJsCompressedFile));

            	FileOutputStream out = new FileOutputStream(PHANTOMJS_TAR_TMP_FILE);
            	
            	IOUtils.copy(bzIn, out);

            	bzIn.close();
            	out.close();
            	
            	compressedInputStream = new TarArchiveInputStream(new FileInputStream(PHANTOMJS_TAR_TMP_FILE));
            }
            
			ArchiveEntry entry;
			while ((entry = compressedInputStream.getNextEntry()) != null) {
				if(entry.getName().endsWith("/bin/phantomjs")){
					entry.
					
				}
			}
            
            compressedInputStream.close();
            
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		return "";
	}
	
	public PhantomJSConfiguration getConfiguration() {
		return configuration;
	}
}
