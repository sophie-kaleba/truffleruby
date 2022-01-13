# Adapted from the yjit-benchmark, available @ https://github.com/Shopify/yjit-bench/tree/main/benchmarks/railsbench
$LOAD_PATH.unshift "#{__dir__}/../railsbench"
require_relative '../../tool/jt.rb'

class BlogRailsRoutes < Benchmarks 

  def initialize
    Dir.chdir("#{__dir__}/../railsbench") do
      JT.ruby(*%w[-S bundle exec bin/rails db:migrate db:seed RAILS_ENV=production])
    end

    ENV['RAILS_ENV'] ||= 'production'

    require 'config/environment'

    @app = Rails.application
    #possible_routes = ['/posts', '/posts.json']
    #possible_routes.concat((1..100).map { |i| "/posts/#{i}"})
    
    #visit_count = 10
    #rng = Random.new(0x1be52551fc152997)
    #@visiting_routes = Array.new(visit_count) { possible_routes.sample(random: rng) }
    #@visiting_routes = ['/posts.json',  '/posts/25']
  end

  def benchmark
      # The app mutates `env`, so we need to create one every time.
      for a in 1..100 do
          env = Rack::MockRequest::env_for("http://localhost/posts/25")
          response_array = @app.call(env)
          unless response_array.first == 200 
            raise "HTTP response is #{response_array.first} instead of 200. Is the benchmark app properly set up? See README.md."
          end
      end

      for a in 1..100 do
          env = Rack::MockRequest::env_for("http://localhost/posts", :method => "POST", :params => {post:{body: "Un body new ", title: "toto new", published: false}})
          response_array = @app.call(env)
          unless response_array.first == 302
            raise "HTTP response is #{response_array.first} instead of 200. Is the benchmark app properly set up? See README.md."
          end
      end
  end

  def verify_result(result)
    # TODO - add some sort of verification
    true
  end

end
