$LOAD_PATH.unshift "#{__dir__}/../psych-load"
require "#{__dir__}/../psych-load/bundle/bundler/setup"
require 'psych'

class PsychLoad < Benchmarks

    def initialize
        Dir.chdir("#{__dir__}/../psych-load") do
            test_yaml_files = Dir["./yaml/*.yaml"].to_a

            # Useful for testing only specific YAML files. I don't think we want
            # a separate benchmark for each YAML file here.
            if ENV['PSYCH_ONLY_LOAD']
                test_yaml_files.select! { |path| path[ENV['PSYCH_ONLY_LOAD']] }
            end

            if test_yaml_files.size < 1
                raise "Not loading any YAML files!"
            end

            @test_yaml = test_yaml_files.map { |p| File.read(p) }
        end
    end

    def benchmark
        10.times do
            @test_yaml.each do |yaml_content|
                y = Psych.load(yaml_content)
            end
        end
    end

    def verify_result(result)
        true
    end

end
