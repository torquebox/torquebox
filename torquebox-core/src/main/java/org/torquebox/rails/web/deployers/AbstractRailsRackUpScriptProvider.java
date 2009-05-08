package org.torquebox.rails.web.deployers;

public abstract class AbstractRailsRackUpScriptProvider implements RailsRackUpScriptProvider {

	private int majorVersion;
	private int minorVersion;
	private int tinyVersion;

	protected AbstractRailsRackUpScriptProvider(int majorVersion, int minorVersion, int tinyVersion) {
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.tinyVersion = tinyVersion;
	}

	public int getMajorVersion() {
		return this.majorVersion;
	}

	public int getMinorVersion() {
		return this.minorVersion;
	}

	public int getTinyVersion() {
		return this.tinyVersion;
	}

	public int compareTo(RailsRackUpScriptProvider that) {
		if (this.getMajorVersion() > that.getMajorVersion()) {
			return 1;
		}
		if (this.getMajorVersion() < that.getMajorVersion()) {
			return -1;
		}
		if (this.getMinorVersion() > that.getMinorVersion()) {
			return 1;
		}
		if (this.getMinorVersion() < that.getMinorVersion()) {
			return -1;
		}
		if (this.getTinyVersion() > that.getTinyVersion()) {
			return 1;
		}
		if (this.getTinyVersion() < that.getTinyVersion()) {
			return -1;
		}
		return 0;
	}
	
	public String toString() {
		return "[RailsRackUpScriptProvider: major=" + this.majorVersion + "; minor=" + this.minorVersion + "; tiny="+ this.tinyVersion + "]";
	}

}
