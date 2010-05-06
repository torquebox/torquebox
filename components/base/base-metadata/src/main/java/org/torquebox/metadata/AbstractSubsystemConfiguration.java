package org.torquebox.metadata;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSubsystemConfiguration implements SubsystemConfiguration {
	
	private String subsystemName;
	private List<String> loadPaths = new ArrayList<String>();
	
	public AbstractSubsystemConfiguration() {
		
	}
	
	/** Add an element to the {@code $LOAD_PATH} variable.
	 * 
	 * <p>The {@code loadPath} should be relative to the application's
	 * root directory.</p>
	 * 
	 * @param loadPath The load-path element to add.
	 */
	public void addLoadPath(String loadPath) {
		this.loadPaths.add( loadPath );
	}
	
	public List<String> getLoadPaths() {
		return this.loadPaths;
	}
	
	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}
	
	public String getSubsystemName() {
		return this.subsystemName;
	}
	
}