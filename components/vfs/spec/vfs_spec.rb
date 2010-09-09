
describe "VFS path resolution" do

  it "should prefix relative paths with the current dir" do
    cwd = Dir.pwd
    path = VFS.resolve_path_url( "foo/bar" )
    path.should match /^vfs:#{cwd}\/foo\/bar$/
  end

  it "should not prefix absolute paths with the current dir" do
    path = VFS.resolve_path_url( "/foo/bar" )
    path.should match /^vfs:\/foo\/bar$/
  end

  it "should treat paths with windows drive letters as absolute" do
    path = VFS.resolve_path_url( "C:/foo/bar" )
    path.should match /^vfs:\/C:\/foo\/bar$/
  end

end
