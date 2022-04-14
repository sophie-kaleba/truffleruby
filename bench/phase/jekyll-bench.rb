$LOAD_PATH.unshift "#{__dir__}/../jekyll"
require "jekyll"
require "fileutils"

class JekyllBench < Benchmarks

    def initialize
        # Jekyll isn't designed to be used in quite this way, and doesn't seem to handle the same
        # process cleaning and then building repeatedly in the obvious way. Rather than try to
        # add new functionality to Jekyll, it makes more sense to carefully exclude the initial
        # slow first build from the benchmarked time, and then only benchmark incremental runs
        # where specific markdown files change.

        # To do incremental builds, we take a markdown file and add a small, changing modification
        # to the end. Like the original benchmark we append a number. Unlike the original, we keep
        # the file size the same for each incremental build.
        Dir.chdir("#{__dir__}/../jekyll/test-three-zero") do
            puts "################################################"
            md_file = "./_posts/2009-05-15-edge-case-nested-and-mixed-lists.md"
            puts "################################################"
            @md_file_size = File.size(md_file)
            puts "################################################"
            # Make sure the first possibly-slow run has completed.
            Jekyll::Commands::Build.process({})
        end
    end

    # TODO: try updating a lot of different markdown files instead of just one and see how much it affects the build time?

    def benchmark
        5.times do |i|
            Dir.chdir("#{__dir__}/../jekyll/test-three-zero") do
            # Replace the final number with 0, 1 or 2 for ensure the content changes.
                File.write("./_posts/2009-05-15-edge-case-nested-and-mixed-lists.md", (i % 3).to_s, @md_file_size - 2)
                Jekyll::Commands::Build.process({})
            end
        end
    end

    def verify_result(result)
        true
    end

end
