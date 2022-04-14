require 'rbconfig'
ruby_engine = RUBY_ENGINE
ruby_version = RbConfig::CONFIG["ruby_version"]
path = File.expand_path('..', __FILE__)
kernel = (class << ::Kernel; self; end)
[kernel, ::Kernel].each do |k|
  if k.private_method_defined?(:gem_original_require)
    private_require = k.private_method_defined?(:require)
    k.send(:remove_method, :require)
    k.send(:define_method, :require, k.instance_method(:gem_original_require))
    k.send(:private, :require) if private_require
  end
end
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/benchmark-ips-2.8.3/lib")
$:.unshift File.expand_path("#{path}/")
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/victor-0.3.2/lib")
