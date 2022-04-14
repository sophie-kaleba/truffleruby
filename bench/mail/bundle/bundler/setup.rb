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
$:.unshift File.expand_path("#{path}/")
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/extensions/x86_64-linux/3.0.2.10/digest-3.1.0")
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/digest-3.1.0/lib")
$:.unshift File.expand_path("#{path}/../../../../../graal/sdk/mxbuild/linux-amd64/GRAALVM_25F9771D4B_JAVA11/graalvm-25f9771d4b-java11-22.1.0-dev/languages/ruby/lib/gems/extensions/x86_64-linux/3.0.2.10/io-wait-0.1.0")
$:.unshift File.expand_path("#{path}/../../../../../graal/sdk/mxbuild/linux-amd64/GRAALVM_25F9771D4B_JAVA11/graalvm-25f9771d4b-java11-22.1.0-dev/languages/ruby/lib/gems/gems/io-wait-0.1.0/lib")
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/mini_mime-1.1.2/lib")
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/mail-2.7.1/lib")
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/timeout-0.2.0/lib")
$:.unshift File.expand_path("#{path}/../#{ruby_engine}/#{ruby_version}/gems/net-protocol-0.1.2/lib")
$:.unshift File.expand_path("#{path}/../../../../../graal/sdk/mxbuild/linux-amd64/GRAALVM_25F9771D4B_JAVA11/graalvm-25f9771d4b-java11-22.1.0-dev/languages/ruby/lib/gems/gems/net-smtp-0.2.1/lib")
