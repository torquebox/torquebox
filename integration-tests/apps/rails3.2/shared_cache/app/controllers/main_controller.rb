class MainController < ApplicationController
  def index
    check_for_success( false )
  end

  def persisted
    check_for_success( true )
  end

  def check_for_success( persisted )
    name = persisted ? "persisted_cache" : "memory_cache"
    cache = TorqueBox::Infinispan::Cache.new( :name => name, :persist => persisted, :encoding => ENV['CACHE_ENCODING'].to_sym )
    cache_random_junk( cache )
    @success = !cache.get( "time" ).nil?
    cache.clear
  end

  def cache_random_junk(cache)
    # stuff the cache with various things to reproduce TORQUE-1073
    cache.put('mogotest_snippet', %q{<li class="site with_screenshot"><div class="details"><div class="thumb"><a href="/sites/dev-albumexposure-com--2/groups/wt8l6w/tests/latest" class="target"><img alt="Thumb_screenshot" src="http://localhost:8080/screenshots/html_document/screenshot_canvas_windows_chrome/1362429501/897191/thumb_screenshot.png" /></a><div class="site-actions mogo-hidden"><ul><li class="show"><a href="/sites/dev-albumexposure-com--2/groups" title="Show Test Groups"><span>Show Groups</span></a></li><li class="add"><a href="/sites/dev-albumexposure-com--2/groups/new" title="Add Test Group"><span>Add Group</span></a></li><li class="edit"><a href="/sites/dev-albumexposure-com--2/edit" title="Edit Site"})
    cache.get('mogotest_snippet')

    cache.put('bacon_ipsum', %q{Bacon ipsum dolor sit amet pork belly doner kielbasa brisket flank chicken chuck sirloin venison shank fatback swine. Spare ribs pork loin ribeye strip steak beef ribs kielbasa prosciutto. Drumstick tenderloin ribeye brisket flank, t-bone pastrami sausage frankfurter venison ham hock pig. Ham hock beef ribs doner pork brisket frankfurter boudin chuck biltong flank pork chop. Filet mignon swine short loin cow ground round. Andouille flank sausage ground round salami beef ribs doner pig venison ribeye. Biltong fatback tail t-bone tenderloin jerky rump pork belly chuck ball tip tri-tip.})
    cache.get('bacon_ipsum')

    cache.put('foobarbaz', {:foo => {:bar => {:baz => true }}})
    cache.get('foobarbaz')
  end
end
