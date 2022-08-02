source 'https://rubygems.org'
git_source(:github) { |repo| "https://github.com/#{repo}.git" }

# Containing gems for blogRailsBench, ErubiRailsBench and ActiveRecordBench

#ruby '3.0.0'
# Additional for ActiveRecordBench
gem "activerecord", "~> 6.0.3", ">= 6.0.3.6"

# Additional for erubi_bench
# Use Puma as the app server
gem 'puma', '~> 5.0'
# Turbolinks makes navigating your web application faster. Read more: https://github.com/turbolinks/turbolinks
gem 'turbolinks', '~> 5'
# Transpile app-like JavaScript. Read more: https://github.com/rails/webpacker
gem 'webpacker', '~> 5.0'
gem 'activerecord-jdbcsqlite3-adapter', '~> 61', platform: :jruby
# Bundle edge Rails instead: gem 'rails', github: 'rails/rails', branch: 'main'
#gem 'rails', '~> 6.1.4', '>= 6.1.4.1'

# For the blog app

gem 'stackprof', platforms: [:mri]
# Bundle edge Rails instead: gem 'rails', github: 'rails/rails'
gem 'rails', '~> 6.0.3', '>= 6.0.3.6'
# Use sqlite3 as the database for Active Record
gem 'sqlite3', '~> 1.4'
# Use webrick for the web server since it's easy to install.
# The web server is not used during the benchmark.
gem 'webrick', '~> 1.7.0'
# Use SCSS for stylesheets
gem 'sass-rails', '>= 6'
# Build JSON APIs with ease. Read more: https://github.com/rails/jbuilder
# Needed in the benchmark for json responses
gem 'jbuilder', '~> 2.7'

# Transpile app-like JavaScript. Read more: https://github.com/rails/webpacker
# gem 'webpacker', '~> 4.0'
# Benchmark doesn't use Turbolinks
# gem 'turbolinks', '~> 5'

# Use Redis adapter to run Action Cable in production
# gem 'redis', '~> 4.0'
# Use Active Model has_secure_password
# gem 'bcrypt', '~> 3.1.7'

# Use Active Storage variant
# gem 'image_processing', '~> 1.2'

# Reduces boot times through caching; required in config/boot.rb
#gem 'bootsnap', '>= 1.4.2', require: false

gem 'psych', '~> 3.3.2'

group :development do
  gem 'listen', '~> 3.2'
  # Call 'byebug' anywhere in the code to stop execution and get a debugger console
  gem 'byebug', platforms: [:mri, :mingw, :x64_mingw]
  # Spring speeds up development by keeping your application running in the background. Read more: https://github.com/rails/spring
  gem 'spring'
  gem 'web-console', '>= 4.1.0'
  # Display performance information such as SQL time and flame graphs for each request in your browser.
  # Can be configured to work on production as well see: https://github.com/MiniProfiler/rack-mini-profiler/blob/master/README.md
  gem 'rack-mini-profiler', '~> 2.0'
end

group :test do
  # Adds support for Capybara system testing and selenium driver
  gem 'capybara', '>= 3.26'
  gem 'selenium-webdriver'
  # Easy installation and use of web drivers to run system tests with browsers
  gem 'webdrivers'
end

if RUBY_VERSION >= "3.1"
  # net-smtp, net-imap and net-pop were removed from default gems in Ruby 3.1
  gem "net-smtp", "~> 0.2.1", require: false
  gem "net-imap", "~> 0.2.1", require: false
  gem "net-pop", "~> 0.1.1", require: false

  # matrix was removed from default gems in Ruby 3.1, but is used by the `capybara` gem.
  # So we need to add it as a dependency until `capybara` is fixed: https://github.com/teamcapybara/capybara/pull/2468
  #gem "matrix", require: false
  gem "securerandom", "0.1.1"
end

# Windows does not include zoneinfo files, so bundle the tzinfo-data gem
gem 'tzinfo-data', platforms: [:mingw, :mswin, :x64_mingw, :jruby]

