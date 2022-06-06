$LOAD_PATH.unshift "#{__dir__}/../sinatra/bundle"
require 'bundler/setup'

require 'sinatra/base'

class SinatraHello < Benchmarks
  def initialize
    @app = App
    @env = Rack::MockRequest.env_for('/', { method: Rack::GET })
  end

  def benchmark
    @app.call(@env.dup)
  end

  def verify_result(result)
    result[0] == 200
  end

  class App < Sinatra::Base
    set :environment, :production

    get '/' do
      'apples, oranges & bananas'
    end
  end
end