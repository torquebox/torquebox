
module JBoss
  
  class Version
    def initialize(version)
      @major = version.getMajor()
      @minor = version.getMinor()
      @revision = version.getRevision()
      @tag = version.getTag()
    end
    
    def major
      @major
    end
    
    def minor
      @minor
    end
    
    def revision
      @revision
    end
    
    def tag
      @tag 
    end
    
    def to_s
      "#{major}.#{minor}.#{revision}.#{tag}"
    end
    
  end
  
  def self.version
    @version
  end
  
  def self.application_name
    @application_name
  end
  
  def self.setup_constants(version, application_name)
    @version          = Version.new( version  )
    @application_name = application_name
  end
  
end

puts "loaded JBoss runtime_constants.rb"