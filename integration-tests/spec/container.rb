require 'jruby/core_ext'
require 'fileutils'

module ArquillianMethods

  # Either a :path option or a block is required.  If :path is
  # non-nil, it will be packaged as a META-INF resource in a
  # JavaArchive.  Otherwise, the archive is assumed to be returned
  # from the block.
  def deploy options = {}, &block
    @run_mode = options.fetch(:run_mode, :client)
    paths = options[:path] || options[:paths]
    @ruby_1_9 = options[:ruby_1_9]
    @ruby_1_9 = (RUBY_VERSION =~ /^1\.9/) if @ruby_1_9.nil?
    paths = duplicate_paths_for_1_9( paths ) if @ruby_1_9
    archive_name = options[:name]
    add_class_annotation( org.jboss.arquillian.api.Run => { "value" => run_mode } )
    metaclass = class << self
                  add_method_signature( "create_deployment", [org.jboss.shrinkwrap.api.spec.JavaArchive] )
                  add_method_annotation( "create_deployment", org.jboss.arquillian.api.Deployment => {} )
                  self
                end
    metaclass.send(:define_method, :create_deployment) do
      paths ? create_archive_for_resources(paths, archive_name) : block.call
    end
  end

  def duplicate_paths_for_1_9(paths)
    paths = [ paths ].flatten
    paths.inject( [] ) do |accum, path|
      puts "Setting up #{path} to run in 1.9 mode"
      ext = File.extname( path )
      path_19 = path[0..-(ext.length+1)] + "-19" + ext
      accum << path_19
      full_19_path = File.join( 'target', 'test-classes', path_19 )
      unless File.exists?( full_19_path )
        FileUtils.cp( File.join( 'target', 'test-classes', path ), full_19_path )
         File.open( full_19_path, 'a' ) do |f|
          f.write <<EOT

ruby:
  version: 1.9
EOT
        end
      end
      accum
    end
  end
  
  def create_archive_for_resources paths, name=nil
    paths = [ paths ].flatten
    if ( name.nil? )
      first_path = paths.first
      tail = first_path.split('/')[-1]
      name = /(.*)\./.match(tail)[1]
      name.gsub!(/-19$/, '') if @ruby_1_9
    end
    archive = org.jboss.shrinkwrap.api.ShrinkWrap.create( org.jboss.shrinkwrap.api.spec.JavaArchive.java_class, "#{name}.jar" )
    paths.each do |path|
      tail = path.split('/')[-1]
      deploymentDescriptorUrl = JRuby.runtime.jruby_class_loader.getResource( path )
      tail.gsub!(/-19\./, '.') if @ruby_1_9
      archive.addResource( deploymentDescriptorUrl, "/META-INF/#{tail}" )
    end
    archive
  end
  
  def run_mode
    case @run_mode
    when :client
      org.jboss.arquillian.api.RunModeType::AS_CLIENT
    when :container
      org.jboss.arquillian.api.RunModeType::IN_CONTAINER
    else
      raise "Unknown :run_mode -- acceptable values are :client or :container"
    end 
  end

end

begin
  require 'rspec'
  ArquillianMethods::Configurator = RSpec
rescue Exception
  require 'spec'
  ArquillianMethods::Configurator = Spec::Runner
end

ArquillianMethods::Configurator.configure do |config|

  config.extend(ArquillianMethods)

  config.before(:suite) do
    configuration = org.jboss.arquillian.impl.XmlConfigurationBuilder.new.build()
    Thread.current[:test_runner_adaptor] = org.jboss.arquillian.impl.DeployableTestBuilder.build(configuration)
    Thread.current[:test_runner_adaptor].beforeSuite
  end
    
  config.before(:all) do
    if (self.class.respond_to? :create_deployment)
      @real_java_class = self.class.become_java!  # ('.') 
      Thread.current[:test_runner_adaptor].beforeClass(@real_java_class)
    end
  end

  config.after(:all) do
    Thread.current[:test_runner_adaptor].afterClass(@real_java_class) if @real_java_class
  end

  config.after(:suite) do
    Thread.current[:test_runner_adaptor].afterSuite rescue nil
  end

end

