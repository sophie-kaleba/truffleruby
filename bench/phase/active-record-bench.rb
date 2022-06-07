$LOAD_PATH.unshift "#{__dir__}/../activerecord"
#require "#{__dir__}/../activerecord/bundle/bundler/setup"
require "securerandom"

Dir.chdir("#{__dir__}/../activerecord") do
  JT.rubyce(*%w[-S bundle install])
  require "active_record"
end

class ActiveRecordBench < Benchmarks 

  class Post < ActiveRecord::Base 
  end

  def initialize
    ActiveRecord::Base.establish_connection adapter: "sqlite3", database: ":memory:"

    ActiveRecord::Schema.define do
      create_table :posts, force: true do |t|
        t.string :title, null: false
        t.string :body
        t.string :type_name, null: false
        t.string :key, null: false
        t.integer :upvotes, null: false
        t.integer :author_id, null: false
        t.timestamps
      end
    end

    50000.times {
      Post.create!(title: SecureRandom.alphanumeric(30),
                  type_name: SecureRandom.alphanumeric(10),
                  key: SecureRandom.alphanumeric(10),
                  body: SecureRandom.alphanumeric(100),
                  upvotes: rand(30),
                  author_id: rand(30))
    }

    # heat any caches
    Post.where(id: 1).first.title
  end

  def benchmark
    100.times do |i|
      Post.where(id: i + 1).first.title
    end
  end

  def verify_result(result)
    # TODO - add some sort of verification
    true
  end

end
