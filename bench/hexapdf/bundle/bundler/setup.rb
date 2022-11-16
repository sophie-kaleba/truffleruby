require 'rbconfig'
kernel = (class << ::Kernel; self; end)
[kernel, ::Kernel].each do |k|
  if k.private_method_defined?(:gem_original_require)
    private_require = k.private_method_defined?(:require)
    k.send(:remove_method, :require)
    k.send(:define_method, :require, k.instance_method(:gem_original_require))
    k.send(:private, :require) if private_require
  end
end
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/cmdparse-3.0.7/lib")
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/geom2d-0.3.1/lib")
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/hexapdf-0.16.0/lib")
