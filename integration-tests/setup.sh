
assembly_dir=$1
output_dir=$2
root_war=$3

cd $assembly_dir
find . | grep -v jruby/share/ri | grep -v jruby/lib/ruby/gems/1.8/doc | cpio -pmvudL $output_dir
cd $output_dir
cp $root_war $output_dir/jboss/server/default/deploy/

