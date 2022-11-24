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
$:.unshift File.expand_path("#{__dir__}/../../../../../graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/gems/extensions/x86_64-linux/3.1.2.3/digest-3.1.0")
$:.unshift File.expand_path("#{__dir__}/../../../../../graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/gems/gems/digest-3.1.0/lib")
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/io-wait-0.1.0/lib")
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/mini_mime-1.1.2/lib")
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/mail-2.7.1/lib")
$:.unshift File.expand_path("#{__dir__}/../../../../../graal/sdk/mxbuild/linux-amd64/GRAALVM_A33290E019_JAVA11/graalvm-a33290e019-java11-23.0.0-dev/languages/ruby/lib/gems/gems/timeout-0.2.0/lib")
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/net-protocol-0.1.3/lib")
$:.unshift File.expand_path("#{__dir__}/../#{RUBY_ENGINE}/#{RbConfig::CONFIG["ruby_version"]}/gems/net-smtp-0.2.1/lib")
