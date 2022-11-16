# truffleruby_primitives: true

require_relative "LookupCacheOneCaller"

lc = LookupCacheOneCaller.new()
puts ARGV[0]

lc.init(ARGV[0].to_i)

Primitive.monitor_calls true
lc.run()
Primitive.monitor_calls false