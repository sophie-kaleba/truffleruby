require 'rbconfig'
ruby_engine = RUBY_ENGINE
ruby_version = RbConfig::CONFIG["ruby_version"]
path = File.expand_path('..', __FILE__)
$:.unshift "#{path}/"
$:.unshift "#{path}/../#{ruby_engine}/#{ruby_version}/extensions/x86_64-linux/3.1.2.3/psych-4.0.1"
$:.unshift "#{path}/../#{ruby_engine}/#{ruby_version}/gems/psych-4.0.1/lib"
