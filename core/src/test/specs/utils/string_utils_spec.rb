
import org.torquebox.ruby.core.util.StringUtils

describe StringUtils do
  
  describe "camelize" do 
    
    it "should capitalize the first character" do
      StringUtils::camelize( "foo" ).should eql( "Foo" )
    end
    
    it "should replace slashes with double-colons" do
      StringUtils::camelize( "/foo/bar" ).should eql( "::Foo::Bar" )
      StringUtils::camelize( "foo/bar" ).should eql( "Foo::Bar" )
    end
    
    it "should remove underscores and capitalize subsequent letters" do
      StringUtils::camelize( "foo_bar" ).should eql( "FooBar" )
      StringUtils::camelize( "foo_bar/baz" ).should eql( "FooBar::Baz" )
    end
  end

  describe "converting paths to class names" do
    it "should remove .rb extension by default" do
      StringUtils::pathToClassName( "foo.rb" ).should eql( "Foo" )
    end
    it "should be happy without an extension" do
      StringUtils::pathToClassName( "foo").should eql( "Foo" )
    end
    it "should remove leading delimeter" do
      StringUtils::pathToClassName( "/foo.rb").should eql( "Foo" )
      StringUtils::pathToClassName( "/foo").should eql( "Foo" )
    end
    describe "accomodating namespaces" do
      it "should work with extensions" do
        StringUtils::pathToClassName( "/foo/bar/baz.rb").should eql( "Foo::Bar::Baz" )
        StringUtils::pathToClassName( "foo/bar/baz.rb").should eql( "Foo::Bar::Baz" )
      end
      it "should work without extensions" do
        StringUtils::pathToClassName( "/foo/bar/baz").should eql( "Foo::Bar::Baz" )
        StringUtils::pathToClassName( "foo/bar/baz" ).should eql( "Foo::Bar::Baz" )
      end
    end
  end

end
