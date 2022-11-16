# truffleruby_primitives: true

require_relative "DocSplitting"

sd = DocSplitting.new()

Primitive.monitor_calls true
sd.run()
Primitive.monitor_calls false