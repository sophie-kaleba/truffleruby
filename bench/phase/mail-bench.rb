$LOAD_PATH.unshift "#{__dir__}/../mail"
require "#{__dir__}/../mail/bundle/bundler/setup"
require "mail"

class MailBench < Benchmarks
  def initialize
    Dir.chdir("#{__dir__}/../mail") do
      @raw_email = File.binread("./raw_email2.eml")
    end
  end

  def benchmark
      50.times do
        Mail::new(@raw_email).to_s
      end
  end

  def verify_result(result)
    true
  end

end
