# Ensure the ruby in PATH is the ruby running this, so we can safely shell out to other commands
require_relative '../../tool/jt.rb'

def run_cmd(*args)
  puts "Command: #{args.join(" ")}"
  system(*args)
end

def setup_cmds(c)
  c.each do |cmd|
    success = run_cmd(cmd)
    raise "Couldn't run setup command for benchmark in #{Dir.pwd.inspect}!" unless success
  end
end

# Set up a Gemfile, install gems and do extra setup
def use_gemfile(extra_setup_cmd: nil)
  # Benchmarks should normally set their current directory and then call this method.

  JT.ruby(*%w[-S bundle install])

  # Need to be in the appropriate directory for this...
  require "bundler/setup"
end

