package org.torquebox.base.metadata;

public class EnvironmentMetaData {
	
	private String environmentName = "development";
	private boolean developmentMode = true;
	
	public EnvironmentMetaData() {
	}
	
	public void setDevelopmentMode(boolean developmentMode) {
		this.developmentMode = developmentMode;
	}
	
	public boolean isDevelopmentMode() {
		return this.developmentMode;
	}
	
	public void setEnvironmentName(String environmentName) {
		this.environmentName = environmentName;
	}
	
	public String getEnvironmentName() {
		return this.environmentName;
	}

}
