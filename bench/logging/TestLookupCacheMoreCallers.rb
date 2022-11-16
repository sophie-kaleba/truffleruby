# truffleruby_primitives: true

require_relative "LookupCacheMoreCallers"

lc = LookupCacheMoreCallers.new()
puts ARGV[0]

lc.init(ARGV[0].to_i)

Primitive.monitor_calls true
lc.run()
Primitive.monitor_calls false