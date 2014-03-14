require 'test/unit'
require 'thread_safe/synchronized_delegator.rb'

class TestSynchronizedDelegator < Test::Unit::TestCase
  def test_wraps_array
    ary = []
    sync_ary = SynchronizedDelegator.new(ary)

    ary << 1
    assert_equal 1, sync_ary[0]
  end

  def test_synchronizes_access
    ary = []
    sync_ary = SynchronizedDelegator.new(ary)

    t1_continue = false
    t2_continue = false

    t1 = Thread.new do
      sync_ary << 1
      sync_ary.each do
        t2_continue = true
        Thread.pass until t1_continue
      end
    end

    t2 = Thread.new do
      Thread.pass until t2_continue
      sync_ary << 2
    end
    
    Thread.pass until t2.status == 'sleep'
    assert_equal 1, ary.size

    t1_continue = true
    t1.join
    t2.join

    assert_equal 2, sync_ary.size
  end
end
