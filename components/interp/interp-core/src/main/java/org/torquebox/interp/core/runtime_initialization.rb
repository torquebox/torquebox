

module TorqueBox

  class VersionSpec
    attr_reader :major, :minor, :revision, :tag, :str

    def initialize(str)
      @str = str
      @major, @minor, @revision, @tag = str.split('.')
      self.freeze 
    end

    def to_s
      @str
    end
  end

  def self.versions
    @versions ||= {}
  end

  def self.version
    self.versions[:torquebox]
  end

  def self.define_versions(logger=nil)
    self.versions[:torquebox] = VersionSpec.new( "${project.version}" )
    self.versions[:jbossas]   = VersionSpec.new( "${version.jbossas}" )
    self.versions[:jruby]     = VersionSpec.new( "${version.jruby}" )

    unless ( logger.nil? )
      logger.info( "TorqueBox...#{self.versions[:torquebox]}" )
      logger.info( "JBossAS.....#{self.versions[:jbossas]}" )
      logger.info( "JRuby.......#{self.versions[:jruby]}" )
    end
  end

  def self.application_name=(application_name)
    @application_name = application_name
  end

  def self.application_name()
    @application_name
  end

end

