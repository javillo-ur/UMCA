package model;

import jakarta.xml.bind.annotation.*;

@XmlRootElement
public class ClientConfig {
	private String url;
	private int port;
	
	@XmlElement(name = "ServerURL")
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@XmlElement(name = "ServerPort")
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
}
