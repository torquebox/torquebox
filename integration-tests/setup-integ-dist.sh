
assembly_dir=$1
output_dir=$2
root_war=$3

echo "assembly from... $assembly_dir"
echo "output to....... $output_dir"
echo "ROOT.war from... $root_war"

if [ -e $output_dir ] ; then
  echo "*** integ-dist appears to be in place, not copying"
else
  echo "*** copying to integ-dist with hardlinks"
  cd $assembly_dir
  find . | grep -v jruby/share/ri | grep -v jruby/lib/ruby/gems/1.8/doc | cpio -pmudL $output_dir
  cd $output_dir
  cp $root_war $output_dir/jboss/server/default/deploy/
  JRUBY_HOME=$output_dir/jruby
  $JRUBY_HOME/bin/jruby -S gem install sinatra -v 1.0
  $JRUBY_HOME/bin/jruby -S gem install haml -v 3.0.17
fi

