
require 'rubygems/installer'

namespace :jboss do 

  namespace :gems do

    task :setup_cache do
      puts "setting up gem cache"
      explode_dir = JBoss::RakeUtils.explode_deployer
      puts "explode to #{explode_dir}"
      cache_dir="#{explode_dir}/gems/cache"
      raise "\n\n** No gems cache; please ensure you're using a full build of the deployer\n\n" unless File.exist?( cache_dir )
      GEMS_CACHE=cache_dir
    end
 
    namespace :'jdbc' do 

      DB_TYPES = { 
        "derby"=>"derby", 
        "h2"=>"h2",
        "hsqldb"=>"hsqldb", 
        "mysql"=>"mysql", 
        "postgresql"=>"postgres", 
        "sqlite3"=>"sqlite3",
      }

      VENDOR_PLUGINS = "#{RAILS_ROOT}/vendor/plugins"

      task :check=>['jboss:as:check']
    
      desc "Install needed JDBC drivers to vendor/plugins/"
      task :'install' do
        Rake::Task['jboss:gems:jdbc:install:auto'].invoke
      end

      desc "Uninstall JDBC drivers"
      task :'uninstall'=>[:check] do
        files = Dir["#{VENDOR_PLUGINS}/activerecord-jdbc*"] + Dir["#{VENDOR_PLUGINS}/jdbc-*"]
        for file in files
          FileUtils.rm_rf( file )
        end
      end
      namespace :'install' do
        task :'auto'=>[:check] do
          database_yml   = YAML.load_file( "#{RAILS_ROOT}/config/database.yml" )
          db_types = []
      
          database_yml.each do |env,db_config|
            adapter = db_config['adapter']
            if ( DB_TYPES.include?( adapter ) )
              db_types << adapter
            elsif ( adapter == 'jdbc' )
              puts "INFO: config/database.yml:#{env}: No need to use the 'jdbc' adapter"
              db_types << simple_adapter
            elsif ( adapter =~ /^jdbc(.+)/ )
              adapter = $1
              if ( DB_TYPES.include? ( adapter ) )
                db_types << adapter
              end
            else
              puts "WARNING: config/database.yml:#{env}: Unknown adapter: #{adapter}"
            end
          end
    
          db_types.uniq!
          db_types.each do |db_type|
            Rake::Task["jboss:gems:jdbc:install:#{db_type}"].invoke
          end
        end
    
        task :check=>['jboss:as:check']
    
        def install_gem_safely(gem_path)
          gem_name = File.basename( gem_path, ".gem" )
          simple_gem_name = File.basename( gem_path )
          simple_gem_name = simple_gem_name.gsub( /-([0-9]+\.)+gem$/, '' )
      
          existing = Dir[ "#{VENDOR_PLUGINS}/#{simple_gem_name}-*" ]
          unless ( existing.empty? )
            puts "WARNING: Gem exists; not installing: #{simple_gem_name}"
            return
          end
          puts "INFO: Installing #{gem_name}"
          Gem::Installer.new( gem_path ).unpack( "#{VENDOR_PLUGINS}/#{gem_name}" )
        end
    
        desc "Install the base activerecord-jdbc gem"
        task :base=>[:check,'jboss:gems:setup_cache'] do
          db_gem = Dir["#{GEMS_CACHE}/activerecord-jdbc-adapter-*.gem"].first
          install_gem_safely( db_gem )
        end
    
        DB_TYPES.keys.each do |db_type|
          desc "Install the activerecord-jdbc-#{db_type} gems"
          task db_type.to_sym=>[:base] do
            glob = "#{GEMS_CACHE}/jdbc-#{db_type}-*.gem"
            db_gems = Dir["#{GEMS_CACHE}/activerecord-jdbc#{db_type}-adapter-*.gem"]
            if ( DB_TYPES[db_type] != nil )
              db_gems += Dir["#{GEMS_CACHE}/jdbc-#{DB_TYPES[db_type]}-*.gem"]
            end
            db_gems.each do |db_gem|
              install_gem_safely( db_gem )
            end
          end
        end
      end
    end

  end

end

