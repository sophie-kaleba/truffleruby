$LOAD_PATH.unshift "#{__dir__}/../erubi_rails"
require "#{__dir__}/../erubi_rails/bundle/bundler/setup"
require_relative '../../tool/jt.rb'
EXPECTED_TEXT_SIZE = 9369

class ERubiRails < Benchmarks 

  def initialize
    ENV['RAILS_ENV'] ||= 'production'
    # The SECRET_KEY_BASE isn't used for anything, but we have to have one.
    ENV['SECRET_KEY_BASE'] = "1d1214a477334166ec542edb79047c7a042fb2b6dc90206d07b580615e0165c0371f365f20c93a06532b8462c11c0ce59da885734cb7b4e46805e2580b26ece5"
    require 'config/environment'

    app = Rails.application
    @fake_controller = FakeDiscourseController.new
  end

  def benchmark
    100.times do
      out = FakeDiscourseController.render :topics_show, assigns: @fake_controller.stub_assigns
    end
  end

  def verify_result(result)
    # TODO - add some sort of verification
    true
  end

end