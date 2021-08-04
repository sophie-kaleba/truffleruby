Dir.chdir(__dir__ + "/test-three-zero")

require 'bundler/inline'
gemfile do
    eval File.read("./Gemfile")
end
require "jekyll"

# Jekyll isn't designed to be used in quite this way, and doesn't seem to handle the same
# process cleaning and then building repeatedly in the obvious way. Rather than try to
# add new functionality to Jekyll, it makes more sense to carefully exclude the initial
# slow first build from the benchmarked time, and then only benchmark incremental runs
# where specific markdown files change.

# To do incremental builds, we take a markdown file and add a small, changing modification
# to the end. Like the original benchmark we append a number. Unlike the original, we keep
# the file size the same for each incremental build.
class Sitebench < Benchmark
  def initialize()
    @md_file = "./_posts/2009-05-15-edge-case-nested-and-mixed-lists.md"
    @md_file_size = File.size(@md_file)
    @iterations = 1
    # Make sure the first possibly-slow run has completed.
    Jekyll::Commands::Build.process({})
  end

  # TODO: try updating a lot of different markdown files instead of just one and see how much it affects the build time?
  def benchmark
    sum = 0
    @iterations.times do |i|
      # Replace the final number with 0, 1 or 2 for ensure the content changes.
      File.write(@md_file, (i % 3).to_s, @md_file_size - 2)
      Jekyll::Commands::Build.process({})
      sum += 1
    end
    sum
  end
  
  def verify_result(result)
    result == @iterations
  end
end