
assembly_dir=$1
output_dir=$2
root_war=$3

echo "assembly from... $assembly_dir"
echo "output to....... $output_dir"
echo "ROOT.war from... $root_war"

if [ $assembly_dir = $output_dir ] ; then
  echo "*** Setting up integration tests against assembly, NOT A COPY."
elif [ -e $output_dir ] ; then
  echo "*** integ-dist appears to be in place, not copying"
else
  echo "*** Copying to integ-dist with hardlinks"
  cd $assembly_dir
  find . | grep -v jruby/share/ri | grep -v jruby/lib/ruby/gems/1.8/doc | cpio -pmudL $output_dir
fi


if [ ! -e "$output_dir/jboss/server/default/deploy/ROOT.war" ] ; then
  echo "*** Installing ROOT.war"
  cp $root_war $output_dir/jboss/server/default/deploy/
fi


gem_install_opts="--no-ri --no-rdoc"

JRUBY_HOME=$output_dir/jruby

$JRUBY_HOME/bin/jruby -S gem list | grep sinatra

if [ $? != 0 ] ; then 
  echo "*** Installing sinatra.gem"
  $JRUBY_HOME/bin/jruby -S gem install $gem_install_opts sinatra -v 1.0
fi

$JRUBY_HOME/bin/jruby -S gem list | grep haml

if [ $? != 0 ] ; then
  echo "*** Installing haml.gem"
  $JRUBY_HOME/bin/jruby -S gem install $gem_install_opts haml -v 3.0.17
fi

