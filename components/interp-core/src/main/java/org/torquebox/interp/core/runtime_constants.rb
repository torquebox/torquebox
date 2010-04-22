
module JBoss
  
  class Version
    def initialize(major, minor, revision, tag)
      @major = major
      @minor = minor
      @revision = revision
      @tag = tag
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
  
  def self.setup_constants(application_name)
    @version          = Version.new( 6, 0, 0, 'M1' )
    @application_name = application_name
  end
  
end