ENV['BUNDLE_GEMFILE'] ||= File.expand_path('../Gemfile', __dir__)

#require 'bundler/setup'
require "#{__dir__}/../bundle/bundler/setup" #use the gems from the standalone setup
#require 'bootsnap/setup' # Speed up boot time by caching expensive operations.
